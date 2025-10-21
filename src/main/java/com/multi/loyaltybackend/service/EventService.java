package com.multi.loyaltybackend.service;


import com.multi.loyaltybackend.dto.EventRequestDTO;
import com.multi.loyaltybackend.dto.EventResponseDTO;
import com.multi.loyaltybackend.dto.UserDTO;
import com.multi.loyaltybackend.model.Event;
import com.multi.loyaltybackend.model.Registration;
import com.multi.loyaltybackend.repository.EventRepository; // Hypothetical
import com.multi.loyaltybackend.repository.EventSpecifications;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class EventService {

    private final EventRepository eventRepository;
    ImageStorageService imageStorageService;

    public EventService(EventRepository eventRepository,  ImageStorageService imageStorageService) {
        this.eventRepository = eventRepository;
        this.imageStorageService = imageStorageService;
    }

    public EventResponseDTO createEvent(EventRequestDTO request) {
        Event event = mapRequestToEntity(request);
        event = eventRepository.save(event);
        return mapEntityToResponse(event);
    }


    @Transactional(readOnly = true)
    public EventResponseDTO getEventById(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Event not found with ID: " + id));
        return mapEntityToResponse(event);
    }


    @Transactional(readOnly = true)
    public List<EventResponseDTO> getAllEvents(String category, LocalDate startDate, LocalDate endDate) {
        Specification<Event> spec = Specification.where(null);

        if (category != null) {
            spec = spec.and(EventSpecifications.hasCategory(category));
        }

        if (startDate != null && endDate != null) {
            spec = spec.and(EventSpecifications.isBetweenDates(startDate, endDate));
        }

        List<Event> events = eventRepository.findAll(spec);

        return events.stream()
                .map(this::mapEntityToResponse)
                .collect(Collectors.toList());
    }

    public EventResponseDTO updateEvent(Long id, EventRequestDTO request) {
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


    public void deleteEvent(Long id) {
        eventRepository.deleteById(id);
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