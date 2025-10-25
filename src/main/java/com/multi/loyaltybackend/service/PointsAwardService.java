package com.multi.loyaltybackend.service;

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
     * Awards event points to user when registration is completed
     *
     * @param registration the completed registration
     */
    @Transactional
    public void awardEventPoints(Registration registration) {
        User user = registration.getUser();
        Event event = registration.getEvent();

        if (!shouldAwardPoints(event)) {
            log.debug("No points to award for event {} - points: {}", event.getId(), event.getPoints());
            return;
        }

        int pointsAwarded = event.getPoints();
        int currentPoints = getOrDefault(user.getTotalPoints(), 0);
        user.setTotalPoints(currentPoints + pointsAwarded);

        int currentEventCount = getOrDefault(user.getEventCount(), 0);
        user.setEventCount(currentEventCount + 1);

        userRepository.save(user);

        log.info("Awarded {} points to user {} for completing event {}",
                pointsAwarded, user.getId(), event.getId());
    }

    private boolean shouldAwardPoints(Event event) {
        return event.getPoints() != null && event.getPoints() > 0;
    }

    private int getOrDefault(Integer value, int defaultValue) {
        return value != null ? value : defaultValue;
    }
}
