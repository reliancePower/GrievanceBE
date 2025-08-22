package com.reliance.grievance.service.impl;

import com.reliance.grievance.dto.HrDirectoryInfo;
import com.reliance.grievance.service.UserDirectoryService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@ConditionalOnBean(name = "hrJdbc")
public class UserDirectoryServiceImpl implements UserDirectoryService {

    private final JdbcTemplate hrJdbc;

    public UserDirectoryServiceImpl(@Qualifier("hrJdbc") JdbcTemplate hrJdbc) {
        this.hrJdbc = hrJdbc;
    }

    @Override
    public Optional<String> findMobileByEmail(String email) {
        if (email == null || email.isBlank()) return Optional.empty();

        String db = hrJdbc.queryForObject("SELECT DB_NAME()", String.class);
        System.out.println("HR connection DB = " + db);


        final String sql = """
                SELECT TOP 1 LTRIM(RTRIM(MobileNo)) AS mobile
                FROM dbo.Employee WITH (NOLOCK)
                WHERE LOWER(LTRIM(RTRIM(Email))) = LOWER(LTRIM(RTRIM(?)))
                """;


        List<String> list = hrJdbc.query(sql, ps -> ps.setString(1, email),
                (rs, i) -> rs.getString("mobile"));  // <- read the alias "mobile"
        return list.stream().filter(Objects::nonNull).findFirst();
    }

    @Override
    public Optional<String> findPrNoByEmail(String email) {
        if (email == null || email.isBlank()) return Optional.empty();

        // Just for debug, you can keep/remove this
        String db = hrJdbc.queryForObject("SELECT DB_NAME()", String.class);
        System.out.println("HR connection DB = " + db);

        final String sql = """
            SELECT TOP 1 LTRIM(RTRIM(PRNo)) AS prNo
            FROM dbo.Employee WITH (NOLOCK)
            WHERE LOWER(LTRIM(RTRIM(Email))) = LOWER(LTRIM(RTRIM(?)))
            """;

        List<String> list = hrJdbc.query(
                sql,
                ps -> ps.setString(1, email),
                (rs, i) -> rs.getString("prNo")   // <-- read using alias
        );

        return list.stream().filter(Objects::nonNull).findFirst();
    }

    @Override
    public boolean isAllowedUser(String email) {
        if (email == null || email.isBlank()) return false;

        String sql = """
        SELECT COUNT(1)
        FROM dbo.Employee WITH (NOLOCK)
        WHERE LOWER(LTRIM(RTRIM(Email))) = LOWER(LTRIM(RTRIM(?)))
          AND (
              Company NOT IN ('RHRS', 'NEPL')
              OR dept <> 'Non R Power Employees Group'
          )
        """;

        Integer count = hrJdbc.queryForObject(sql, Integer.class, email);
        return count != null && count > 0;
    }

    @Override
    public Optional<HrDirectoryInfo> findHrInfoByEmail(String email) {
        if (email == null || email.isBlank()) return Optional.empty();

        // (Optional) sanity: confirm you are on the right DB
        // String db = hrJdbc.queryForObject("SELECT DB_NAME()", String.class);
        // System.out.println("HR connection DB = " + db);

        final String sql = """
            SELECT TOP 1
                LTRIM(RTRIM(MobileNo))   AS mobile,
                LTRIM(RTRIM(PRNo))       AS prNo,
                LTRIM(RTRIM(Location))   AS location,
                LTRIM(RTRIM(Dept))       AS dept,
                CASE
                  WHEN Company NOT IN ('RHRS','NEPL')
                       OR Dept <> 'Non R Power Employees Group'
                  THEN 1 ELSE 0
                END AS allowed
            FROM dbo.Employee WITH (NOLOCK)
            WHERE LOWER(LTRIM(RTRIM(Email))) = LOWER(LTRIM(RTRIM(?)))
            """;

        List<HrDirectoryInfo> rows = hrJdbc.query(
                sql,
                ps -> ps.setString(1, email),
                (rs, i) -> new HrDirectoryInfo(
                        rs.getString("mobile"),
                        rs.getString("prNo"),
                        rs.getString("location"),
                        rs.getString("dept"),
                        rs.getInt("allowed") == 1
                )
        );

        return rows.stream().filter(Objects::nonNull).findFirst();
    }


}



