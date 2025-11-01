package com.multi.loyaltybackend.service;

import com.multi.loyaltybackend.dto.CreateSupportMessageRequest;
import com.multi.loyaltybackend.dto.RespondToSupportMessageRequest;
import com.multi.loyaltybackend.dto.SupportMessageResponse;
import com.multi.loyaltybackend.dto.SupportMessageResponseDTO;
import com.multi.loyaltybackend.exception.ResourceNotFoundException;
import com.multi.loyaltybackend.model.SupportMessage;
import com.multi.loyaltybackend.model.SupportMessageStatus;
import com.multi.loyaltybackend.model.User;
import com.multi.loyaltybackend.repository.SupportMessageRepository;
import com.multi.loyaltybackend.repository.SupportMessageResponseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SupportMessageService {

    private final SupportMessageRepository supportMessageRepository;
    private final SupportMessageResponseRepository supportMessageResponseRepository;

    @Transactional
    public SupportMessageResponse createMessage(CreateSupportMessageRequest request, User user) {
        SupportMessage message = SupportMessage.builder()
                .user(user)
                .subject(request.getSubject())
                .message(request.getMessage())
                .status(SupportMessageStatus.OPEN)
                .build();

        SupportMessage savedMessage = supportMessageRepository.save(message);
        return mapToResponse(savedMessage);
    }

    @Transactional(readOnly = true)
    public Page<SupportMessageResponse> getUserMessages(User user, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<SupportMessage> messages = supportMessageRepository.findByUserOrderByCreatedAtDesc(user, pageable);
        return messages.map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public SupportMessageResponse getMessageById(Long id, User user) {
        SupportMessage message = supportMessageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SupportMessage", "id", id));

        // Check if user has access to this message (either owner or admin)
        if (!message.getUser().getId().equals(user.getId()) && !isAdmin(user)) {
            throw new AccessDeniedException("You don't have permission to view this message");
        }

        return mapToResponse(message);
    }

    @Transactional(readOnly = true)
    public Page<SupportMessageResponse> getAllMessages(int page, int size, SupportMessageStatus status) {
        Pageable pageable = PageRequest.of(page, size);
        Page<SupportMessage> messages;

        if (status != null) {
            messages = supportMessageRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        } else {
            messages = supportMessageRepository.findAllByOrderByCreatedAtDesc(pageable);
        }

        return messages.map(this::mapToResponse);
    }

    @Transactional
    public SupportMessageResponse respondToMessage(Long id, RespondToSupportMessageRequest request, User admin) {
        SupportMessage message = supportMessageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SupportMessage", "id", id));

        // Create a new response
        com.multi.loyaltybackend.model.SupportMessageResponse response =
            com.multi.loyaltybackend.model.SupportMessageResponse.builder()
                .supportMessage(message)
                .respondedBy(admin)
                .response(request.getResponse())
                .build();

        supportMessageResponseRepository.save(response);

        // Update status if provided
        if (request.getStatus() != null) {
            message.setStatus(request.getStatus());
            supportMessageRepository.save(message);
        }

        // Refresh message to get updated responses
        message = supportMessageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SupportMessage", "id", id));

        return mapToResponse(message);
    }

    @Transactional
    public SupportMessageResponse updateMessageStatus(Long id, SupportMessageStatus status, User admin) {
        SupportMessage message = supportMessageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SupportMessage", "id", id));

        message.setStatus(status);

        SupportMessage updatedMessage = supportMessageRepository.save(message);
        return mapToResponse(updatedMessage);
    }

    @Transactional(readOnly = true)
    public long countMessagesByStatus(SupportMessageStatus status) {
        return supportMessageRepository.countByStatus(status);
    }

    private SupportMessageResponse mapToResponse(SupportMessage message) {
        // Map responses
        var responseDTOs = message.getResponses().stream()
                .map(r -> SupportMessageResponseDTO.builder()
                        .id(r.getId())
                        .respondedByUserId(r.getRespondedBy().getId())
                        .respondedByFullName(r.getRespondedBy().getFullName())
                        .response(r.getResponse())
                        .createdAt(r.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return SupportMessageResponse.builder()
                .id(message.getId())
                .userId(message.getUser().getId())
                .userFullName(message.getUser().getFullName())
                .userEmail(message.getUser().getEmail())
                .userMobileNumber(message.getUser().getMobileNumber())
                .subject(message.getSubject())
                .message(message.getMessage())
                .status(message.getStatus())
                .responses(responseDTOs)
                .createdAt(message.getCreatedAt())
                .updatedAt(message.getUpdatedAt())
                .build();
    }

    private boolean isAdmin(User user) {
        return user.getRole().name().equals("ADMIN");
    }
}
