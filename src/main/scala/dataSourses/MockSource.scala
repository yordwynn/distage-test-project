package dataSourses

import cats.effect.IO
import covid19.model.{CovidData, Response}
import covid19.sources.Source


class MockSource extends Source {
  val data: IO[Response] = IO.pure(new Response(List(
    CovidData("reg1", Option("reg1-iso"), 1000, 500, 333),
    CovidData("reg2", Option("reg1-iso"), 1500, 800, 100),
    CovidData("reg3", Option("reg1-iso"), 1300, 400, 200),
  )))

  override def baseUrl: String = "http://localhost:8888/"

  override def getInfected: IO[Response] = data

  override def getInfectedByLocation(isoCode: String): IO[CovidData] = data
    .map(rp =>
      rp
        .items
        .find(_.isoCode.contains(isoCode))
        .fold(new CovidData("", Some(isoCode), 0, 0, 0))(x => x)
    )
}
