package dataStrorage
import covid19.model.CovidData

trait DataStorage {
  def save(data: Seq[CovidData]): Unit
  def count: Int
  def mostInfected: CovidData
}