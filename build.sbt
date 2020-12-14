organization := "meetup"
name := "spark-docker-example"

version := "0.1.0"

scalaVersion := "2.12.12"

val sparkVer = "3.0.1"
libraryDependencies += "org.apache.spark" %% "spark-sql" % sparkVer % Provided

enablePlugins(JavaAppPackaging)
enablePlugins(DockerPlugin)
dockerBaseImage := "spark:v3.0.1-demo"
dockerEntrypoint := Seq("/opt/entrypoint.sh")
dockerExposedPorts ++= Seq(4040)
