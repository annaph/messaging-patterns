name := "messaging-patterns"

organization := "org.messaging.patterns"

scalaVersion := "2.12.3"

scalacOptions ++= Seq(
  "-feature")

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.5.3",
  "com.typesafe.akka" %% "akka-persistence" % "2.5.3",
  "org.iq80.leveldb" % "leveldb" % "0.7",
  "org.fusesource.leveldbjni" % "leveldbjni-all" % "1.8",
  "com.google.protobuf" % "protobuf-java" % "3.4.0",
  "org.apache.activemq" % "activemq-broker" % "5.14.5")

fork := true
