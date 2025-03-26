package com.b02.peep_it.common;

import com.b02.peep_it.domain.State;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Component
public class StateInitializer {

    private final JdbcTemplate jdbcTemplate;
    @PersistenceContext
    private EntityManager em;


    @Transactional
    @EventListener(ApplicationReadyEvent.class)
    public void insertAllStates() throws Exception {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("data/법정동코드 전체자료.txt");
        if (inputStream == null) {
            throw new IllegalStateException("📁 data/법정동코드 전체자료.txt 파일을 찾을 수 없습니다.");
        }

        List<State> states = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("EUC-KR")))) {
            reader.readLine(); // 헤더 skip
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\t");
                if (parts.length >= 3 && parts[2].equals("존재")) {
                    states.add(new State(parts[0], parts[1]));
                }
            }
        }

        // batch insert
        long start = System.nanoTime();
        int inserted = 0;

        /*
        JDBC INSERT
         */
        // INSERT IGNORE
//        String sql = "INSERT IGNORE INTO state (code, name) VALUES (?, ?)"

        // ON DUPLICATE KEY UPDATE
//        String sql = "INSERT IGNORE INTO state (code, name) VALUES (?, ?)"
//                + "ON DUPLICATE KEY UPDATE name = VALUES(name)";

        // REPLACE INTO
//        String sql = "REPLACE INTO state (code, name) VALUES (?, ?)";

//        int[][] batchResults = jdbcTemplate.batchUpdate(sql, states, 1000,
//                (PreparedStatement ps, State state) -> {
//                    ps.setString(1, state.getCode());
//                    ps.setString(2, state.getName());
//                });
//
//        inserted = 0;
//        for (int[] batch : batchResults) {
//            for (int count : batch) {
//                if (count == Statement.SUCCESS_NO_INFO) {
//                    inserted++;
//                }
//            }
//        }

        /*
        JPA
         */
        inserted = 0;

        for (int i = 0; i < states.size(); i++) {
            State state = states.get(i);

            try {
                if (em.find(State.class, state.getCode()) == null) {
                    em.persist(state);
                    inserted++;
                }
            } catch (Exception e) {
                log.error("🚨 삽입 중 오류 발생: code={}, name={}", state.getCode(), state.getName(), e);
            }

            if (i % 1000 == 0) {
                em.flush();
                em.clear();
            }
        }

        em.flush();
        em.clear();

        long end = System.nanoTime();

        Set<String> codes = new HashSet<>();
        long dupes = states.stream()
                .filter(s -> !codes.add(s.getCode()))
                .count();
        log.info("📌 중복된 code 개수: {}", dupes);

        log.info("소요 시간(ms): " + (end - start) / 1_000_000);
        log.info("✅ 총 {}건 중 {}건 삽입 완료", states.size(), inserted);
    }
}