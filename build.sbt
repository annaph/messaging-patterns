name := "messaging-patterns"

organization := "org.messaging.patterns"

scalaVersion := "2.12.3"

scalacOptions ++= Seq(
  "-feature")

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.5.3")

fork := true
