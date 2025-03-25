package com.b02.peep_it.service;

import com.b02.peep_it.common.exception.CustomError;
import com.b02.peep_it.common.response.CommonResponse;
import com.b02.peep_it.common.util.AuthUtils;
import com.b02.peep_it.domain.Member;
import com.b02.peep_it.domain.State;
import com.b02.peep_it.domain.Town;
import com.b02.peep_it.dto.RequestPatchTownDto;
import com.b02.peep_it.dto.member.CommonMemberDto;
import com.b02.peep_it.repository.StateRepository;
import com.b02.peep_it.repository.TownRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    public ResponseEntity<CommonResponse<CommonMemberDto>> updateTownInfo(RequestPatchTownDto requestDto) {
        // 현재 사용자 정보를 불러온다
        Member member = userInfo.getCurrentMember();
        Optional<State> townName = stateRepository.findByCode(requestDto.legalDistrictCode());
        if (townName.isEmpty()) {
            return CommonResponse.failed(CustomError.TOWN_NOT_FOUND);
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
}
