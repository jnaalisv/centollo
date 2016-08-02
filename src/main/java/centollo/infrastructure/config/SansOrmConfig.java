package centollo.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;

@Configuration
@ComponentScan("centollo.infrastructure.sansorm")
@Import(DataSourceConfig.class)
public class SansOrmConfig {

    @Bean
    public TransactionAwareDataSourceProxy transactionAwareDataSource(DataSource hikariDataSource) {
        return new TransactionAwareDataSourceProxy(hikariDataSource);
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
