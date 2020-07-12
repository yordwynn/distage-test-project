package dataStrorage
import covid19.model.CovidData

class CassandraStorage extends DataStorage {
  override def save(data: Seq[CovidData]): Unit = ???

  override def getByLocation(location: String): Option[CovidData] = ???
}
