scalaVersion := "2.11.8"
name := "perftest"

libraryDependencies += "io.gatling.highcharts" % "gatling-charts-highcharts" % "2.1.7"
libraryDependencies += "io.gatling"            % "gatling-test-framework"    % "2.1.7" % "test"
libraryDependencies += "com.amazonaws" % "aws-lambda-java-core" % "1.0.0"
libraryDependencies += "com.amazonaws" % "aws-lambda-java-events" % "1.0.0"
libraryDependencies += "com.amazonaws" % "aws-java-sdk" % "1.10.71"

enablePlugins(GatlingPlugin)

updateOptions := updateOptions.value.withCachedResolution(true)
