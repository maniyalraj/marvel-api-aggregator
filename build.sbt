ThisBuild / scalaVersion     := "2.13.12"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

lazy val root = (project in file("."))
  .settings(
    name := "Marvel Finagle ING API",
    libraryDependencies ++= Seq(
      "com.twitter"        %% "finagle-http"            % "22.12.0",
      "io.circe"           %% "circe-core"              % "0.14.3",
      "io.circe"           %% "circe-generic"           % "0.14.3",
      "io.circe"           %% "circe-parser"            % "0.14.3",
      "com.typesafe"        % "config"                  % "1.4.3",
      "org.slf4j"           % "slf4j-api"               % "1.7.32",
      "ch.qos.logback"      % "logback-classic"         % "1.2.6",
      "com.google.inject"   % "guice"                   % "5.1.0",
      "com.github.blemale" %% "scaffeine"               % "5.1.1",
      "net.debasishg"      %% "redisclient"             % "3.42",
      "org.scalatest"      %% "scalatest"               % "3.2.16"  % Test,
      "org.mockito"        %% "mockito-scala"           % "1.17.12" % Test,
      "org.mockito"        %% "mockito-scala-scalatest" % "1.17.12" % Test
    )
  )
