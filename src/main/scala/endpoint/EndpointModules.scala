package endpoint

import cats.effect.{ContextShift, IO}
import com.typesafe.config.ConfigFactory
import covid19.sources.{RussianSource, Source, WorldSource}
import dataSourses.MockSource
import dataStrorage._
import distage.ModuleDef
import distage.config.ConfigModuleDef
import izumi.distage.config.model.AppConfig
import izumi.logstage.api.logger.AbstractLogger
import logger.CovidLogger
import plugins.{CovidPlugin, SourceAxis, ZIOPlugin}
import sttp.client.{HttpURLConnectionBackend, Identity, NothingT, SttpBackend}

import scala.concurrent.ExecutionContext

object EndpointModules {
  implicit val sttpBackend: SttpBackend[Identity, Nothing, NothingT] = HttpURLConnectionBackend()
  implicit val contextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  def endpointDummyStorage: ModuleDef = new ModuleDef {
    make[Source].tagged(SourceAxis.Mock).from[MockSource]
    make[Source].tagged(SourceAxis.Russia).from[RussianSource]
    make[Source].tagged(SourceAxis.World).from[WorldSource]
    make[DataStorage].fromResource(DummyStorage.managed)
    make[Endpoint]

    addImplicit[SttpBackend[Identity, Nothing, NothingT]]
    addImplicit[ContextShift[IO]]
  }

  def endpointCassandraStorage: ConfigModuleDef = new ConfigModuleDef {
    include(ZIOPlugin)
    include(CovidPlugin)

    make[AbstractLogger].from[CovidLogger]

    make[Endpoint]

    make[AppConfig].from(AppConfig(ConfigFactory.load("common-reference.conf")))
  }
}