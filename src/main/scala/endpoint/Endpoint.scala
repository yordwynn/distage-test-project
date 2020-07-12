package endpoint

import covid19.model.CovidData
import covid19.sources.Source
import dataStrorage.DataStorage
import zio.Task
import zio.interop.catz._

final class Endpoint(source: Source, storage: DataStorage) {
  def run: Task[Option[CovidData]] = {
    source
      .getInfected
      .map(r => storage.save(r.items))
      .map(_ => storage.getByLocation("Russia"))
  }.to[Task]
}
