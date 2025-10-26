package com.multi.loyaltybackend.chat.service;

import com.multi.loyaltybackend.chat.dto.*;
import com.multi.loyaltybackend.chat.exception.ChatAccessDeniedException;
import com.multi.loyaltybackend.chat.exception.ChatAlreadyClosedException;
import com.multi.loyaltybackend.chat.exception.ChatNotFoundException;
import com.multi.loyaltybackend.chat.mapper.ChatMapper;
import com.multi.loyaltybackend.chat.model.Chat;
import com.multi.loyaltybackend.chat.model.ChatMessage;
import com.multi.loyaltybackend.chat.model.ChatStatus;
import com.multi.loyaltybackend.chat.model.MessageType;
import com.multi.loyaltybackend.chat.repository.ChatMessageRepository;
import com.multi.loyaltybackend.chat.repository.ChatRepository;
import com.multi.loyaltybackend.chat.specification.ChatSpecifications;
import com.multi.loyaltybackend.exception.UserNotFoundException;
import com.multi.loyaltybackend.model.Role;
import com.multi.loyaltybackend.model.User;
import com.multi.loyaltybackend.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing support chat operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final ChatRepository chatRepository;
    private final ChatMessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ChatMapper chatMapper;
    private final S3FileService s3FileService;
    private final DynamoDbChatMessageService dynamoDbService;

    /**
     * Create a new support chat
     */
    @Transactional
    public ChatResponse createChat(Long userId, ChatCreateRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));

        // Create chat
        Chat chat = Chat.builder()
            .user(user)
            .subject(request.getSubject())
            .status(ChatStatus.OPEN)
            .messageCount(1)
            .unreadByAdmin(1)
            .lastMessagePreview(truncateMessage(request.getInitialMessage()))
            .lastMessageAt(LocalDateTime.now())
            .build();

        chat = chatRepository.save(chat);

        // Create initial message
        ChatMessage message = ChatMessage.builder()
            .chat(chat)
            .sender(user)
            .content(request.getInitialMessage())
            .messageType(MessageType.TEXT)
            .isRead(false)
            .isAdminMessage(false)
            .build();

        messageRepository.save(message);

        // Save to DynamoDB
        String dynamoDbMessageId = dynamoDbService.saveMessage(message);
        message.setDynamoDbMessageId(dynamoDbMessageId);
        messageRepository.save(message);

        log.info("Created chat {} for user {}", chat.getId(), userId);
        return chatMapper.toChatResponse(chat);
    }

    /**
     * Send a message in a chat
     */
    @Transactional
    public MessageResponse sendMessage(Long userId, MessageSendRequest request, MultipartFile attachment) {
        User sender = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));

        Chat chat = chatRepository.findById(request.getChatId())
            .orElseThrow(() -> new ChatNotFoundException(request.getChatId()));

        // Check access
        validateChatAccess(sender, chat);

        // Check if chat is closed
        if (chat.getStatus() == ChatStatus.CLOSED) {
            chat.reopen();
        }

        // Handle file attachment
        String s3Key = null;
        String filename = null;
        Long fileSize = null;
        String mimeType = null;

        if (attachment != null && !attachment.isEmpty()) {
            s3Key = s3FileService.uploadFile(attachment, chat.getId());
            filename = attachment.getOriginalFilename();
            fileSize = attachment.getSize();
            mimeType = attachment.getContentType();
            request.setMessageType(MessageType.FILE);
        }

        // Create message
        ChatMessage message = ChatMessage.builder()
            .chat(chat)
            .sender(sender)
            .content(request.getContent())
            .messageType(request.getMessageType())
            .attachmentS3Key(s3Key)
            .attachmentFilename(filename)
            .attachmentSize(fileSize)
            .attachmentMimeType(mimeType)
            .isRead(false)
            .isAdminMessage(sender.getRole() == Role.ADMIN)
            .build();

        message = messageRepository.save(message);

        // Update chat
        chat.incrementMessageCount();
        chat.setLastMessagePreview(truncateMessage(request.getContent()));
        chat.setLastMessageAt(LocalDateTime.now());

        if (sender.getRole() == Role.ADMIN) {
            chat.setUnreadByUser(chat.getUnreadByUser() + 1);
        } else {
            chat.setUnreadByAdmin(chat.getUnreadByAdmin() + 1);
        }

        chatRepository.save(chat);

        // Save to DynamoDB
        String dynamoDbMessageId = dynamoDbService.saveMessage(message);
        message.setDynamoDbMessageId(dynamoDbMessageId);
        messageRepository.save(message);

        // Generate presigned URL for attachment
        String attachmentUrl = s3Key != null ? s3FileService.generatePresignedUrl(s3Key) : null;

        log.info("Message sent in chat {} by user {}", chat.getId(), userId);
        return chatMapper.toMessageResponse(message, attachmentUrl);
    }

    /**
     * Get messages for a chat
     */
    public Page<MessageResponse> getMessages(Long userId, Long chatId, Pageable pageable) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));

        Chat chat = chatRepository.findById(chatId)
            .orElseThrow(() -> new ChatNotFoundException(chatId));

        validateChatAccess(user, chat);

        Page<ChatMessage> messages = messageRepository.findByChatOrderByCreatedAtAsc(chat, pageable);

        return messages.map(message -> {
            String attachmentUrl = message.getAttachmentS3Key() != null ?
                s3FileService.generatePresignedUrl(message.getAttachmentS3Key()) : null;
            return chatMapper.toMessageResponse(message, attachmentUrl);
        });
    }

    /**
     * Get all chats for a user
     */
    public List<ChatResponse> getUserChats(Long userId, ChatFilterDTO filters) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));

        Specification<Chat> spec = ChatSpecifications.byUser(user);

        if (filters != null) {
            spec = spec.and(ChatSpecifications.withFilters(filters));
        }

        List<Chat> chats = chatRepository.findAll(spec);
        return chats.stream()
            .map(chatMapper::toChatResponse)
            .collect(Collectors.toList());
    }

    /**
     * Get all chats (admin)
     */
    public List<ChatResponse> getAllChats(ChatFilterDTO filters) {
        Specification<Chat> spec = Specification.where(null);

        if (filters != null) {
            spec = ChatSpecifications.withFilters(filters);
        }

        List<Chat> chats = chatRepository.findAll(spec);
        return chats.stream()
            .map(chatMapper::toChatResponse)
            .collect(Collectors.toList());
    }

    /**
     * Get a specific chat
     */
    public ChatResponse getChat(Long userId, Long chatId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));

        Chat chat = chatRepository.findById(chatId)
            .orElseThrow(() -> new ChatNotFoundException(chatId));

        validateChatAccess(user, chat);

        return chatMapper.toChatResponse(chat);
    }

    /**
     * Mark messages as read
     */
    @Transactional
    public void markMessagesAsRead(Long userId, Long chatId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));

        Chat chat = chatRepository.findById(chatId)
            .orElseThrow(() -> new ChatNotFoundException(chatId));

        validateChatAccess(user, chat);

        messageRepository.markAllMessagesAsReadForUser(chat, user);

        // Update unread counts
        if (user.getRole() == Role.ADMIN) {
            chat.setUnreadByAdmin(0);
        } else {
            chat.setUnreadByUser(0);
        }

        chatRepository.save(chat);
        log.info("Marked messages as read in chat {} for user {}", chatId, userId);
    }

    /**
     * Close a chat
     */
    @Transactional
    public ChatResponse closeChat(Long userId, Long chatId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));

        Chat chat = chatRepository.findById(chatId)
            .orElseThrow(() -> new ChatNotFoundException(chatId));

        validateChatAccess(user, chat);

        if (chat.getStatus() == ChatStatus.CLOSED) {
            throw new ChatAlreadyClosedException(chatId);
        }

        chat.close();
        chatRepository.save(chat);

        // Create system message
        ChatMessage systemMessage = ChatMessage.builder()
            .chat(chat)
            .sender(user)
            .content("Chat closed by " + (user.getRole() == Role.ADMIN ? "support" : "user"))
            .messageType(MessageType.SYSTEM)
            .isRead(true)
            .isAdminMessage(user.getRole() == Role.ADMIN)
            .build();

        messageRepository.save(systemMessage);
        dynamoDbService.saveMessage(systemMessage);

        log.info("Closed chat {} by user {}", chatId, userId);
        return chatMapper.toChatResponse(chat);
    }

    /**
     * Assign chat to an agent (admin only)
     */
    @Transactional
    public ChatResponse assignChat(Long chatId, Long agentId) {
        Chat chat = chatRepository.findById(chatId)
            .orElseThrow(() -> new ChatNotFoundException(chatId));

        User agent = userRepository.findById(agentId)
            .orElseThrow(() -> new UserNotFoundException(agentId));

        if (agent.getRole() != Role.ADMIN) {
            throw new IllegalArgumentException("Can only assign to admin users");
        }

        chat.setAssignedTo(agent);
        chat.setStatus(ChatStatus.ACTIVE);
        chatRepository.save(chat);

        // Create system message
        ChatMessage systemMessage = ChatMessage.builder()
            .chat(chat)
            .sender(agent)
            .content("Chat assigned to " + agent.getFullName())
            .messageType(MessageType.SYSTEM)
            .isRead(false)
            .isAdminMessage(true)
            .build();

        messageRepository.save(systemMessage);
        dynamoDbService.saveMessage(systemMessage);

        log.info("Assigned chat {} to agent {}", chatId, agentId);
        return chatMapper.toChatResponse(chat);
    }

    /**
     * Delete a chat and all its messages
     */
    @Transactional
    public void deleteChat(Long userId, Long chatId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(userId));

        Chat chat = chatRepository.findById(chatId)
            .orElseThrow(() -> new ChatNotFoundException(chatId));

        validateChatAccess(user, chat);

        // Delete files from S3
        List<ChatMessage> messages = messageRepository.findByChatOrderByCreatedAtAsc(chat);
        messages.stream()
            .filter(m -> m.getAttachmentS3Key() != null)
            .forEach(m -> s3FileService.deleteFile(m.getAttachmentS3Key()));

        // Delete from DynamoDB
        dynamoDbService.deleteMessagesForChat(chatId);

        // Delete from PostgreSQL
        chatRepository.delete(chat);

        log.info("Deleted chat {} by user {}", chatId, userId);
    }

    private void validateChatAccess(User user, Chat chat) {
        if (user.getRole() != Role.ADMIN && !chat.getUser().getId().equals(user.getId())) {
            throw new ChatAccessDeniedException(user.getId(), chat.getId());
        }
    }

    private String truncateMessage(String message) {
        if (message == null) return null;
        return message.length() > 200 ? message.substring(0, 197) + "..." : message;
    }
}
