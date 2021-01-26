ThisBuild / organization := "meetup"
ThisBuild / version      := "0.1.0"
ThisBuild / scalaVersion := "2.12.13"

name := "spark-docker-example"

// It does not really matter for the purpose of the demo
// that the two versions of Spark differ
// The demos don't use anything 3.1.1-specific
val sparkVer = "3.0.1"
val sparkImageVer = "3.1.1-rc1"

// The idea of the build setup is to have the following projects
// (from the least changeable to the most):
// 1. Docker image with Spark only
// 2. Application Dependencies
// 3. Spark application
// That should leverage Docker layers and caching
// which should make builds faster

// k run -ti abc --image=meetup-spark-app:0.1.0 -- bash

lazy val root = (project in file("."))
  .aggregate(
    `meetup-app-deps`,
    `meetup-spark-app`,
    `spark-streams-demo`)

lazy val `meetup-app-deps` = (project in file("meetup-app-deps"))
  .enablePlugins(JavaAppPackaging, DockerPlugin)
  .settings(
    dockerBaseImage := s"spark:v$sparkImageVer",
    // FIXME Are the following settings required?
    dockerEntrypoint := Seq("/opt/entrypoint.sh"),
    dockerExposedPorts := Seq(4040)
  )

import com.typesafe.sbt.packager.docker._
val common = Seq(
  libraryDependencies += "org.apache.spark" %% "spark-sql" % sparkVer % Provided,
  libraryDependencies += "org.apache.spark" %% "spark-kubernetes" % sparkVer % Provided,
  mappings in Universal := {
    val universalMappings = (mappings in Universal).value
    universalMappings
      .filterNot { case (_, v) => v.startsWith("bin/") }
      .map { case (k, v) =>
        (k, v.replaceFirst("lib/", "jars/"))
      }
  },
  // There are so many changes in the default configuration
  // that it seems easier to start from scratch
  dockerCommands := Seq(
    // FIXME Reference the other project's image (not hardcode the name)
    Cmd("FROM", s"meetup-app-deps:${(ThisBuild/version).value}"),
    // /2/opt/docker/jars is the directory used by sbt-native-packager
    Cmd("COPY", s"/2/opt/docker/jars /opt/spark/jars"),
    Cmd("WORKDIR", "/opt/spark/work-dir"),
    Cmd("EXPOSE", "4040"),
    ExecCmd("ENTRYPOINT", "/opt/entrypoint.sh"),
    ExecCmd("CMD", "")
  )
)

lazy val `meetup-spark-app` = (project in file("meetup-spark-app"))
  .dependsOn(`meetup-app-deps`)
  .enablePlugins(JavaAppPackaging, DockerPlugin)
  .settings(common)

lazy val `spark-streams-demo` = (project in file("spark-streams-demo"))
  .dependsOn(`meetup-app-deps`)
  .enablePlugins(JavaAppPackaging, DockerPlugin)
  .settings(common)
