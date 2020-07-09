import covid19.sources.Source
import dataSources.DummySource
import dataStrorage.{DataStorage, DummyDataStorage}
import distage.ModuleDef
import endpoint.Endpoint

package object modules {
  val dummyModule: ModuleDef = new ModuleDef {
    make[Source].from[DummySource]
    make[DataStorage].from[DummyDataStorage]
    make[Endpoint]
  }
}
