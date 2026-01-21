package io.cockroachdb.pest.cluster.local;

import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import io.cockroachdb.pest.config.ClosableDataSource;
import io.cockroachdb.pest.model.Cluster;

@Repository
public class MetaDataRepository {
    @Autowired
    private Function<DataSourceProperties, ClosableDataSource> dataSourceFactory;

    public String queryClusterVersion(Cluster cluster) {
        try (ClosableDataSource dataSource
                     = dataSourceFactory.apply(cluster.getDataSourceProperties())) {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            return jdbcTemplate.queryForObject("select version()", String.class);
        }
    }
}
