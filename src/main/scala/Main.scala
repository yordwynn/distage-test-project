import distage.Injector
import endpoint.Endpoint

object MainDummy extends App {
  Injector()
    .produceGet[Endpoint](modules.dummyModule)
    .use(_.run())
    .unsafeRunSync()
}
