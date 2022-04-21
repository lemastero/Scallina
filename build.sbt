
organization := "scala.of.coq"

name := "scallina"

version := "0.8-SNAPSHOT"

scalaVersion := "2.13.8"

libraryDependencies ++= Seq(
  "org.scalactic" %% "scalactic" % "3.0.9",
  "org.scalatest" %% "scalatest" % "3.0.9" % Test,
  "org.scala-lang.modules"  %% "scala-parser-combinators" % "2.1.1"
)

libraryDependencies += "com.eed3si9n" %% "treehugger" % "0.4.4"
