package com.multi.loyaltybackend.mapper;

import com.multi.loyaltybackend.dto.response.UserEventResponse;
import com.multi.loyaltybackend.model.Event;
import com.multi.loyaltybackend.model.Registration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class UserEventMapper {

    public  UserEventResponse toResponse(Registration registration) {
        Event event = registration.getEvent();

        return UserEventResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .points(event.getPoints())
                .dateTime(event.getDateTime())
                .status(registration.getStatus())
                .build();
    }

    public List<UserEventResponse> toResponseList(List<Registration> registrations) {
        return registrations.stream()
                .map(this::toResponse)
                .toList();
    }
}
