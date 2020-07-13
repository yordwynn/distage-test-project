package covid

import endpoint.Endpoint
import izumi.distage.model.reflection.DIKey
import izumi.distage.plugins.PluginConfig
import izumi.distage.testkit.TestConfig
import izumi.distage.testkit.scalatest.{AssertIO, DistageBIOEnvSpecScalatest}
import zio.ZIO

abstract class CovidTest extends DistageBIOEnvSpecScalatest[ZIO] with AssertIO {
  override def config: TestConfig = TestConfig(
    pluginConfig = PluginConfig.cached(packagesEnabled = Seq("modules")),
    memoizationRoots = Set(
      DIKey[Endpoint]
    ),
    configBaseName = "covid-test"
  )
}
