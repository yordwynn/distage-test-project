package dataStrorage

import covid19.model.CovidData
import zio.{IO, Managed, UIO}

import scala.collection.mutable

final class DummyStorage extends DataStorage {
  val data: mutable.Map[String, CovidData] = mutable.Map.empty

  override def save(data: Seq[CovidData]): UIO[Unit] = {
    IO.effectTotal(this.data.addAll(data.map(x => (x.locationName, x))))
  }

  override def getByLocation(location: String): UIO[Option[CovidData]] = {
    IO.effectTotal(data.get(location))
  }

  override def create: UIO[Unit] = IO.succeed(())
}

object DummyStorage {
  val managed: Managed[Nothing, DataStorage] = Managed.succeed(new DummyStorage)
}