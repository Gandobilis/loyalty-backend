package com.multi.loyaltybackend.service;

import com.multi.loyaltybackend.model.User;
import com.multi.loyaltybackend.repository.UserRepository;
import com.multi.loyaltybackend.model.RegistrationStatus;
import com.multi.loyaltybackend.model.Event;
import com.multi.loyaltybackend.repository.EventRepository;
import com.multi.loyaltybackend.exception.DuplicateRegistrationException;
import com.multi.loyaltybackend.exception.ResourceNotFoundException;
import com.multi.loyaltybackend.model.Registration;
import com.multi.loyaltybackend.dto.RegistrationResponse;
import com.multi.loyaltybackend.repository.RegistrationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RegistrationService {

    private final RegistrationRepository eventRegistrationRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Transactional
    public RegistrationResponse registerUserToEvent(String email, Long eventId, String comment) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        log.info("Attempting to register user {} to event {}", email, eventId);

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", "id", eventId));

        if (eventRegistrationRepository.existsByUserIdAndEventId(user.getId(), eventId)) {
            throw new DuplicateRegistrationException(
                    String.format("User %s is already registered for event %d", email, eventId)
            );
        }

        Registration registration = Registration.builder()
                .user(user)
                .event(event)
                .comment(comment)
                .status(RegistrationStatus.REGISTERED)
                .build();

        Registration savedRegistration = eventRegistrationRepository.save(registration);

        log.info("Successfully registered user {} to event {}", email, eventId);

        return mapToResponse(savedRegistration);
    }

    @Transactional
    public RegistrationResponse updateRegistrationStatus(Long registrationId, RegistrationStatus newStatus) {
        log.info("Updating registration {} status to {}", registrationId, newStatus);

        Registration registration = eventRegistrationRepository.findById(registrationId)
                .orElseThrow(() -> new ResourceNotFoundException("Registration", "id", registrationId));

        registration.setStatus(newStatus);
        Registration updatedRegistration = eventRegistrationRepository.save(registration);

        log.info("Successfully updated registration {} status to {}", registrationId, newStatus);

        return mapToResponse(updatedRegistration);
    }

    @Transactional
    public void cancelRegistration(Long registrationId) {
        log.info("Canceling registration {}", registrationId);

        Registration registration = eventRegistrationRepository.findById(registrationId)
                .orElseThrow(() -> new ResourceNotFoundException("Registration", "id", registrationId));

        registration.setStatus(RegistrationStatus.CANCELLED);
        eventRegistrationRepository.save(registration);

        log.info("Successfully cancelled registration {}", registrationId);
    }

    public RegistrationResponse getRegistration(Long registrationId) {
        log.debug("Fetching registration {}", registrationId);

        Registration registration = eventRegistrationRepository.findById(registrationId)
                .orElseThrow(() -> new ResourceNotFoundException("Registration", "id", registrationId));

        return mapToResponse(registration);
    }

    private RegistrationResponse mapToResponse(Registration registration) {
        return new RegistrationResponse(
                registration.getId(),
                registration.getUser().getId(),
                registration.getUser().getUsername(),
                registration.getEvent().getId(),
                registration.getEvent().getTitle(),
                registration.getComment(),
                registration.getStatus(),
                registration.getRegisteredAt(),
                registration.getUpdatedAt()
        );
    }
}