package centollo.infrastructure.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jooq.SQLDialect;
import org.jooq.impl.DataSourceConnectionProvider;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.impl.DefaultDSLContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@ComponentScan("centollo.infrastructure.jooq")
@EnableTransactionManagement
//@Import(DataSourceConfig.class)
public class JOOQConfig {

    @Bean(destroyMethod = "close")
    public DataSource hikariDataSource() {
        HikariConfig dataSourceConfig = new HikariConfig();
        dataSourceConfig.setDriverClassName("org.h2.Driver");
        dataSourceConfig.setJdbcUrl("jdbc:h2:mem:db");
        dataSourceConfig.setUsername("sa");
        dataSourceConfig.setPassword("");
        return new HikariDataSource(dataSourceConfig);
    }

//    @Bean
//    public LazyConnectionDataSourceProxy lazyConnectionDataSource(DataSource hikariDataSource) {
//        return new LazyConnectionDataSourceProxy(hikariDataSource);
//    }
//

    @Bean
    public TransactionAwareDataSourceProxy transactionAwareDataSource() {
        return new TransactionAwareDataSourceProxy(hikariDataSource());
    }

    @Bean
    public DataSourceTransactionManager transactionManager(TransactionAwareDataSourceProxy transactionAwareDataSource) {
        return new DataSourceTransactionManager(transactionAwareDataSource);
    }

    @Bean
    public DataSourceConnectionProvider connectionProvider(TransactionAwareDataSourceProxy transactionAwareDataSource) {
        return new DataSourceConnectionProvider(transactionAwareDataSource);
    }

    @Bean
    public DefaultConfiguration configuration(DataSourceConnectionProvider connectionProvider) {
        DefaultConfiguration jooqConfiguration = new DefaultConfiguration();
        jooqConfiguration.set(connectionProvider);
        jooqConfiguration.set(SQLDialect.H2);
        return jooqConfiguration;
    }

    @Bean
    public DefaultDSLContext dsl(DefaultConfiguration configuration) {
        return new DefaultDSLContext(configuration);
    }

    @Bean
    public DataSourceInitializer dataSourceInitializer(DataSource hikariDataSource) {
        DataSourceInitializer initializer = new DataSourceInitializer();
        initializer.setDataSource(hikariDataSource);
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("create-db.sql"));
        initializer.setDatabasePopulator(populator);
        return initializer;
    }
}
