package covid

import covid19.sources.Source
import dataStrorage.{CassandraStorage, DataStorage}
import izumi.distage.model.definition.Activation
import izumi.distage.model.reflection.DIKey
import izumi.distage.plugins.PluginConfig
import izumi.distage.testkit.TestConfig
import izumi.distage.testkit.scalatest.{AssertIO, DistageBIOEnvSpecScalatest}
import plugins.SourceAxis
import zio.{Task, ZIO}
import zio.interop.catz._

abstract class CovidTest extends DistageBIOEnvSpecScalatest[ZIO] with AssertIO {
  override def config: TestConfig = TestConfig(
    pluginConfig = PluginConfig.cached(packagesEnabled = Seq("plugins")),
    memoizationRoots = Set(
      DIKey[CassandraStorage]
    ),
    configBaseName = "covid-test"
  )
}

trait MockTest extends CovidTest {
  override final def config: TestConfig = super.config.copy(
    activation = Activation(SourceAxis -> SourceAxis.Mock)
  )
}

trait WorldTest extends CovidTest {
  override final def config: TestConfig = super.config.copy(
    activation = Activation(SourceAxis -> SourceAxis.World)
  )
}

abstract class SaveCovidDataTest extends CovidTest {
  "Covid" should {
    "get and save" in {
      (source: Source, storage: DataStorage) => {
        for {
          _ <- storage.create //move to container definition
          data <- source.getInfected.to[Task]
          _ <- storage.save(data.items)
          all <- storage.selectAll
          count = all.length
          _ <- assertIO(count > 0)
        } yield ()
      }
    }

    "most infected" in {
      (source: Source, storage: DataStorage) => {
        for {
          _ <- storage.create
          data <- source.getInfected.to[Task]
          _ <- storage.save(data.items)
          all <- storage.selectAll
          maxFromApi = data.items.maxBy(_.confirmed)
          maxFromDb = all.maxBy(_.confirmed)
          _ <- assertIO(maxFromApi == maxFromDb)
        } yield ()
      }
    }
  }
}

final class SaveCovidDataTestWorld extends SaveCovidDataTest with WorldTest
