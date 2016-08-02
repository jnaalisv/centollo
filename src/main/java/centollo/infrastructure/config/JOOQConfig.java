package centollo.infrastructure.config;

import org.jooq.SQLDialect;
import org.jooq.impl.DataSourceConnectionProvider;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.impl.DefaultDSLContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.inject.Named;
import javax.sql.DataSource;

@Configuration
@ComponentScan("centollo.infrastructure.jooq")
@Import(DataSourceConfig.class)
public class JOOQConfig {

    @Bean
    public LazyConnectionDataSourceProxy lazyConnectionDataSource(@Named("hikariDataSource") DataSource hikariDataSource) {
        return new LazyConnectionDataSourceProxy(hikariDataSource);
    }

    @Bean
    public TransactionAwareDataSourceProxy transactionAwareDataSource(LazyConnectionDataSourceProxy lazyConnectionDataSource) {
        return new TransactionAwareDataSourceProxy(lazyConnectionDataSource);
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
