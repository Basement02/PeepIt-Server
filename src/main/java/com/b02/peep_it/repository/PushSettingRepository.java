package com.b02.peep_it.repository;

import com.b02.peep_it.domain.PushSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PushSettingRepository extends JpaRepository<PushSetting, Long> {
}
