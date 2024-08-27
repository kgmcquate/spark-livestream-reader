scalaVersion := "2.13.14"

name := "spark-livestream-reader"
organization := "com.kgmcquate"
version := "0.1"

val sparkVersion = "3.5.2"

libraryDependencies ++= Seq(
    "org.apache.spark" %% "spark-core" % sparkVersion,
    "org.apache.spark" %% "spark-sql" % sparkVersion,
    "org.apache.spark" %% "spark-sql-kafka-0-10" % sparkVersion,
    "org.apache.spark" %% "spark-streaming-kafka-0-10" % sparkVersion,
    "org.jsoup" % "jsoup" % "1.18.1",
    "com.lihaoyi" %% "ujson" % "3.1.2",
    "io.lindstrom" % "m3u8-parser" % "0.28",
    "org.openpnp" % "opencv" % "4.9.0-0",
    "org.scalatest" %% "scalatest" % "3.2.16" % Test
)

ThisBuild / githubOwner := "kgmcquate"
ThisBuild / githubRepository := "spark-livestream-reader"