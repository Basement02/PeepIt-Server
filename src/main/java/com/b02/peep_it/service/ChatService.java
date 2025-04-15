package com.b02.peep_it.service;

import com.b02.peep_it.common.response.CommonResponse;
import com.b02.peep_it.domain.Chat;
import com.b02.peep_it.domain.Member;
import com.b02.peep_it.domain.Peep;
import com.b02.peep_it.dto.ChatResponseDto;
import com.b02.peep_it.repository.ChatRepository;
import com.b02.peep_it.repository.MemberRepository;
import com.b02.peep_it.repository.PeepRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;
    private final MemberRepository memberRepository;
    private final PeepRepository peepRepository;

    public Chat save(Long peepId, String memberId, String content) {
        Member member = memberRepository.findById(memberId).orElseThrow();
        Peep peep = peepRepository.findById(peepId).orElseThrow();
        Chat chat = Chat.builder()
                .content(content)
                .peep(peep)
                .member(member)
                .build();
        return chatRepository.save(chat);
    }

    public ResponseEntity<CommonResponse<List<ChatResponseDto>>> getChatsByPeepId(Long peepId) {
        List<ChatResponseDto> chatList = chatRepository.findByPeepIdOrderByCreatedAtAsc(peepId)
                .stream()
                .map(chat -> ChatResponseDto.builder()
                        .sender(chat.getMember().getId())
                        .content(chat.getContent())
                        .sentAt(chat.getCreatedAt())
                        .build())
                .toList();

        return CommonResponse.ok(chatList);
    }
}