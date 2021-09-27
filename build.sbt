name := "eskimiTestTask"

version := "0.1"

scalaVersion := "2.13.6"

val akkaVersion = "2.6.8"
val akkaHttpVersion = "10.2.6"



libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
  "ch.qos.logback" % "logback-classic" % "1.2.6" % Runtime
)

