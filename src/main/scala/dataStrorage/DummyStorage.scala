package dataStrorage

import covid19.model.CovidData
import zio.{IO, Managed, UIO}

import scala.collection.mutable

final class DummyStorage extends DataStorage {
  val data: mutable.Map[String, CovidData] = mutable.Map.empty

  override def save(data: Seq[CovidData]): IO[Throwable, Unit] = {
    IO.effectTotal(this.data.addAll(data.map(x => (x.locationName, x))))
  }

  override def selectByLocation(location: String): IO[Throwable, Option[CovidData]] = {
    IO.effectTotal(data.get(location))
  }

  override def selectAll: IO[Throwable, List[CovidData]] = IO.succeed(data.toList.map(_._2))
}

object DummyStorage {
  val managed: Managed[Nothing, DataStorage] = Managed.succeed(new DummyStorage)
}