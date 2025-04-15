package com.b02.peep_it.service;

import com.b02.peep_it.common.exception.CustomError;
import com.b02.peep_it.common.response.CommonResponse;
import com.b02.peep_it.common.response.PagedResponse;
import com.b02.peep_it.common.util.AuthUtils;
import com.b02.peep_it.domain.Member;
import com.b02.peep_it.domain.State;
import com.b02.peep_it.domain.Town;
import com.b02.peep_it.dto.CommonTownDto;
import com.b02.peep_it.dto.RequestPatchTownDto;
import com.b02.peep_it.dto.member.ResponseCommonMemberDto;
import com.b02.peep_it.repository.StateRepository;
import com.b02.peep_it.repository.TownRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class TownService {
    private final AuthUtils userInfo;
    private final StateRepository stateRepository;
    private final TownRepository townRepository;

    /*
    법정동 코드로 사용자 동네 등록
     */
    public ResponseEntity<CommonResponse<ResponseCommonMemberDto>> updateTownInfo(RequestPatchTownDto requestDto) {
        // 현재 사용자 정보를 불러온다
        Member member = userInfo.getCurrentMember();

        // 형식 검증: 10자리 숫자인지 확인
        String code = requestDto.legalDistrictCode();
        if (code == null || !code.matches("^[0-9]{10}$")) {
            return CommonResponse.failed(CustomError.STATE_NOT_FOUND);
        }

        Optional<State> townName = stateRepository.findByCode(requestDto.legalDistrictCode());
        if (townName.isEmpty()) {
            return CommonResponse.failed(CustomError.STATE_NOT_FOUND);
        }

        // 사용자의 Town 객체를 불러온다
        Optional<Town> town = townRepository.findByMid(member.getId());
        if (town.isEmpty()) {
            return CommonResponse.failed(CustomError.TOWN_NOT_FOUND);
        }

        // 법정동 코드와 사용자 코드로 Town 객체를 갱신한다
        townRepository.save(town.get().updateTown(townName.get()));

        return CommonResponse.created(null);
    }

    /*
    법정동 정보 조회
     */
    public ResponseEntity<CommonResponse<PagedResponse<CommonTownDto>>> getStateListInfo(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "code"));
        Page<State> statePage = stateRepository.findAll(pageRequest);

        Page<CommonTownDto> responseDtoPage = statePage.map(p -> {
            return CommonTownDto.builder()
                    .legalCode(p.getCode())
                    .name(p.getName())
                    .build();
        });

        PagedResponse<CommonTownDto> pagedResponse = PagedResponse.create(
                responseDtoPage.getContent(),
                responseDtoPage.getNumber(),
                responseDtoPage.getSize(),
                responseDtoPage.getTotalPages(),
                responseDtoPage.getTotalElements()
        );

        return CommonResponse.ok(pagedResponse);
    }

    /*
    법정동 코드로 법정동 정보 조회
     */
    public ResponseEntity<CommonResponse<CommonTownDto>> getStateInfo(String legalCode) {
        // 법정동 객체 조회
        Optional<State> optionalState = stateRepository.findByCode(legalCode);
        if (optionalState.isEmpty()) {
            return CommonResponse.failed(CustomError.TOWN_NOT_FOUND); // 존재하지 않는 법정동코드입니다
        }

        State state = optionalState.get();

        // response dto 생성
        CommonTownDto responseDto = CommonTownDto.builder()
                .legalCode(legalCode)
                .name(state.getName())
                .build();

        // response 반환
        return CommonResponse.ok(responseDto);
    }
}
