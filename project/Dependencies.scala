import sbt._

object Version {
  val pallas          = "2.1.0"
  val sttp            = "2.0.1"
  val catsEffect      = "2.1.3"
  val distage         = "0.10.16"
  val zio             = "1.0.0-RC21"
  val zioInteropCats  = "2.1.3.0-RC16"
  val slf4j           = "1.7.5"
  val akka            = "2.6.7"
  val cassandra       = "3.9.0"
}

object Dependencies {
  val pallas: Seq[ModuleID] = Seq(
    // https://github.com/yordwynn/pallas
    "com.github.yordwynn" % "pallas" % Version.pallas
  )

  val sttp: Seq[ModuleID] = Seq(
    "com.softwaremill.sttp.client" %% "core" % Version.sttp,
    "com.softwaremill.sttp.client" %% "async-http-client-backend-future" % Version.sttp
  )

  val catsEffect: Seq[ModuleID] = Seq(
    "org.typelevel" %% "cats-effect" % Version.catsEffect
  )

  val distage: Seq[ModuleID] = Seq(
    "io.7mind.izumi" %% "distage-core" % Version.distage
  )

  val zio: Seq[ModuleID] = Seq(
    "dev.zio" %% "zio" % Version.zio
  )

  val zioInteropCats: Seq[ModuleID] = Seq(
    "dev.zio"  %% "zio-interop-cats"  % Version.zioInteropCats
  )

  val slf4j: Seq[ModuleID] = Seq(
    "org.slf4j" % "slf4j-api" % Version.slf4j
  )

  val akka: Seq[ModuleID] = Seq(
    "com.typesafe.akka" %% "akka-actor" % Version.akka,
    "com.typesafe.akka" %% "akka-slf4j" % Version.akka
  )

  val cassandra: Seq[ModuleID] = Seq(
    "com.datastax.cassandra" % "cassandra-driver-core" % Version.cassandra
  )
}
