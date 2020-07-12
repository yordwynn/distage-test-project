package dataStrorage
import covid19.model.CovidData

trait DataStorage {
  def save(data: Seq[CovidData]): Unit
  def getByLocation(location: String): Option[CovidData]
}