package com.reliance.grievance.service.impl;

import com.reliance.grievance.service.ExternalIdService;
import org.springframework.dao.DeadlockLoserDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class ExternalIdServiceImpl implements ExternalIdService {

    private final JdbcTemplate jdbc;

    public ExternalIdServiceImpl(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final DateTimeFormatter YM = DateTimeFormatter.ofPattern("yyyyMM");

    /**
     * Generates an ID like 2025/08/000123.
     * Uses a single-row upsert + UPDLOCK/HOLDLOCK to be atomic under concurrency.
     */
    @Override
    @Transactional
    public String nextExternalId() {
        String ym = LocalDate.now().format(YM);                 // "202508"
        int counter = tryNext(ym, 3);                           // retry a few times on deadlocks
        String yyyy = ym.substring(0, 4);
        String mm   = ym.substring(4, 6);
        return String.format("%s/%s/%06d", yyyy, mm, counter);
    }

    private int tryNext(String ym, int retries) {
        for (int i = 0; i < retries; i++) {
            try {
                // SQL Server–friendly atomic upsert:
                // 1) Try UPDATE with locks to increment and return the new value.
                // 2) If no row, INSERT with 1 and return 1.
                Integer val = jdbc.query(
                        """
                        SET NOCOUNT ON;
    
                        DECLARE @val INT;
    
                        UPDATE dbo.grievance_seq WITH (UPDLOCK, HOLDLOCK)
                           SET next_val = next_val + 1
                         OUTPUT inserted.next_val
                         WHERE year_month = ?;
    
                        IF @@ROWCOUNT = 0
                        BEGIN
                          BEGIN TRY
                            INSERT INTO dbo.grievance_seq (year_month, next_val) VALUES (?, 1);
                            SET @val = 1;
                          END TRY
                          BEGIN CATCH
                            -- If another thread inserted concurrently, increment it now
                            UPDATE dbo.grievance_seq WITH (UPDLOCK, HOLDLOCK)
                               SET next_val = next_val + 1
                             OUTPUT inserted.next_val
                             WHERE year_month = ?;
                          END CATCH
                        END
                        """,
                        ps -> {
                            ps.setString(1, ym);
                            ps.setString(2, ym);
                            ps.setString(3, ym);
                        },
                        rs -> rs.next() ? rs.getInt(1) : null
                );

                if (val == null) {
                    // If the second UPDATE (inside CATCH) produced the row, read it explicitly:
                    val = jdbc.queryForObject(
                            "SELECT next_val FROM dbo.grievance_seq WITH (HOLDLOCK, UPDLOCK) WHERE year_month = ?",
                            Integer.class, ym
                    );
                }
                return val;
            } catch (DeadlockLoserDataAccessException ex) {
                // brief spin then retry
                try { Thread.sleep(10); } catch (InterruptedException ignored) {}
            }
        }
        // If we truly fail, bubble up; Spring will roll back the transaction
        throw new IllegalStateException("Failed to generate external id after retries");
    }
}

