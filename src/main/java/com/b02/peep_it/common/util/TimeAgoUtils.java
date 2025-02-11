package com.b02.peep_it.common.util;

import com.b02.peep_it.domain.Peep;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

@Component
public class TimeAgoUtils {
    public String getTimeAgo(LocalDateTime createdAt) {
        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(createdAt, now);

        long seconds = duration.getSeconds();
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (seconds < 60) {
            return "방금 전";
        } else if (minutes < 60) {
            return minutes + "분 전";
        } else if (hours < 24) {
            return hours + "시간 전";
        } else if (days < 7) {
            return days + "일 전";
        } else if (days < 30) {
            return (days / 7) + "주 전";
        } else if (days < 365) {
            return (days / 30) + "개월 전";
        } else {
            return (days / 365) + "년 전";
        }
    }

    /*
     활성 상태 확인
     */
    public boolean isActiveWithin24Hours(LocalDateTime activeTime) {
        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(activeTime, now);

        // 24시간(= 86,400초) 이내인지 확인
        return duration.toHours() < 24;
    }
}

