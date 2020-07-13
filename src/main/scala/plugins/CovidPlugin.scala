package plugins

import cats.effect.{ContextShift, IO}
import covid19.sources.{RussianSource, Source, WorldSource}
import dataSourses.MockSource
import dataStrorage.{CassandraConfig, CassandraResource, CassandraStorage, CassandraTransactor, DataStorage}
import distage.ModuleDef
import distage.plugins.PluginDef
import izumi.distage.config.ConfigModuleDef
import sttp.client.{HttpURLConnectionBackend, Identity, NothingT, SttpBackend}

import scala.concurrent.ExecutionContext

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
      make[CassandraTransactor].fromResource[CassandraResource]
      make[DataStorage].from[CassandraStorage]
    }

    val configs: ConfigModuleDef = new ConfigModuleDef {
      makeConfig[CassandraConfig]("cassandra")
    }

    val implicits: ModuleDef = new ModuleDef {
      implicit val sttpBackend: SttpBackend[Identity, Nothing, NothingT] = HttpURLConnectionBackend()
      implicit val contextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

      addImplicit[SttpBackend[Identity, Nothing, NothingT]]
      addImplicit[ContextShift[IO]]
    }
  }
}
