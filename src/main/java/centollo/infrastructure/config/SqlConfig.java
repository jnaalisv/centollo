package centollo.infrastructure.config;

import org.springframework.context.annotation.Bean;
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
@Import(DataSourceConfig.class)
public class SqlConfig {

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
    public DataSourceInitializer dataSourceInitializer(DataSource hikariDataSource) {
        DataSourceInitializer initializer = new DataSourceInitializer();
        initializer.setDataSource(hikariDataSource);
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("create-db.sql"));
        initializer.setDatabasePopulator(populator);
        return initializer;
    }
}
