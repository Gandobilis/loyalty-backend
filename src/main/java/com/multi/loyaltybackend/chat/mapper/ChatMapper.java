package com.multi.loyaltybackend.chat.mapper;

import com.multi.loyaltybackend.chat.dto.ChatResponse;
import com.multi.loyaltybackend.chat.dto.MessageResponse;
import com.multi.loyaltybackend.chat.model.Chat;
import com.multi.loyaltybackend.chat.model.ChatMessage;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting Chat entities to DTOs.
 */
@Component
public class ChatMapper {

    public ChatResponse toChatResponse(Chat chat) {
        return ChatResponse.builder()
            .id(chat.getId())
            .userId(chat.getUser().getId())
            .userFullName(chat.getUser().getFullName())
            .userEmail(chat.getUser().getEmail())
            .assignedToId(chat.getAssignedTo() != null ? chat.getAssignedTo().getId() : null)
            .assignedToFullName(chat.getAssignedTo() != null ? chat.getAssignedTo().getFullName() : null)
            .subject(chat.getSubject())
            .status(chat.getStatus())
            .messageCount(chat.getMessageCount())
            .unreadByUser(chat.getUnreadByUser())
            .unreadByAdmin(chat.getUnreadByAdmin())
            .lastMessagePreview(chat.getLastMessagePreview())
            .lastMessageAt(chat.getLastMessageAt())
            .createdAt(chat.getCreatedAt())
            .updatedAt(chat.getUpdatedAt())
            .closedAt(chat.getClosedAt())
            .build();
    }

    public MessageResponse toMessageResponse(ChatMessage message, String attachmentUrl) {
        return MessageResponse.builder()
            .id(message.getId())
            .chatId(message.getChat().getId())
            .senderId(message.getSender().getId())
            .senderFullName(message.getSender().getFullName())
            .senderEmail(message.getSender().getEmail())
            .content(message.getContent())
            .messageType(message.getMessageType())
            .attachmentS3Key(message.getAttachmentS3Key())
            .attachmentFilename(message.getAttachmentFilename())
            .attachmentSize(message.getAttachmentSize())
            .attachmentMimeType(message.getAttachmentMimeType())
            .attachmentUrl(attachmentUrl)
            .isRead(message.getIsRead())
            .readAt(message.getReadAt())
            .isAdminMessage(message.getIsAdminMessage())
            .createdAt(message.getCreatedAt())
            .build();
    }
}
