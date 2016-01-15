import _root_.sbt.Keys._

name := "rxscalatests"

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies += "com.google.guava" % "guava" % "19.0"

libraryDependencies += "io.reactivex" % "rxscala_2.11" % "0.25.1"

libraryDependencies += "org.scalatest" % "scalatest_2.11" % "2.2.5" % "test"



