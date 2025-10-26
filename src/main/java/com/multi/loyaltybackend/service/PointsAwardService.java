package com.multi.loyaltybackend.service;

import com.multi.loyaltybackend.config.LoggingConstants;
import com.multi.loyaltybackend.model.Event;
import com.multi.loyaltybackend.model.Registration;
import com.multi.loyaltybackend.model.User;
import com.multi.loyaltybackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service responsible for awarding points to users
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PointsAwardService {

    private final UserRepository userRepository;

    /**
     * Awards event points to user when registration is completed (Admin Panel)
     *
     * @param registration the completed registration
     */
    @Transactional
    public void awardEventPoints(Registration registration) {
        awardEventPoints(registration, LoggingConstants.ADMIN_PANEL);
    }

    /**
     * Awards event points to user when registration is completed
     *
     * @param registration the completed registration
     * @param appId Application identifier (ADMIN_PANEL or API)
     */
    @Transactional
    public void awardEventPoints(Registration registration, String appId) {
        User user = registration.getUser();
        Event event = registration.getEvent();

        if (!shouldAwardPoints(event)) {
            log.debug("{} No points to award for Event ID={} - points: {}",
                    appId, event.getId(), event.getPoints());
            return;
        }

        int pointsAwarded = event.getPoints();
        int currentPoints = getOrDefault(user.getTotalPoints(), 0);
        int newTotalPoints = currentPoints + pointsAwarded;
        user.setTotalPoints(newTotalPoints);

        int currentEventCount = getOrDefault(user.getEventCount(), 0);
        user.setEventCount(currentEventCount + 1);

        userRepository.save(user);

        log.info("{} {} - Awarded {} points to User ID={} (total: {} -> {}) for Event ID={}",
                appId,
                LoggingConstants.AWARD_POINTS,
                pointsAwarded,
                user.getId(),
                currentPoints,
                newTotalPoints,
                event.getId());
    }

    private boolean shouldAwardPoints(Event event) {
        return event.getPoints() != null && event.getPoints() > 0;
    }

    private int getOrDefault(Integer value, int defaultValue) {
        return value != null ? value : defaultValue;
    }
}
