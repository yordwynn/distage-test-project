import distage.Injector
import endpoint.Endpoint
import izumi.distage.model.definition.Activation
import izumi.distage.model.plan.Roots
import izumi.distage.model.reflection.DIKey
import modules.SourceAxis
import zio.Task

object Main extends App {
  def runWith(activation: Activation): Unit = {
    val res = Injector(activation)
      .produceF[Task](modules.endpointDummyStorage, Roots(DIKey[Endpoint]))
      .use(_.get[Endpoint].run)

    println(zio.Runtime.default.unsafeRun(res))
  }

  runWith(Activation(SourceAxis -> SourceAxis.World))
}

object MainCassandra extends App {
  def runWith(activation: Activation): Unit = {
    val res = Injector(activation)
      .produceGet[Endpoint](modules.endpointCassandraStorage)
      .use(x => zio.Runtime.default.unsafeRun(x.run)) //can we run this out of the use?

    println(res)
  }

  runWith(Activation(SourceAxis -> SourceAxis.World))
}