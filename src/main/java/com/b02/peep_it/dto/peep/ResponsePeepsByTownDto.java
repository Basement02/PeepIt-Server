package com.b02.peep_it.dto.peep;

import lombok.Builder;

import java.util.List;
import java.util.Map;

@Builder
public record ResponsePeepsByTownDto(
        Map<String, String> topTowns, // 동네 코드 : 이름
        Map<String, List<CommonPeepDto>> peepsByTown // 동네 코드 : 핍 리스트
) {
}
