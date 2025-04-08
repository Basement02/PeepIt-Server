package com.b02.peep_it.dto;

import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public record RequestPatchProfileImgDto(
        @NotNull MultipartFile profileImg
) {
}
