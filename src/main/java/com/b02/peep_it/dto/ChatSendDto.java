package com.b02.peep_it.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatSendDto {
    private Long peepId;
    private String uid;
    private String content;
    private LocalDateTime regDate;
}