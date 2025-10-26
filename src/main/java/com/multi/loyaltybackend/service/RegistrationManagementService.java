package com.multi.loyaltybackend.service;

import com.multi.loyaltybackend.config.LoggingConstants;
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
     * Update registration status with business logic validation (Admin Panel)
     */
    @Transactional
    public void updateRegistrationStatus(Long registrationId, RegistrationStatus newStatus) {
        updateRegistrationStatus(registrationId, newStatus, LoggingConstants.ADMIN_PANEL);
    }

    /**
     * Update registration status with business logic validation
     *
     * @param registrationId Registration ID
     * @param newStatus New status
     * @param appId Application identifier (ADMIN_PANEL or API)
     */
    @Transactional
    public void updateRegistrationStatus(Long registrationId, RegistrationStatus newStatus, String appId) {
        Registration registration = findRegistrationById(registrationId);
        RegistrationStatus oldStatus = registration.getStatus();

        log.info("{} {} {} - Status change: {} -> {}",
                appId,
                LoggingConstants.UPDATE,
                LoggingConstants.REGISTRATION_ENTITY,
                oldStatus,
                newStatus);

        // Validate volunteer limit when approving
        if (isApprovingRegistration(oldStatus, newStatus)) {
            log.debug("{} Validating volunteer limit for Event ID={}", appId, registration.getEvent().getId());
            registrationValidator.validateVolunteerLimit(registration.getEvent());
        }

        // Award points when completing
        if (isCompletingRegistration(oldStatus, newStatus)) {
            log.info("{} {} for Registration ID={}, User ID={}",
                    appId,
                    LoggingConstants.AWARD_POINTS,
                    registrationId,
                    registration.getUser().getId());
            pointsAwardService.awardEventPoints(registration, appId);
        }

        registration.setStatus(newStatus);
        registrationRepository.save(registration);

        log.info("{} Successfully updated Registration ID={} from {} to {}",
                appId, registrationId, oldStatus, newStatus);
    }

    /**
     * Delete registration (Admin Panel)
     */
    @Transactional
    public void deleteRegistration(Long registrationId) {
        deleteRegistration(registrationId, LoggingConstants.ADMIN_PANEL);
    }

    /**
     * Delete registration
     *
     * @param registrationId Registration ID
     * @param appId Application identifier (ADMIN_PANEL or API)
     */
    @Transactional
    public void deleteRegistration(Long registrationId, String appId) {
        if (!registrationRepository.existsById(registrationId)) {
            log.warn("{} {} attempt failed - Registration ID={} not found",
                    appId, LoggingConstants.DELETE, registrationId);
            throw new RegistrationNotFoundException(registrationId);
        }

        log.info("{} {} {} ID={}",
                appId,
                LoggingConstants.DELETE,
                LoggingConstants.REGISTRATION_ENTITY,
                registrationId);

        registrationRepository.deleteById(registrationId);

        log.info("{} Successfully deleted Registration ID={}", appId, registrationId);
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
