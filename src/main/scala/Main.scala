import distage.Injector
import endpoint.Endpoint
import izumi.distage.model.definition.Activation
import modules.SourceAxis

object Main extends App {
  def runWith(activation: Activation): Unit = {
    val res = Injector(activation)
      .produceGet[Endpoint](modules.endpointModule)
      .use(_.run())
      .unsafeRunSync()

    println(res)
  }

  runWith(Activation(SourceAxis -> SourceAxis.World))
}
