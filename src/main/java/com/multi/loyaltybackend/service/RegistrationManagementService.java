package com.multi.loyaltybackend.service;

import com.multi.loyaltybackend.dto.RegistrationFilterDTO;
import com.multi.loyaltybackend.dto.RegistrationManagementDTO;
import com.multi.loyaltybackend.exception.RegistrationNotFoundException;
import com.multi.loyaltybackend.mapper.RegistrationMapper;
import com.multi.loyaltybackend.model.Registration;
import com.multi.loyaltybackend.model.RegistrationStatus;
import com.multi.loyaltybackend.model.User;
import com.multi.loyaltybackend.repository.RegistrationRepository;
import com.multi.loyaltybackend.repository.RegistrationSpecifications;
import com.multi.loyaltybackend.repository.UserRepository;
import com.multi.loyaltybackend.validator.RegistrationValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing event registrations in the admin panel
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RegistrationManagementService {

    private final RegistrationRepository registrationRepository;
    private final UserRepository userRepository;
    private final RegistrationValidator registrationValidator;
    private final RegistrationMapper registrationMapper;
    private final PointsAwardService pointsAwardService;

    /**
     * Get filtered registrations with pagination
     */
    public Page<RegistrationManagementDTO> getFilteredRegistrations(RegistrationFilterDTO filter, Pageable pageable) {
        Specification<Registration> spec = buildSpecification(filter);
        Page<Registration> registrations = registrationRepository.findAll(spec, pageable);
        return registrations.map(registrationMapper::toManagementDTO);
    }

    /**
     * Update registration status with business logic validation
     */
    @Transactional
    public void updateRegistrationStatus(Long registrationId, RegistrationStatus newStatus) {
        Registration registration = findRegistrationById(registrationId);
        RegistrationStatus oldStatus = registration.getStatus();

        // Validate volunteer limit when approving
        if (isApprovingRegistration(oldStatus, newStatus)) {
            registrationValidator.validateVolunteerLimit(registration.getEvent());
        }

        // Award points when completing
        if (isCompletingRegistration(oldStatus, newStatus)) {
            pointsAwardService.awardEventPoints(registration);
        }

        registration.setStatus(newStatus);
        registrationRepository.save(registration);

        log.info("Updated registration {} status from {} to {}", registrationId, oldStatus, newStatus);
    }

    /**
     * Delete registration
     */
    @Transactional
    public void deleteRegistration(Long registrationId) {
        if (!registrationRepository.existsById(registrationId)) {
            throw new RegistrationNotFoundException(registrationId);
        }
        registrationRepository.deleteById(registrationId);
        log.info("Deleted registration {}", registrationId);
    }

    private Registration findRegistrationById(Long id) {
        return registrationRepository.findById(id)
                .orElseThrow(() -> new RegistrationNotFoundException(id));
    }

    private boolean isApprovingRegistration(RegistrationStatus oldStatus, RegistrationStatus newStatus) {
        return oldStatus == RegistrationStatus.PENDING && newStatus == RegistrationStatus.REGISTERED;
    }

    private boolean isCompletingRegistration(RegistrationStatus oldStatus, RegistrationStatus newStatus) {
        return oldStatus == RegistrationStatus.REGISTERED && newStatus == RegistrationStatus.COMPLETED;
    }

    private Specification<Registration> buildSpecification(RegistrationFilterDTO filter) {
        Specification<Registration> spec = Specification.where(null);

        if (filter == null) {
            return spec;
        }

        if (hasValue(filter.getUserEmail())) {
            spec = spec.and(RegistrationSpecifications.userEmailContains(filter.getUserEmail()));
        }
        if (hasValue(filter.getUserName())) {
            spec = spec.and(RegistrationSpecifications.userNameContains(filter.getUserName()));
        }
        if (hasValue(filter.getEventTitle())) {
            spec = spec.and(RegistrationSpecifications.eventTitleContains(filter.getEventTitle()));
        }
        if (filter.getEventId() != null) {
            spec = spec.and(RegistrationSpecifications.hasEventId(filter.getEventId()));
        }
        if (hasValue(filter.getStatus())) {
            spec = spec.and(RegistrationSpecifications.hasStatus(filter.getStatus()));
        }
        if (filter.getRegisteredFrom() != null) {
            spec = spec.and(RegistrationSpecifications.registeredAfter(filter.getRegisteredFrom().atStartOfDay()));
        }
        if (filter.getRegisteredTo() != null) {
            spec = spec.and(RegistrationSpecifications.registeredBefore(filter.getRegisteredTo().atTime(23, 59, 59)));
        }

        return spec;
    }

    private boolean hasValue(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
