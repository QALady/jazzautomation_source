//Exported from XMl via http://logback.qos.ch/translator/asGroovy.html

statusListener(OnConsoleStatusListener)


import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.FileAppender

import static ch.qos.logback.classic.Level.DEBUG
import static ch.qos.logback.classic.Level.ERROR
import static ch.qos.logback.classic.Level.INFO

// define the USER_HOME variable setting its value to that of the "user.home" system property
def USER_HOME = System.getProperty("user.home")

appender("STDOUT", ConsoleAppender) {
  encoder(PatternLayoutEncoder) {
    pattern = "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
  }
}
appender("FILE", FileAppender) {
  file = "${USER_HOME}/jazzautomation.log"
  encoder(PatternLayoutEncoder) {
//    pattern = "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} [lineNum:%line] - %msg%n"
//    pattern = "%d{HH:mm:ss.SSS} %-5level %logger{36}:%line - %msg%n"
    pattern = " %msg%n"
  }
}
logger("org.apache", ERROR)
logger("com.jazzautomation", ERROR)
logger("com.jazzautomation.page", ERROR)
logger("com.jazzautomation.AutomationDriver", DEBUG)
logger("com.jazzautomation.SuiteProcessor", DEBUG)
logger("com.jazzautomation.cucumber", INFO)
root(ERROR, ["STDOUT", "FILE"])


