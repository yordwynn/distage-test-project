package dataStrorage

import covid19.model.CovidData

sealed trait DataStorage {
  def save(data: Seq[CovidData]): Unit
}

final class DummyDataStorage extends DataStorage {
  override def save(data: Seq[CovidData]): Unit = println(data)
}
