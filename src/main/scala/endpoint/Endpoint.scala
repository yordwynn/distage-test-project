package endpoint

import cats.effect.IO
import covid19.model.CovidData
import covid19.sources.Source
import dataStrorage.DataStorage
import zio.Task
import zio.interop.catz._

final class Endpoint(source: Source, storage: DataStorage) {
  def run: Task[CovidData] = {
    //zio.interop.catz.core.
    source
      .getInfected
      .map(r => storage.save(r.items))
      .map(_ => storage.mostInfected)
  }.to[Task]
}
