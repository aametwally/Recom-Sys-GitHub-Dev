name := "Homework3"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http-experimental" % "2.0.1",
  "org.scalatest"   %% "scalatest"    % "2.2.4"   % "test"
)

// https://mvnrepository.com/artifact/com.typesafe.play/play-json_2.10
libraryDependencies += "com.typesafe.play" % "play-json_2.11" % "2.4.0-M1"
