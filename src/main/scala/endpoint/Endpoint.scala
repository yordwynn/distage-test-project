package endpoint

import cats.effect.IO
import covid19.model.CovidData
import covid19.sources.Source
import dataStrorage.DataStorage

final class Endpoint(source: Source, storage: DataStorage) {
  def run(): IO[CovidData] = {
    source
      .getInfected
      .map(r => storage.save(r.items))
      .map(_ => storage.mostInfected)
  }
}
