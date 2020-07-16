import distage.Injector
import endpoint.{Endpoint, EndpointModules}
import izumi.distage.model.definition.Activation
import izumi.distage.model.plan.Roots
import izumi.distage.model.reflection.DIKey
import izumi.logstage.api.IzLogger
import plugins.SourceAxis
import zio.Task

object Main extends App {
  def runWith(activation: Activation): Unit = {
    val res = Injector(activation)
      .produceF[Task](EndpointModules.endpointDummyStorage, Roots(DIKey[Endpoint]))
      .use(_.get[Endpoint].run)

    println(zio.Runtime.default.unsafeRun(res))
  }

  runWith(Activation(SourceAxis -> SourceAxis.World))
}

object MainCassandra extends App {
  def runWith(activation: Activation): Unit = {
    val res = Injector(activation)
      .produceRunF(EndpointModules.endpointCassandraStorage) {
        (_: Endpoint).run
      }

    println(zio.Runtime.default.unsafeRun(res))

    val logger = IzLogger()
  }

  runWith(Activation(SourceAxis -> SourceAxis.World))
}