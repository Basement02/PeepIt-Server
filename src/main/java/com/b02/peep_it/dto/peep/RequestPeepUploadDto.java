package com.b02.peep_it.dto.peep;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "핍 업로드 요청 데이터 (JSON 형식)")
public record RequestPeepUploadDto(

        @Schema(description = "법정동 코드", example = "1111010100")
        String legalDistrictCode,

        @Schema(description = "핍 내용", example = "새로운 핍을 등록합니다.")
        String content,

        @Schema(description = "위도", example = "37.5665")
        Double latitude,

        @Schema(description = "경도", example = "126.9780")
        Double longitude,

        @Schema(description = "우편번호", example = "04524")
        String postalCode,

        @Schema(description = "도로명 주소", example = "서울특별시 중구 세종대로 110")
        String roadNameAddress,

        @Schema(description = "도로명 코드", example = "1234567890")
        String roadNameCode,

        @Schema(description = "건물명", example = "서울시청")
        String building
) {
}