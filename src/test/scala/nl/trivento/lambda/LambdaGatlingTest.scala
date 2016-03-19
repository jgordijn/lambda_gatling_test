package nl.trivento.lambda

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class LambdaGatlingTest extends Simulation {
  val conf = http.baseURL("https://rk9wlzrsqb.execute-api.eu-west-1.amazonaws.com/prod")

  val javascn = scenario("Java Test")
        .exec(http("java lambda")
        .get("/java"))
  val nodescn = scenario("Node Test")
        .exec(http("node lambda")
        .get("/node"))


  setUp(nodescn.inject(constantUsersPerSec(25) during (10 minutes)),
    javascn.inject(constantUsersPerSec(25) during (10 minutes)))
        .protocols(conf)
}
