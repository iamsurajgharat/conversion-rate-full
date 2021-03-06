name := """conversion-rate-service"""
organization := "com.surajgharat"

version := "1.0-SNAPSHOT"

lazy val databaseHost = sys.env.getOrElse("DB_HOST", "localhost:5432")
lazy val databaseName = sys.env.getOrElse("DB_NAME", "postgres")
lazy val databaseUrl = s"jdbc:postgresql://$databaseHost/$databaseName"
lazy val databaseUser = sys.env.getOrElse("DB_USER", "postgres")
lazy val databasePassword = sys.env.getOrElse("DB_PASSWORD", "postgres")

scalaVersion := "2.13.6"
val FlywayVersion = "8.0.4"
val zioVersion = "2.0.0-M4"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .enablePlugins(FlywayPlugin)
  .enablePlugins(AshScriptPlugin)
  .settings(
    libraryDependencies ++= Seq(
      guice,
      "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test,
      "com.github.nscala-time" %% "nscala-time" % "2.30.0",
      "dev.zio" %% "zio" % zioVersion,
      "org.mockito" %% "mockito-scala" % "1.16.46",
      jdbc,
      "com.typesafe.slick" %% "slick" % "3.3.3",
      // "org.slf4j" % "slf4j-nop" % "1.6.4",
      "com.typesafe.slick" %% "slick-hikaricp" % "3.3.3",
      "org.postgresql" % "postgresql" % "9.4-1206-jdbc42",
      "com.github.tototoshi" %% "slick-joda-mapper" % "2.4.2",
      "org.joda" % "joda-convert" % "1.7",
      "org.flywaydb" % "flyway-core" % FlywayVersion
    ),
    flywayLocations := Seq("filesystem:app/resources/db/migration"),
    flywayUrl := databaseUrl,
    flywayUser := databaseUser,
    flywayPassword := databasePassword,
    flywayBaselineOnMigrate := true
  )

import com.typesafe.sbt.packager.docker.DockerChmodType
import com.typesafe.sbt.packager.docker.DockerPermissionStrategy
dockerChmodType := DockerChmodType.UserGroupWriteExecute
dockerPermissionStrategy := DockerPermissionStrategy.CopyChown

Docker / maintainer := "mr.surajgharat2@gmail.com"
Docker / packageName := "surajgharat/conversion-rate-main-service"
Docker / version := sys.env.getOrElse("BUILD_NUMBER", "0")
Docker / daemonUserUid := None
Docker / daemonUser := "daemon"
dockerExposedPorts := Seq(9000)
dockerBaseImage := "openjdk:8-jre-alpine"
dockerUpdateLatest := true

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.surajgharat.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.surajgharat.binders._"
