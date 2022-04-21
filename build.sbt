
organization := "scala.of.coq"

name := "scallina"

version := "0.8-SNAPSHOT"

scalaVersion := "2.13.8"

libraryDependencies ++= Seq(
  "org.scala-lang.modules"  %% "scala-parser-combinators" % "2.1.1",
  "com.eed3si9n" %% "treehugger" % "0.4.4",
  "org.scalatest" %% "scalatest" % "3.2.11" % Test
)
