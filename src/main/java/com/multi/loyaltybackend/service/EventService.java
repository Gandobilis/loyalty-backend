package com.multi.loyaltybackend.service;


import com.multi.loyaltybackend.dto.EventFilterDTO;
import com.multi.loyaltybackend.dto.EventRequestDTO;
import com.multi.loyaltybackend.dto.EventResponseDTO;
import com.multi.loyaltybackend.dto.UserDTO;
import com.multi.loyaltybackend.model.Event;
import com.multi.loyaltybackend.repository.EventRepository;
import com.multi.loyaltybackend.repository.EventSpecifications;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final ImageStorageService imageStorageService;

    public EventResponseDTO createEvent(EventRequestDTO request, MultipartFile file) {
        Event event = mapRequestToEntity(request);
        event = eventRepository.save(event);
        return mapEntityToResponse(event);
    }

    public EventResponseDTO updateEvent(
            Long id,
            EventRequestDTO request,
            MultipartFile file
    ) {
        Event existingEvent = eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Event not found with ID: " + id));

        existingEvent.setTitle(request.title());
        existingEvent.setShortDescription(request.shortDescription());
        existingEvent.setDescription(request.description());
        existingEvent.setCategory(request.category());
        existingEvent.setAddress(request.address());
        existingEvent.setLatitude(request.latitude());
        existingEvent.setLongitude(request.longitude());
        existingEvent.setDateTime(request.dateTime());
        Event updatedEvent = eventRepository.save(existingEvent);
        return mapEntityToResponse(updatedEvent);
    }

    @Transactional(readOnly = true)
    public EventResponseDTO getEventById(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Event not found with ID: " + id));
        return mapEntityToResponse(event);
    }


    @Transactional(readOnly = true)
    public List<EventResponseDTO> getAllEvents(String search, String category, LocalDate startDate, LocalDate endDate) {
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

        List<Event> events = eventRepository.findAll(spec);

        return events.stream()
                .map(this::mapEntityToResponse)
                .collect(Collectors.toList());
    }

    public void deleteEvent(Long id) {
        eventRepository.deleteById(id);
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
                (event.getFileName() != null
                        ? imageStorageService.getFilePath(event.getFileName())
                        : null),
                event.getTitle(),
                event.getShortDescription(),
                event.getDescription(),
                event.getCategory(),
                event.getAddress(),
                event.getLatitude(),
                event.getLongitude(),
                event.getDateTime(),
                event.getCreatedAt(),
                event.getUpdatedAt(),
                event.getUsers().stream().map(registration -> UserDTO.builder()
                        .id(registration.getUser().getId())
                        .fileName(registration.getUser().getFileName() != null
                                ? imageStorageService.getFilePath(registration.getUser().getFileName())
                                : null)
                        .build()).collect(Collectors.toList())
        );
    }
}