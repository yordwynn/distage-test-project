package endpoint

import covid19.model.CovidData
import covid19.sources.Source
import dataStrorage.DataStorage
import zio.Task
import zio.interop.catz._

final class Endpoint(source: Source, storage: DataStorage) {
  def run: Task[List[CovidData]] = {
    source
      .getInfected.to[Task]
      .flatMap(r => storage.save(r.items))
      .flatMap(_ => storage.selectAll)
  }
}
