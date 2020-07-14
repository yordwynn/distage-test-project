package covid.docker

import dataStrorage.CassandraPortConfig
import distage.ModuleDef
import izumi.distage.docker.modules.DockerSupportModule
import zio.Task

object CassandraDockerModule extends ModuleDef {
  include(DockerSupportModule[Task])

  make[CassandraContainerDef.Container].fromResource(CassandraContainerDef.make[Task])

  make[CassandraPortConfig].from {
    docker: CassandraContainerDef.Container =>
      val knownAddress = docker.availablePorts.availablePorts(CassandraContainerDef.primaryPort).head
      CassandraPortConfig(knownAddress.hostV4, knownAddress.port)
  }
}
