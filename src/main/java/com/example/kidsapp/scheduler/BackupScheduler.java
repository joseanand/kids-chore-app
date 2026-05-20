package com.example.kidsapp.scheduler;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class BackupScheduler {

    private final JdbcTemplate jdbcTemplate;

    public BackupScheduler(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Scheduled(fixedRate = 3600000)
    public void backupDatabase() {
        jdbcTemplate.execute("SCRIPT TO 'backup.sql'");
        System.out.println("Database backup completed.");
    }
}