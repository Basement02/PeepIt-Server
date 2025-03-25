package com.b02.peep_it.common;

import com.b02.peep_it.domain.State;
import com.b02.peep_it.repository.StateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class StateInitializer {

    private final StateRepository stateRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void insertAllStates() throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("data/법정동코드 전체자료.txt");

        if (inputStream == null) {
            throw new FileNotFoundException("data/법정동코드 전체자료.txt 파일을 찾을 수 없습니다.");
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, Charset.forName("EUC-KR")))) {
            reader.readLine(); // Skip header

            List<State> newStates = reader.lines()
                    .map(line -> line.split("\t"))
                    .filter(parts -> parts.length >= 3 && "존재".equals(parts[2]))
                    .map(parts -> new State(parts[0], parts[1]))
                    .collect(Collectors.toList());

            // 이미 DB에 존재하는 code들 조회
            Set<String> existingCodes = stateRepository.findAll().stream()
                    .map(State::getCode)
                    .collect(Collectors.toSet());

            // 중복 제거 후 insert 대상만 추림
            List<State> filtered = newStates.stream()
                    .filter(state -> !existingCodes.contains(state.getCode()))
                    .collect(Collectors.toList());

            stateRepository.saveAll(filtered);
            log.info("✅ 법정동 코드 {}건 삽입 완료", filtered.size());
        }
    }
}