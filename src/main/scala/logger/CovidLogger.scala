package logger

import izumi.logstage.api.IzLogger
import izumi.logstage.api.Log.CustomContext
import izumi.logstage.api.routing.StaticLogRouter

class CovidLogger extends IzLogger(
  new StaticLogRouter(),
  CustomContext.empty
)
