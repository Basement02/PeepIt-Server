package com.b02.peep_it.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatReceiveDto {
    private Long peepId;
    private String nickname;
    private String imgUrl;
    private String content;
    private String sendAt;
}
