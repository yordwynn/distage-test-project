package dataStrorage
import covid19.model.CovidData
import zio.UIO

trait DataStorage {
  def save(data: Seq[CovidData]): UIO[Unit]
  def getByLocation(location: String): UIO[Option[CovidData]]
  def create: UIO[Unit]
}