package dataStrorage
import covid19.model.CovidData
import zio.UIO

trait DataStorage {
  def save(data: Seq[CovidData]): UIO[Unit]
  def selectByLocation(location: String): UIO[Option[CovidData]]
  def create: UIO[Unit]
  def selectAll: UIO[List[CovidData]]
}