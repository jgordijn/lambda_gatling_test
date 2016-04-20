package nl.trivento.lambda

import java.nio.ByteBuffer

import akka.actor.ActorDSL._
import akka.actor.ActorRef
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.regions.{Region, Regions}
import com.amazonaws.services.lambda.AWSLambdaClient
import com.amazonaws.services.lambda.model.InvokeRequest
import io.gatling.core.action.Chainable
import io.gatling.core.action.builder.ActionBuilder
import io.gatling.core.config.{Protocol, Protocols}
import io.gatling.core.result.message.{KO, OK}
import io.gatling.core.result.writer.DataWriterClient
import io.gatling.core.session.Session
import io.gatling.core.util.TimeHelper


case class LambdaProtocol(awsAccessKeyId: String, awsSecretAccessKey: String) extends Protocol {
  val credentials = new BasicAWSCredentials(awsAccessKeyId, awsSecretAccessKey)
  val lambdaClient = new AWSLambdaClient(credentials)
  lambdaClient.setRegion(Region.getRegion(Regions.EU_WEST_1))


  def bytesToString(buffer: ByteBuffer): String = {
    val bytes = buffer.array()
    return new String(bytes, "UTF-8")
  }

  def call(functionName: String): Int = {
    val request = new InvokeRequest
    request.setFunctionName(functionName)
    val result = lambdaClient.invoke(request)
    println(bytesToString(result.getPayload))
    result.getStatusCode
  }
}

class FunctionCall(functionName: String, protocol: LambdaProtocol, val next: ActorRef) extends Chainable with DataWriterClient {

  override def execute(session: Session): Unit = {
    val start = TimeHelper.nowMillis
    val result = protocol.call(functionName)
    val end = TimeHelper.nowMillis
    if(result >= 200 && result <= 299)
      writeRequestData(session, "Call function", start, start, end, end, OK )
    else
      writeRequestData(session, "Call function", start, start, end, end, KO )
    next ! session
  }
}

case class AWSLambdaBuilder(functionName: String) extends ActionBuilder {
  def lambdaProtocol(protocols: Protocols) = protocols.getProtocol[LambdaProtocol].getOrElse(throw new UnsupportedOperationException("LambdaProtocol Protocol wasn't registered"))

  override def build(next: ActorRef, protocols: Protocols): ActorRef = {
      actor(actorName("Functioncall"))(new FunctionCall(functionName, lambdaProtocol(protocols), next))
  }
}
