package com.multi.loyaltybackend.validator;

import com.multi.loyaltybackend.exception.VolunteerLimitExceededException;
import com.multi.loyaltybackend.model.Event;
import com.multi.loyaltybackend.model.RegistrationStatus;
import com.multi.loyaltybackend.repository.RegistrationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RegistrationValidator {

    private static final List<RegistrationStatus> ACTIVE_STATUSES = Arrays.asList(
            RegistrationStatus.REGISTERED,
            RegistrationStatus.COMPLETED
    );

    private final RegistrationRepository registrationRepository;

    /**
     * Validates if event has available volunteer slots
     *
     * @param event the event to check
     * @throws VolunteerLimitExceededException if volunteer limit is exceeded
     */
    public void validateVolunteerLimit(Event event) {
        if (!hasVolunteerLimit(event)) {
            return;
        }

        long activeRegistrationsCount = countActiveRegistrations(event.getId());

        if (activeRegistrationsCount >= event.getMaxParticipants()) {
            throw new VolunteerLimitExceededException(
                    String.format("Event has reached maximum volunteer limit (%d)", event.getMaxParticipants())
            );
        }
    }

    /**
     * Checks if event has a volunteer limit configured
     */
    private boolean hasVolunteerLimit(Event event) {
        return event != null && event.getMaxParticipants() > 0;
    }

    /**
     * Counts active (REGISTERED + COMPLETED) registrations for an event
     */
    private long countActiveRegistrations(Long eventId) {
        return registrationRepository.countByEventIdAndStatusIn(eventId, ACTIVE_STATUSES);
    }
}
