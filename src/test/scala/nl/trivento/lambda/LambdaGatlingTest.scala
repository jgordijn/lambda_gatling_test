package nl.trivento.lambda

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.http.request.{ExtraInfo, ExtraInfoExtractor}

import scala.concurrent.duration._

class LambdaGatlingTest extends Simulation {
  val conf = http.baseURL("https://rk9wlzrsqb.execute-api.eu-west-1.amazonaws.com/prod").extraInfoExtractor(new ExtraInfoExtractor {
    override def apply(v1: ExtraInfo): List[Any] = { println(s">>>>>>>>>> ${v1.response.body.string}"); List(s">>>>>>>>>> ${v1.response.body.string}") }
  })

  val lconf = LambdaProtocol("key", "pass")

  val javascn = scenario("Java Test")
    .exec(http("Call via API Gateway").get("/java"))
  val nodescn = scenario("Node Test")
    .exec(
      http("node lambda")
        .get("/node")
    )

  val laction = scenario("Lambda call")
    .exec(AWSLambdaBuilder("SmallJava"))

  val lactionWithP = scenario("Lambda call")
    .repeat(10, "foo") {
      exec(AWSLambdaBuilder("SmallJava"))
        .pause { session ⇒ Math.pow(2, session("foo").as[Int]).seconds }
    }

  setUp(
    laction.inject(
      atOnceUsers(1),
      nothingFor(	1 second	),  atOnceUsers(1),
      nothingFor(	2	seconds),  atOnceUsers(1),
      nothingFor(	4	seconds),  atOnceUsers(1),
      nothingFor(	8	seconds),  atOnceUsers(1),
      nothingFor(	10 seconds	),  atOnceUsers(1),
      nothingFor(	20 seconds	),  atOnceUsers(1),
      nothingFor(	30 seconds),  atOnceUsers(1),
      nothingFor(	40 seconds),  atOnceUsers(1),
      nothingFor(	50 seconds),  atOnceUsers(1),
      nothingFor(	1 minute	),  atOnceUsers(1),
      nothingFor(	2 minutes	),  atOnceUsers(1),
      nothingFor(	4 minutes	),  atOnceUsers(1),
      nothingFor(	5 minutes	),  atOnceUsers(1),
      nothingFor(	10 minutes	),  atOnceUsers(1),
      nothingFor(	15 minutes	),  atOnceUsers(1),
      nothingFor(	30 minutes	),  atOnceUsers(1),
      nothingFor(	60 minutes	),  atOnceUsers(1),
      nothingFor(	90 minutes	),  atOnceUsers(1),
      nothingFor(	2 hours ),  atOnceUsers(1),
      nothingFor(	3 hours ),  atOnceUsers(1),
      nothingFor(	6 hours),  atOnceUsers(1),
      nothingFor(	9 hours ),  atOnceUsers(1),
      nothingFor(	12 hours ),  atOnceUsers(1),
      nothingFor(	1 day ),  atOnceUsers(1),
      nothingFor(	2 days ),  atOnceUsers(1),
      nothingFor(	3 days ),  atOnceUsers(1)
    )
  )
    .protocols(lconf)


}
