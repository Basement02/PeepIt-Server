package com.b02.peep_it.repository;

import com.b02.peep_it.domain.PushMsg;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PushMsgRepository extends JpaRepository<PushMsg, Long> {
}
