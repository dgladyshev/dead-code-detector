import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.ConsoleAppender
import org.slf4j.MDC

import static ch.qos.logback.classic.Level.OFF

def appenderList = ["CONSOLE"]

def sourceType = "server"
def sourceId = "dead-code-detector"

jmxConfigurator()

logger('org.springframework', OFF)
logger('org.springframework.boot', OFF)
logger('org.springframework.web.servlet', OFF)
logger('org.springframework.security.web', OFF)
logger('org.springframework.context.support', OFF)
logger("com.dgladyshev", INFO)

appender("CONSOLE", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
    }
}

MDC.put("source_type", sourceType)
MDC.put("source_id", sourceId)
root(INFO, appenderList)