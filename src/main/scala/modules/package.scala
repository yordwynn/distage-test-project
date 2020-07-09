import cats.effect.{ContextShift, IO}
import covid19.sources.{RussianSource, Source, WorldSource}
import dataSources.MockSource
import dataStrorage.{DataStorage, DummyDataStorage}
import distage.ModuleDef
import endpoint.Endpoint
import sttp.client.{HttpURLConnectionBackend, Identity, NothingT, SttpBackend}

import scala.concurrent.ExecutionContext

package object modules {
  implicit val sttpBackend: SttpBackend[Identity, Nothing, NothingT] = HttpURLConnectionBackend()
  implicit val contextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  val endpointModule: ModuleDef = new ModuleDef {
    make[Source].tagged(SourceAxis.Mock).from[MockSource]
    make[Source].tagged(SourceAxis.Russia).from[RussianSource]
    make[Source].tagged(SourceAxis.World).from[WorldSource]
    addImplicit[SttpBackend[Identity, Nothing, NothingT]]
    addImplicit[ContextShift[IO]]

    make[DataStorage].from[DummyDataStorage]
    make[Endpoint]
  }
}
