package centollo.infrastructure.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
public class DataSourceConfig {

    @Bean(destroyMethod = "close")
    public DataSource hikariDataSource() {
        HikariConfig dataSourceConfig = new HikariConfig();
        dataSourceConfig.setDriverClassName("org.h2.Driver");
        dataSourceConfig.setJdbcUrl("jdbc:h2:mem:db");
        dataSourceConfig.setUsername("sa");
        dataSourceConfig.setPassword("");
        return new HikariDataSource(dataSourceConfig);
    }
}
