package nl.trivento.lambda

import java.nio.ByteBuffer

import akka.actor.{ ActorSystem, Props }
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.handlers.AsyncHandler
import com.amazonaws.regions.{ Region, Regions }
import com.amazonaws.services.lambda.AWSLambdaAsyncClient
import com.amazonaws.services.lambda.model.{ InvokeRequest, InvokeResult }
import io.gatling.commons.stats.{ KO, OK }
import io.gatling.commons.util.TimeHelper
import io.gatling.core.CoreComponents
import io.gatling.core.action.builder.ActionBuilder
import io.gatling.core.action.{ Action, ActionActor, ExitableActorDelegatingAction }
import io.gatling.core.config.GatlingConfiguration
import io.gatling.core.protocol._
import io.gatling.core.session.Session
import io.gatling.core.stats.StatsEngine
import io.gatling.core.stats.message.ResponseTimings
import io.gatling.core.structure.ScenarioContext
import io.gatling.jms.action.JmsReqReply._

import scala.concurrent.{ Future, Promise }

object LambdaProtocol {
  val LambdaProtocolKey = new ProtocolKey {

    type Protocol = LambdaProtocol
    type Components = LambdaComponents

    def protocolClass: Class[io.gatling.core.protocol.Protocol] = classOf[LambdaProtocol].asInstanceOf[Class[io.gatling.core.protocol.Protocol]]

    def defaultValue(configuration: GatlingConfiguration): LambdaProtocol = throw new IllegalStateException("Can't provide a default value for JmsProtocol")

    def newComponents(system: ActorSystem, coreComponents: CoreComponents): LambdaProtocol ⇒ LambdaComponents = {
      lambdaProtocol ⇒ LambdaComponents(lambdaProtocol)
    }
  }

}

case class LambdaProtocol(awsAccessKeyId: String, awsSecretAccessKey: String) extends Protocol {
  val credentials = new BasicAWSCredentials(awsAccessKeyId, awsSecretAccessKey)
  val lambdaClient = new AWSLambdaAsyncClient(credentials)
  lambdaClient.setRegion(Region.getRegion(Regions.EU_WEST_1))

  def bytesToString(buffer: ByteBuffer): String = {
    val bytes = buffer.array()
    return new String(bytes, "UTF-8")
  }

  def asynccall(functionName: String): Future[Int] = {
    val request = new InvokeRequest
    request.setFunctionName(functionName)
    val promise = Promise[Int]()
    lambdaClient.invokeAsync(request, new AsyncHandler[InvokeRequest, InvokeResult] {
      override def onError(exception: Exception): Unit = {
        promise.failure(exception)
      }

      override def onSuccess(request: InvokeRequest, result: InvokeResult): Unit = {
        println(bytesToString(result.getPayload))
        promise.success(result.getStatusCode)
      }
    })
    promise.future
  }

  def call(functionName: String): Int = {
    val request = new InvokeRequest
    request.setFunctionName(functionName)
    val result = lambdaClient.invoke(request)
    println(bytesToString(result.getPayload))
    result.getStatusCode
  }
}

case class LambdaComponents(lambdaProtocol: LambdaProtocol) extends ProtocolComponents {

  def onStart: Option[Session ⇒ Session] = None
  def onExit: Option[Session ⇒ Unit] = None
}

object FunctionCall {
  def apply(functionName: String, protocol: LambdaProtocol, system: ActorSystem, statsEngine: StatsEngine, next: Action) = {
    val actor = system.actorOf(Props(new FunctionCall(functionName, protocol, next, statsEngine)))
    new ExitableActorDelegatingAction(genName("jmsReqReply"), statsEngine, next, actor)
  }

}

class FunctionCall(functionName: String, protocol: LambdaProtocol, val next: Action, statsEngine: StatsEngine) extends ActionActor {

  override def execute(session: Session): Unit = {
    val start = TimeHelper.nowMillis
    val result = protocol.call(functionName)
    val end = TimeHelper.nowMillis
    if (result >= 200 && result <= 299) {
      val timings = ResponseTimings(start, end)
      statsEngine.logResponse(session, "Call function", timings, OK, None, None)
    } else {
      val timings = ResponseTimings(start, end)
      statsEngine.logResponse(session, "Call function", timings, KO, None, None)
    }
    next ! session
  }
}

case class AWSLambdaBuilder(functionName: String) extends ActionBuilder {
  def lambdaProtocol(protocols: Protocols) = protocols.protocol[LambdaProtocol].getOrElse(throw new UnsupportedOperationException("LambdaProtocol Protocol wasn't registered"))

  private def components(protocolComponentsRegistry: ProtocolComponentsRegistry): LambdaComponents =
    protocolComponentsRegistry.components(LambdaProtocol.LambdaProtocolKey)

  override def build(ctx: ScenarioContext, next: Action): Action = {
    import ctx._
    val statsEngine = coreComponents.statsEngine

    val lambdaComponents = components(protocolComponentsRegistry)
    FunctionCall(functionName, lambdaComponents.lambdaProtocol, ctx.system, statsEngine, next)
  }

}

