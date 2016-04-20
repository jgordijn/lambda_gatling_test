package nl.trivento.lambda

import akka.actor.{ActorRef, Props}
import io.gatling.core.Predef._
import io.gatling.core.action.builder.ActionBuilder
import io.gatling.core.config.{Protocol, Protocols}
import io.gatling.http.Predef._
import io.gatling.http.request.{ExtraInfo, ExtraInfoExtractor}

import scala.concurrent.duration._

class LambdaGatlingTest extends Simulation {
  val conf = http.baseURL("https://rk9wlzrsqb.execute-api.eu-west-1.amazonaws.com/prod").extraInfoExtractor(new ExtraInfoExtractor {
    override def apply(v1: ExtraInfo): List[Any] = {println(s">>>>>>>>>> ${v1.response.body.string}"); List(s">>>>>>>>>> ${v1.response.body.string}")}
  })

  val lconf = LambdaProtocol("key", "pass")

//  val lambdaProtocol = LambdaProtocol

  val javascn = scenario("Java Test")
        .exec(http("java lambda").get("/java"))
  val nodescn = scenario("Node Test")
        .exec(
          http("node lambda")
          .get("/node")
        )

  val laction = scenario("Lambda call")
        .exec(AWSLambdaBuilder("SmallJava"))

//  setUp(nodescn.inject(constantUsersPerSec(25) during (60 seconds))
//    ,
//    javascn.inject(constantUsersPerSec(25) during (10 seconds)))
//  )
//        .protocols(conf)

//  setUp(javascn.inject(constantUsersPerSec(1) during (5 minutes)))
//        .protocols(conf)

    setUp(laction.inject(constantUsersPerSec(50) during (5 minutes)))
      .protocols(lconf)
}
