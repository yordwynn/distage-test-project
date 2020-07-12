package dataStrorage

import covid19.model.CovidData
import zio.Managed

import scala.collection.mutable

final class DummyStorage extends DataStorage {
  val data: mutable.Map[String, CovidData] = mutable.Map.empty

  override def save(data: Seq[CovidData]): Unit = {
    this.data.addAll(data.map(x => (x.locationName, x)))
  }

  override def getByLocation(location: String): Option[CovidData] = {
    data.get(location)
  }
}

object DummyStorage {
  val managed: Managed[Nothing, DummyStorage] = Managed.succeed(new DummyStorage)
}