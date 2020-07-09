import sbt._

object Version {
  val pallas      = "2.1.0"
  val sttp        = "2.0.1"
  val catsEffect  = "2.1.3"
  val distage     = "0.10.16"
}

object Dependencies {
  val pallas: Seq[ModuleID] = Seq(
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
}
