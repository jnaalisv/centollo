appender("STDOUT", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%d{HH:mm:ss.SSS} [%thread] %highlight(%-5level) %logger{40}: %msg%n"
    }
}

root(WARN, ["STDOUT"])

logger("centollo", WARN)
logger("org.springframework", WARN)

logger("org.hibernate", WARN)
