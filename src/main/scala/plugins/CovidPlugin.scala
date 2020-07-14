package plugins

import cats.effect.{ContextShift, IO}
import covid19.sources.{RussianSource, Source, WorldSource}
import dataSourses.MockSource
import dataStrorage.{CassandraConfig, CassandraPortConfig, CassandraResource, CassandraStorage, CassandraTransactor, CassandraTransactorResource, DataStorage}
import distage.ModuleDef
import distage.plugins.PluginDef
import izumi.distage.config.ConfigModuleDef
import izumi.fundamentals.platform.integration.PortCheck
import sttp.client.{HttpURLConnectionBackend, Identity, NothingT, SttpBackend}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object CovidPlugin extends PluginDef {
  include(modules.source)
  include(modules.storage)
  include(modules.configs)
  include(modules.implicits)

  object modules {
    val source: ModuleDef = new ModuleDef {
      make[Source].tagged(SourceAxis.Mock).from[MockSource]
      make[Source].tagged(SourceAxis.Russia).from[RussianSource]
      make[Source].tagged(SourceAxis.World).from[WorldSource]
    }

    val storage: ModuleDef = new ModuleDef {
      make[CassandraTransactor].fromResource[CassandraTransactorResource]
      make[DataStorage].fromResource[CassandraResource]
      make[PortCheck].from(new PortCheck(100.seconds))
    }

    val configs: ConfigModuleDef = new ConfigModuleDef {
      makeConfig[CassandraConfig]("cassandra.mock").tagged(SourceAxis.Mock)
      makeConfig[CassandraConfig]("cassandra.world").tagged(SourceAxis.World)
      makeConfig[CassandraPortConfig]("cassandra.mock").tagged(SourceAxis.Mock)
      makeConfig[CassandraPortConfig]("cassandra.world").tagged(SourceAxis.World)
    }

    val implicits: ModuleDef = new ModuleDef {
      implicit val sttpBackend: SttpBackend[Identity, Nothing, NothingT] = HttpURLConnectionBackend()
      implicit val contextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

      addImplicit[SttpBackend[Identity, Nothing, NothingT]]
      addImplicit[ContextShift[IO]]
    }
  }
}
