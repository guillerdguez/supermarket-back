package com.supermarket.supermarket.service.demo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Connection;

@Service
@Profile("demo")
@RequiredArgsConstructor
@Slf4j
public class DemoResetService {
 
    private static final String[] TABLES_CHILD_TO_PARENT = {
            "payments", "sale_detail", "sale", "stock_transfers",
            "branch_inventory", "cash_registers", "notifications",
            "audit_logs", "product", "users", "branch"
    };

    private final JdbcTemplate jdbcTemplate;

 
    @Scheduled(cron = "${demo.reset.cron:0 0 */4 * * *}")
    @Transactional
    public void resetDemoData() {
        log.info("Starting scheduled demo data reset");
        truncateAll();
        reseedFromDataSql();
        log.info("Demo data reset completed successfully");
    }

    private void truncateAll() {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
        for (String table : TABLES_CHILD_TO_PARENT) {
            jdbcTemplate.execute("TRUNCATE TABLE " + table);
        }
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
    }

    private void reseedFromDataSql() {
        ClassPathResource seed = new ClassPathResource("data.sql");
        jdbcTemplate.execute((ConnectionCallback<Void>) (Connection con) -> {
            ScriptUtils.executeSqlScript(con, seed);
            return null;
        });
    }
}
