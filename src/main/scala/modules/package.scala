import cats.effect.{ContextShift, IO}
import com.typesafe.config.ConfigFactory
import covid19.sources.{RussianSource, Source, WorldSource}
import dataSourses.MockSource
import dataStrorage.{CassandraConfig, CassandraResource, CassandraStorage, CassandraTransactor, DataStorage, DummyStorage}
import distage.ModuleDef
import distage.config.ConfigModuleDef
import endpoint.Endpoint
import izumi.distage.config.model.AppConfig
import sttp.client.{HttpURLConnectionBackend, Identity, NothingT, SttpBackend}

import scala.concurrent.ExecutionContext

package object modules {
  implicit val sttpBackend: SttpBackend[Identity, Nothing, NothingT] = HttpURLConnectionBackend()
  implicit val contextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  val endpointDummyStorage: ModuleDef = new ModuleDef {
    make[Source].tagged(SourceAxis.Mock).from[MockSource]
    make[Source].tagged(SourceAxis.Russia).from[RussianSource]
    make[Source].tagged(SourceAxis.World).from[WorldSource]
    make[DataStorage].fromResource(DummyStorage.managed)
    make[Endpoint]

    addImplicit[SttpBackend[Identity, Nothing, NothingT]]
    addImplicit[ContextShift[IO]]
  }

  val endpointCassandraStorage: ConfigModuleDef = new ConfigModuleDef {
    make[Source].tagged(SourceAxis.Mock).from[MockSource]
    make[Source].tagged(SourceAxis.Russia).from[RussianSource]
    make[Source].tagged(SourceAxis.World).from[WorldSource]

    // what if we have a chain of dependent resources? in witch order will they released?
    make[CassandraTransactor].fromResource[CassandraResource]
    make[DataStorage].from[CassandraStorage]

    make[Endpoint]

    makeConfig[CassandraConfig]("cassandra")
    make[AppConfig].from(AppConfig(ConfigFactory.load("cassandra.conf")))

    addImplicit[SttpBackend[Identity, Nothing, NothingT]]
    addImplicit[ContextShift[IO]]
  }
}
