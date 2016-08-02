appender("STDOUT", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%d{HH:mm:ss.SSS} [%thread] %highlight(%-5level) %logger{40}: %msg%n"
    }
}

root(INFO, ["STDOUT"])

logger("centollo", INFO)
logger("org.springframework", INFO)
logger("org.springframework.jdbc.datasource.DataSourceTransactionManager", DEBUG)

logger("org.hibernate", INFO)
logger("org.springframework.orm.hibernate5.HibernateTransactionManager", DEBUG)

logger("org.hibernate.tool.hbm2ddl.SchemaExport", INFO)
logger("org.hibernate.SQL", INFO)
