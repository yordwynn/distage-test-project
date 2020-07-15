package endpoint

import cats.effect.{ContextShift, IO}
import com.typesafe.config.ConfigFactory
import covid19.sources.{RussianSource, Source, WorldSource}
import dataSourses.MockSource
import dataStrorage.{CassandraConfig, CassandraPortConfig, CassandraResource, CassandraTransactor, CassandraTransactorResource, DataStorage, DummyStorage}
import distage.ModuleDef
import distage.config.ConfigModuleDef
import izumi.distage.config.model.AppConfig
import izumi.fundamentals.platform.integration.PortCheck
import plugins.SourceAxis
import sttp.client.{HttpURLConnectionBackend, Identity, NothingT, SttpBackend}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

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
    make[Source].tagged(SourceAxis.Mock).from[MockSource]
    make[Source].tagged(SourceAxis.Russia).from[RussianSource]
    make[Source].tagged(SourceAxis.World).from[WorldSource]

    // what if we have a chain of dependent resources? in witch order will they released?
    make[CassandraTransactor].fromHas[CassandraTransactorResource]
    make[DataStorage].fromResource[CassandraResource]
    make[PortCheck].from(new PortCheck(3.seconds))

    make[Endpoint]

    makeConfig[CassandraConfig]("cassandra.mock").tagged(SourceAxis.Mock)
    makeConfig[CassandraConfig]("cassandra.world").tagged(SourceAxis.World)
    makeConfig[CassandraPortConfig]("cassandra.mock").tagged(SourceAxis.Mock)
    makeConfig[CassandraPortConfig]("cassandra.world").tagged(SourceAxis.World)
    make[AppConfig].from(AppConfig(ConfigFactory.load("common-reference.conf")))

    addImplicit[SttpBackend[Identity, Nothing, NothingT]]
    addImplicit[ContextShift[IO]]
  }
}