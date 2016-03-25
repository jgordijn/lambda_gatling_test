package nl.trivento.lambda

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.http.request.{ExtraInfo, ExtraInfoExtractor}
import scala.concurrent.duration._

class LambdaGatlingTest extends Simulation {
  val conf = http.baseURL("https://rk9wlzrsqb.execute-api.eu-west-1.amazonaws.com/prod").extraInfoExtractor(new ExtraInfoExtractor {
    override def apply(v1: ExtraInfo): List[Any] = {println(s">>>>>>>>>> ${v1.response.body.string}"); List(s">>>>>>>>>> ${v1.response.body.string}")}
  })


  val javascn = scenario("Java Test")
        .exec(http("java lambda")
        .get("/java"))
  val nodescn = scenario("Node Test")
        .exec(http("node lambda")
        .get("/node"))


//  setUp(nodescn.inject(constantUsersPerSec(25) during (60 seconds)),
//    javascn.inject(constantUsersPerSec(25) during (60 seconds)))
//        .protocols(conf)
  setUp(javascn.inject(constantUsersPerSec(100) during (60 seconds)))
        .protocols(conf)
}
