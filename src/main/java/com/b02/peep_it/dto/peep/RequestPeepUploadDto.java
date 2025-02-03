package com.b02.peep_it.dto.peep;

import lombok.Builder;

@Builder
public record RequestPeepUploadDto(
        String legalDistrictCode,
        String content,

        Double latitude,
        Double longitude,
        String postalCode,
        String roadNameAddress,
        String roadNameCode,
        String building
) {
}