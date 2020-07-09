package dataStrorage

import covid19.model.CovidData

import scala.collection.mutable

sealed trait DataStorage {
  def save(data: Seq[CovidData]): Unit
  def count: Int
  def mostInfected: CovidData
}

final class DummyDataStorage extends DataStorage {
  val data: mutable.Map[String, CovidData] = mutable.Map.empty

  override def save(data: Seq[CovidData]): Unit = {
    this.data.addAll(data.map(x => (x.locationName, x)))
  }

  override def count: Int = data.size

  override def mostInfected: CovidData = data.max(Ordering.by[(String, CovidData), Int](_._2.confirmed))._2
}