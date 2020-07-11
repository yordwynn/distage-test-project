package dataStrorage

import covid19.model.CovidData
import zio.Managed

import scala.collection.mutable

final class DummyStorage extends DataStorage {
  val data: mutable.Map[String, CovidData] = mutable.Map.empty

  override def save(data: Seq[CovidData]): Unit = {
    this.data.addAll(data.map(x => (x.locationName, x)))
  }

  override def count: Int = data.size

  override def mostInfected: CovidData = data.max(Ordering.by[(String, CovidData), Int](_._2.confirmed))._2
}

object DummyStorage {
  val managed: Managed[Nothing, DummyStorage] = Managed.succeed(new DummyStorage)
}