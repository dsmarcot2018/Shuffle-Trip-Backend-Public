ThisBuild / scalaVersion := "2.13.10"

ThisBuild / version := "1.0-SNAPSHOT"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(
    name := """Shuffle-Trip""",
    maintainer := "shuffletripplanner@gmail.com",
    libraryDependencies ++= Seq(
      guice,
      "com.google.inject" % "guice" % "5.1.0",
      "com.google.inject.extensions" % "guice-assistedinject" % "5.1.0",
      "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % Test,
      "com.softwaremill.sttp.client3" %% "core" % "3.8.10",
      "com.squareup.okhttp3" % "okhttp" % "4.10.0",
      "io.spray" %% "spray-json" % "1.3.6",
      "org.scalatest" %% "scalatest" % "3.2.15" % Test,
      "org.neo4j.driver" % "neo4j-java-driver" % "5.5.0",
      "com.google.apis" % "google-api-services-oauth2" % "v2-rev91-1.20.0",
      "com.google.api-client" % "google-api-client-gson" % "2.2.0"
    )
  )