scalaVersion := "2.11.8"
name := "perftest"

libraryDependencies += "io.gatling.highcharts" % "gatling-charts-highcharts" % "2.1.7"
libraryDependencies += "io.gatling"            % "gatling-test-framework"    % "2.1.7" % "test"

enablePlugins(GatlingPlugin)
