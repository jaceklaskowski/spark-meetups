ThisBuild / organization := "meetup"
ThisBuild / version      := "0.1.0"
ThisBuild / scalaVersion := "2.12.13"

name := "spark-docker-example"

// It does not really matter for the purpose of the demo
// that the two versions of Spark differ
// The demos don't use anything 3.1.1-specific
val sparkVer = "3.0.1"
val sparkImageVer = "3.1.1-rc1"
// https://search.maven.org/artifact/com.google.cloud.bigdataoss/gcs-connector/hadoop3-2.2.0/jar
val gcsConnectorVer = "hadoop3-2.2.0"

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
    `meetup-spark-app`,
    `spark-streams-demo`,
    `spark-streams-google-storage-demo`)

// NOTE:
// s/spark-streams-google-storage-demo/any of the above projects

// Use the following to switch to a project in sbt shell
// All commands then are going to be applied to this project only
//
// project spark-streams-google-storage-demo
//

// 0. show docker:dockerCommands
// 1. clean; docker:stage
//   * tree spark-streams-google-storage-demo/target/docker/stage/
// 2. docker:publishLocal
// 3. docker run --entrypoint bash -it spark-streams-google-storage-demo:0.1.0

import com.typesafe.sbt.packager.docker._
val common = Seq(
  libraryDependencies += "org.apache.spark" %% "spark-sql" % sparkVer % Provided,
  libraryDependencies += "org.apache.spark" %% "spark-kubernetes" % sparkVer % Provided,
  // FROM
  dockerBaseImage := s"spark:v$sparkImageVer",
  // WORKDIR
  defaultLinuxInstallLocation in Docker := "/opt/spark",
  // USER
  daemonUser in Docker := "root",
  daemonUserUid in Docker := None,
  daemonGroupGid in Docker := None,

  // Layer directories with libs and jars
  // show spark-base-image/docker:mappings
  // show spark-base-image/docker:dockerLayerMappings
  // The following is just an example and not really needed
  // dockerGroupLayers in Docker  := { case _ => 1 },
  // There are two directories sbt-native-packager uses
  // 1/ for dependencies
  // 2/ for packageBin

  // COPY
  mappings in Universal := {
    // take whatever the parent project does with the mappings
    val universalMappings = (mappings in Universal).value
    // exclude this and all parent projects' build artifacts
    // they are part of their respective images anyway
    // No need to include Scala dependency as it's part of the Spark image
    val exclusions = Set("/org/scala-lang/")
    universalMappings
      // No use of binaries in Spark.
      // Skip them (as they're already in the image)
      .filterNot { case (_, v) => v.startsWith("bin/") }
      .map { case (k, v) =>
        // Copy project jars to jars folder (following Spark's convention)
        (k, v.replaceFirst("lib/", "jars/"))
      }
      .filterNot { case (projectFile, _) =>
        exclusions.exists(projectFile.toString.contains)
      }
  },
  // ENTRYPOINT
  dockerEntrypoint := Seq("/opt/entrypoint.sh"),
  // EXPOSE
  dockerExposedPorts := Seq(4040),
  {
    // FIXME Is the following needed / used?
    import com.typesafe.sbt.packager.docker.DockerPermissionStrategy
    dockerPermissionStrategy := DockerPermissionStrategy.None
  },
//  dockerCommands := {
//    val cmds = dockerCommands.value
//    import com.typesafe.sbt.packager.docker._
//    cmds.flatMap {
//      // case Cmd(name, _*) if name == "WORKDIR" => Some(Cmd(name, "/opt/spark/work-dir"))
//      case ExecCmd(cmd, _*) if cmd == "CMD" => /* skip it */ None
//      case c => Some(c)
//    }
//  },
  // For docker:publish
  dockerRepository in Docker := Some("eu.gcr.io/spark-on-kubernetes-2021")
)

lazy val `meetup-spark-app` = (project in file("meetup-spark-app"))
  .enablePlugins(JavaAppPackaging, DockerPlugin)
  .settings(common)


/**

 project spark-streams-demo

 show docker:mappings
 show docker:dockerCommands

 clean; docker:stage
 tree spark-streams-demo/target/docker/stage/

 set dockerRepository in Docker := None
 clean; docker:publishLocal
 docker run --entrypoint bash -it spark-streams-demo:0.1.0

 clean; docker:publishLocal
 docker images 'eu.gcr.io/spark-on-kubernetes-2021/*'

 */
 */
lazy val `spark-streams-demo` = (project in file("spark-streams-demo"))
  .enablePlugins(JavaAppPackaging, DockerPlugin)
  .settings(common)

/**

 project spark-streams-google-storage-demo

 show docker:dockerCommands
 show docker:mappings

 clean; docker:stage
 tree spark-streams-google-storage-demo/target/docker/stage/

 clean; docker:publishLocal
 docker images 'eu.gcr.io/spark-on-kubernetes-2021/*'

 set dockerRepository in Docker := None
 clean; docker:publishLocal
 docker run --entrypoint bash -it spark-streams-google-storage-demo:0.1.0

 */
 */
lazy val `spark-streams-google-storage-demo` = (project in file("spark-streams-google-storage-demo"))
  .dependsOn(`spark-streams-demo`)
  .enablePlugins(JavaAppPackaging, DockerPlugin)
  .settings(common)
  .settings(
    libraryDependencies += "com.google.cloud.bigdataoss" % "gcs-connector" % gcsConnectorVer classifier "shaded" intransitive(),
    // FROM
    dockerBaseImage := (dockerAlias in `spark-streams-demo`).value.toString,
    // Leave the one dependency only
    mappings in Universal := {
      // take whatever the parent project does with the mappings
      val universalMappings = (mappings in Universal).value
      // exclude this and all parent projects' build artifacts
      // they are part of their respective images anyway
      val exclusions = Set("/target/scala-2.12/", "/org/scala-lang/")
      universalMappings
        .filterNot { case (projectFile, _) =>
          exclusions.exists(projectFile.toString.contains)
        }
    }
  )
