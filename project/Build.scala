import sbt._
import sbt.Keys._
import sbtassembly.Plugin._
import AssemblyKeys._

object CrossPlatformAnalytics extends Build {

  val ScalaVersion = "2.10.6"

  val SparkVersion = "1.6.1"

  val ScaldingVersion = "0.16.0"

  val HadoopVersion = "2.7.2"

  val KafkaVersion = "0.8.2.2"

  val ParquetVersion = "1.8.1"

  val commonSettings = Seq(
    scalaVersion := ScalaVersion,
    scalacOptions := Seq("-deprecation", "-language:_"),
    javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint"),
    initialize <<= (initialize) { _ =>
      if (sys.props("java.specification.version") != "1.8") {
        sys.error("Java 8 is required for this project.")
      }
    },
    resolvers ++= Seq(
      "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
      "Maven Central Repository" at "http://repo1.maven.org/maven2",
      "Concurrent Maven Repository" at "http://conjars.org/repo",
      "Local Maven Repository" at "file://" + Path.userHome.absolutePath + "/.m2/repository"
    ),
    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic" % "1.0.13",
      "org.slf4j" % "slf4j-api" % "1.7.12",
      "org.scalatest" %% "scalatest" % "2.2.6" % "test",
      "org.mockito" % "mockito-core" % "1.9.5" % "test"
    )
  )

  val privateSettings = Seq(
    publish := (),
    publishLocal := (),
    publishArtifact := false,
    publishTo := None
  )

  val publishSettings = Seq(
    publishMavenStyle := true,
    publishArtifact := true,
    publishArtifact in Test := false,
    publishArtifact in (Compile, packageDoc) := true,
    pomIncludeRepository := { _ => false }
  )

  val commonMergeStrategy: PartialFunction[String, MergeStrategy] = {
    case fp if fp.endsWith("pom.properties") => MergeStrategy.discard
    case fp if fp.endsWith("pom.xml") => MergeStrategy.discard
    case fp if fp.endsWith(".class") => MergeStrategy.first
    case fp if fp.endsWith(".html") => MergeStrategy.discard
    case PathList("logback.xml") => MergeStrategy.first
  }

  val commonAssemblySettings = assemblySettings ++ Seq(
    mergeStrategy in assembly <<= (mergeStrategy in assembly) { dedup =>
      commonMergeStrategy orElse {
        case fp => dedup(fp)
      }
    }
  )

  val commonExecutableSettings = commonSettings ++ commonAssemblySettings ++ Seq(
    jarName in assembly := s"${name.value}.jar"
  )

  lazy val root = Project(
    "root",
    file("."),
    settings = privateSettings
  ).aggregate(common, sparkCommon, sparkStreaming, sparkBatch, scalding)

  lazy val common = Project(
    "common",
    file("common"),
    settings = commonSettings ++ publishSettings
  )

  lazy val sparkCommon = Project(
    "spark-common",
    file("spark-common"),
    settings = commonSettings ++ publishSettings ++ Seq(
      libraryDependencies ++= Seq(
        "org.slf4j" % "log4j-over-slf4j" % "1.7.12",
        "org.apache.spark" %% "spark-core" % SparkVersion
          exclude("log4j", "log4j")
          exclude("org.slf4j","slf4j-log4j12")
      )
    )
  ).dependsOn(common)

  lazy val sparkStreaming = Project(
    "spark-streaming",
    file("spark-streaming"),
    settings = privateSettings ++ commonExecutableSettings ++ Seq(
      libraryDependencies ++= Seq(
        "org.apache.spark" %% "spark-streaming" % SparkVersion
          exclude("log4j", "log4j")
          exclude("org.slf4j","slf4j-log4j12"),
        "org.apache.spark" %% "spark-streaming-kafka" % SparkVersion
          exclude("log4j", "log4j")
          exclude("org.slf4j","slf4j-log4j12"),
        "org.apache.kafka" %% "kafka" % KafkaVersion
          exclude("log4j", "log4j")
          exclude("org.slf4j","slf4j-log4j12")
      ),
      mainClass in (Compile, run) := Some("analytics.spark.Tool"),
      mainClass in assembly := Some("analytics.spark.Tool")
    )
  ).dependsOn(sparkCommon)

  lazy val sparkBatch = Project(
    "spark-batch",
    file("spark-batch"),
    settings = privateSettings ++ commonExecutableSettings ++ Seq(
      libraryDependencies ++= Seq(
        "org.apache.spark" %% "spark-sql" % SparkVersion
      ),
      mainClass in (Compile, run) := Some("analytics.spark.Tool"),
      mainClass in assembly := Some("analytics.spark.Tool")
    )
  ).dependsOn(sparkCommon)

  lazy val scalding = Project(
    "scalding",
    file("scalding"),
    settings = privateSettings ++ commonExecutableSettings ++ Seq(
      libraryDependencies ++= Seq(
        "com.twitter" %% "scalding-core" % ScaldingVersion,
        "com.twitter" %% "scalding-parquet" % ScaldingVersion,
        "org.apache.hadoop" % "hadoop-client" % HadoopVersion % "provided"
      ),
      run in Compile <<= Defaults.runTask(fullClasspath in Compile, mainClass in (Compile, run), runner in (Compile, run)),
      mainClass in (Compile, run) := Some("com.twitter.scalding.Tool"),
      mainClass in assembly := Some("com.twitter.scalding.Tool")
    )
  ).dependsOn(common)
}
