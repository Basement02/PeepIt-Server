package com.b02.peep_it.dto;

import com.b02.peep_it.domain.CustomGender;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;

@Builder
public record RequestSignUpDto(
        @NotNull
        @Size(min = 10, max = 20, message = "ID는 10~20자 이내로 입력해주세요.")
        @Pattern(regexp = "^[a-zA-Z0-9_.]+$", message = "ID는 영어, 숫자, 밑줄(_) 및 마침표(.)만 사용할 수 있습니다.")
        String id,

        @NotNull
        @Size(min = 1, max = 18, message = "닉네임은 1자 이상 18자 이내로 입력해주세요.")
        @Pattern(regexp = "^[가-힣a-zA-Z0-9]+$", message = "닉네임은 한글, 영어, 숫자만 사용할 수 있습니다.")
        String nickname,

        @Nullable
        LocalDateTime birth,

        @Nullable
        CustomGender gender,

        @NotNull
        Boolean isAgree, // 마케팅 약관 동의 여부

        @Nullable
        @Pattern(regexp = "^\\d{11}$", message = "전화번호는 11자리 숫자만 입력 가능합니다.")
        String phone
        ) {
}
