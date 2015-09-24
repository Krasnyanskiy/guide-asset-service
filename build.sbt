import _root_.sbt.Keys._
import play.Project._

name := "guide-assets"

version := "1.0"

playScalaSettings

libraryDependencies ++= Seq(
  "bbc.curationkit" %% "models" % "0.0.43"
)
