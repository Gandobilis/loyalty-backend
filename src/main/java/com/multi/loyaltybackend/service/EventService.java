package com.multi.loyaltybackend.service;


import com.multi.loyaltybackend.config.LoggingConstants;
import com.multi.loyaltybackend.dto.EventFilterDTO;
import com.multi.loyaltybackend.dto.EventRequestDTO;
import com.multi.loyaltybackend.dto.EventResponseDTO;
import com.multi.loyaltybackend.dto.UserDTO;
import com.multi.loyaltybackend.model.Event;
import com.multi.loyaltybackend.model.RegistrationStatus;
import com.multi.loyaltybackend.model.User;
import com.multi.loyaltybackend.repository.EventRepository;
import com.multi.loyaltybackend.repository.EventSpecifications;
import com.multi.loyaltybackend.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final ImageStorageService imageStorageService;
    private final UserRepository userRepository;

    /**
     * Create event (API)
     */
    public EventResponseDTO createEvent(EventRequestDTO request, MultipartFile file) {
        return createEvent(request, file, LoggingConstants.API);
    }

    /**
     * Create event with app identifier
     */
    public EventResponseDTO createEvent(EventRequestDTO request, MultipartFile file, String appId) {
        log.info("{} {} {} - Title: {}, Category: {}",
                appId,
                LoggingConstants.CREATE,
                LoggingConstants.EVENT_ENTITY,
                request.title(),
                request.category());

        Event event = mapRequestToEntity(request);
        event = eventRepository.save(event);

        log.info("{} Successfully created Event ID={} - Title: {}",
                appId, event.getId(), event.getTitle());

        return mapEntityToResponse(event);
    }

    /**
     * Update event (API)
     */
    public EventResponseDTO updateEvent(
            Long id,
            EventRequestDTO request,
            MultipartFile file
    ) {
        return updateEvent(id, request, file, LoggingConstants.API);
    }

    /**
     * Update event with app identifier
     */
    public EventResponseDTO updateEvent(
            Long id,
            EventRequestDTO request,
            MultipartFile file,
            String appId
    ) {
        log.info("{} {} {} ID={} - Title: {}, Category: {}",
                appId,
                LoggingConstants.UPDATE,
                LoggingConstants.EVENT_ENTITY,
                id,
                request.title(),
                request.category());

        Event existingEvent = eventRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("{} {} attempt failed - Event ID={} not found",
                            appId,
                            LoggingConstants.UPDATE,
                            id);
                    return new EntityNotFoundException("Event not found with ID: " + id);
                });

        existingEvent.setTitle(request.title());
        existingEvent.setShortDescription(request.shortDescription());
        existingEvent.setDescription(request.description());
        existingEvent.setCategory(request.category());
        existingEvent.setAddress(request.address());
        existingEvent.setLatitude(request.latitude());
        existingEvent.setLongitude(request.longitude());
        existingEvent.setDateTime(request.dateTime());
        Event updatedEvent = eventRepository.save(existingEvent);

        log.info("{} Successfully updated Event ID={} - Title: {}",
                appId, updatedEvent.getId(), updatedEvent.getTitle());

        return mapEntityToResponse(updatedEvent);
    }

    @Transactional(readOnly = true)
    public EventResponseDTO getEventById(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Event not found with ID: " + id));
        return mapEntityToResponse(event);
    }


    @Transactional(readOnly = true)
    public List<EventResponseDTO> getAllEvents(String email, String search, String category, LocalDate startDate, LocalDate endDate) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        Specification<Event> spec = Specification.where(null);


        if (search != null) {
            spec = spec.and(EventSpecifications.searchContains(search));
        }

        if (category != null) {
            spec = spec.and(EventSpecifications.hasCategory(category));
        }

        if (startDate != null) {
            spec = spec.and(EventSpecifications.dateTimeAfter(startDate.atStartOfDay()));
        }

        if (endDate != null) {
            spec = spec.and(EventSpecifications.dateTimeBefore(endDate.atTime(23, 59, 59)));
        }

        Specification<Event> baseSpec = EventSpecifications.isFutureEvent();
        Specification<Event> combinedSpec = baseSpec.and(EventSpecifications.isNotRegisteredBy(user.getId()));
        if (spec != null) {
            combinedSpec = combinedSpec.and(spec);
        }
        List<Event> events = eventRepository.findAll(combinedSpec);

        return events.stream()
                .map(this::mapEntityToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Delete event (API)
     */
    public void deleteEvent(Long id) {
        deleteEvent(id, LoggingConstants.API);
    }

    /**
     * Delete event with app identifier
     */
    public void deleteEvent(Long id, String appId) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("{} {} attempt failed - Event ID={} not found",
                            appId,
                            LoggingConstants.DELETE,
                            id);
                    return new EntityNotFoundException("Event not found with ID: " + id);
                });

        log.info("{} {} {} ID={} - Title: {}",
                appId,
                LoggingConstants.DELETE,
                LoggingConstants.EVENT_ENTITY,
                id,
                event.getTitle());

        eventRepository.deleteById(id);

        log.info("{} Successfully deleted Event ID={}", appId, id);
    }

    /**
     * Get filtered events with pagination for admin panel
     */
    @Transactional(readOnly = true)
    public Page<Event> getFilteredEvents(EventFilterDTO filter, Pageable pageable) {
        Specification<Event> spec = Specification.where(null);

        if (filter != null) {
            if (filter.getTitle() != null && !filter.getTitle().isEmpty()) {
                spec = spec.and(EventSpecifications.titleContains(filter.getTitle()));
            }
            if (filter.getCategory() != null && !filter.getCategory().isEmpty()) {
                spec = spec.and(EventSpecifications.hasCategory(filter.getCategory()));
            }
            if (filter.getAddress() != null && !filter.getAddress().isEmpty()) {
                spec = spec.and(EventSpecifications.addressContains(filter.getAddress()));
            }
            if (filter.getDateFrom() != null) {
                spec = spec.and(EventSpecifications.dateTimeAfter(filter.getDateFrom().atStartOfDay()));
            }
            if (filter.getDateTo() != null) {
                spec = spec.and(EventSpecifications.dateTimeBefore(filter.getDateTo().atTime(23, 59, 59)));
            }
            if (filter.getMinPoints() != null) {
                spec = spec.and(EventSpecifications.hasPointsGreaterThanOrEqual(filter.getMinPoints()));
            }
            if (filter.getMaxPoints() != null) {
                spec = spec.and(EventSpecifications.hasPointsLessThanOrEqual(filter.getMaxPoints()));
            }
            if (filter.getHasLocation() != null && filter.getHasLocation()) {
                spec = spec.and(EventSpecifications.hasLocation());
            }
        }

        Page<Event> events = eventRepository.findAll(spec, pageable);
        return events.map(event -> {
            // Map fileName to full image path
            if (event.getFileName() != null) {
                event.setFileName(imageStorageService.getFilePath(event.getFileName()));
            }
            return event;
        });
    }

    /**
     * Get all events (no pagination) for admin panel
     */
    @Transactional(readOnly = true)
    public List<Event> getAllEventsForAdmin() {
        return eventRepository.findAll();
    }

    private Event mapRequestToEntity(EventRequestDTO request) {
        Event event = new Event();
        event.setTitle(request.title());
        event.setShortDescription(request.shortDescription());
        event.setDescription(request.description());
        event.setCategory(request.category());
        event.setAddress(request.address());
        event.setLatitude(request.latitude());
        event.setLongitude(request.longitude());
        event.setDateTime(request.dateTime());
        return event;
    }

    private EventResponseDTO mapEntityToResponse(Event event) {
        return new EventResponseDTO(
                event.getId(),
                (imageStorageService.getFilePath(event.getFileName())),
                event.getTitle(),
                event.getShortDescription(),
                event.getDescription(),
                event.getCategory(),
                event.getAddress(),
                event.getLatitude(),
                event.getLongitude(),
                event.getDateTime(),
                eventRepository.countRegistrationsForEventWithStatus(event.getId(), RegistrationStatus.PENDING),
                eventRepository.countRegistrationsForEventWithStatus(event.getId(), RegistrationStatus.REGISTERED),
                eventRepository.countRegistrationsForEventWithStatus(event.getId(), RegistrationStatus.COMPLETED),
                eventRepository.countRegistrationsForEventWithStatus(event.getId(), RegistrationStatus.CANCELLED),
                event.getMaxParticipants(),
                event.getCreatedAt(),
                event.getUpdatedAt(),
                event.getUsers().stream()
                        .filter(registration -> registration.getStatus() == RegistrationStatus.REGISTERED)
                        .map(registration -> UserDTO.builder()
                        .id(registration.getUser().getId())
                        .fileName(imageStorageService.getFilePath(registration.getUser().getFileName()))
                        .status(registration.getStatus().toString())
                        .build()).collect(Collectors.toList())
        );
    }
}