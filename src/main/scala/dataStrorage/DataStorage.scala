package dataStrorage
import covid19.model.CovidData
import zio.IO

trait DataStorage {
  def save(data: Seq[CovidData]): IO[Throwable, Unit]
  def selectByLocation(location: String): IO[Throwable, Option[CovidData]]
  def selectAll: IO[Throwable, List[CovidData]]
}