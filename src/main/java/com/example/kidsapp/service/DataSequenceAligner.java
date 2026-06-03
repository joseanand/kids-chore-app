package com.example.kidsapp.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataSequenceAligner {

    @PersistenceContext
    private EntityManager entityManager;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void alignDatabaseSequences() {
        System.out.println("🔄 Aligning H2 auto-increment sequence counters...");
        try {
            // This forces H2 to look at the maximum existing ID and restart its counter right above it
            entityManager.createNativeQuery(
                    "ALTER TABLE CHORE ALTER COLUMN ID RESTART WITH (SELECT MAX(ID) + 1 FROM CHORE)"
            ).executeUpdate();
            entityManager.createNativeQuery(
                    "ALTER TABLE chore_completion ALTER COLUMN ID RESTART WITH (SELECT MAX(ID) + 1 FROM chore_completion)"
            ).executeUpdate();
            entityManager.createNativeQuery(
                    "ALTER TABLE REWARD ALTER COLUMN ID RESTART WITH (SELECT MAX(ID) + 1 FROM REWARD)"
            ).executeUpdate();

            System.out.println("✅ CHORE sequence successfully fast-forwarded past existing records!");
        } catch (Exception e) {
            System.out.println("ℹ️ Sequence alignment skipped (table might be empty): " + e.getMessage());
        }
    }
}
