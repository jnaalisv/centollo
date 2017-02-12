package centollo.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;

import javax.sql.DataSource;

@Configuration
@Import(DataSourceConfig.class)
public class SqlConfig {

    @Bean
    public LazyConnectionDataSourceProxy lazyConnectionDataSource(DataSource hikariDataSource) {
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
}
