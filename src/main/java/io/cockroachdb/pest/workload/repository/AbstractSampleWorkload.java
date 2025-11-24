package io.cockroachdb.pest.workload.repository;

import javax.sql.DataSource;

import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.transaction.support.TransactionTemplate;

public abstract class AbstractSampleWorkload implements Runnable {
    protected final SampleRepository sampleRepository;

    protected final JdbcTemplate jdbcTemplate;

    protected final TransactionTemplate transactionTemplate;

    protected AbstractSampleWorkload(DataSource dataSource) {
        this.sampleRepository = new JdbcSampleRepository(dataSource);
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.transactionTemplate = new TransactionTemplate(new DataSourceTransactionManager(dataSource));

        initSchema(dataSource);
    }

    private void initSchema(DataSource dataSource) {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.setCommentPrefix("--");
        populator.setIgnoreFailedDrops(true);
        populator.addScript(new ClassPathResource("db/create.sql"));

        DatabasePopulatorUtils.execute(populator, dataSource);
    }
}
