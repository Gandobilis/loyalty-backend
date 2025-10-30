package com.multi.loyaltybackend.service;

import com.multi.loyaltybackend.repository.CompanyRepository;
import com.multi.loyaltybackend.config.LoggingConstants;
import com.multi.loyaltybackend.dto.*;
import com.multi.loyaltybackend.exception.UserNotFoundException;
import com.multi.loyaltybackend.mapper.UserMapper;
import com.multi.loyaltybackend.model.Role;
import com.multi.loyaltybackend.model.User;
import com.multi.loyaltybackend.repository.EventRepository;
import com.multi.loyaltybackend.repository.RegistrationRepository;
import com.multi.loyaltybackend.repository.UserRepository;
import com.multi.loyaltybackend.specification.UserSpecifications;
import com.multi.loyaltybackend.repository.UserVoucherRepository;
import com.multi.loyaltybackend.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final VoucherRepository voucherRepository;
    private final EventRepository eventRepository;
    private final RegistrationRepository registrationRepository;
    private final UserVoucherRepository userVoucherRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    /**
     * Get dashboard statistics
     */
    public DashboardStatsDTO getDashboardStats() {
        List<User> allUsers = userRepository.findAll();
        long totalUsers = allUsers.stream()
                .filter(user -> user.getRole() == Role.USER)
                .count();
        long totalAdmins = allUsers.stream()
                .filter(user -> user.getRole() == Role.ADMIN)
                .count();

        long totalCompanies = companyRepository.count();
        long totalVouchers = voucherRepository.count();
        long totalEvents = eventRepository.count();
        long totalRegistrations = registrationRepository.count();

        LocalDateTime now = LocalDateTime.now();
        long activeVouchers = voucherRepository.findAll().stream()
                .filter(v -> v.getExpiry().isAfter(now))
                .count();
        long expiredVouchers = voucherRepository.findAll().stream()
                .filter(v -> v.getExpiry().isBefore(now))
                .count();

        long totalPointsDistributed = allUsers.stream()
                .mapToLong(user -> user.getTotalPoints() != null ? user.getTotalPoints() : 0)
                .sum();

        long totalVouchersExchanged = userVoucherRepository.count();

        return DashboardStatsDTO.builder()
                .totalUsers(totalUsers)
                .totalAdmins(totalAdmins)
                .totalCompanies(totalCompanies)
                .totalVouchers(totalVouchers)
                .totalEvents(totalEvents)
                .totalRegistrations(totalRegistrations)
                .activeVouchers(activeVouchers)
                .expiredVouchers(expiredVouchers)
                .totalPointsDistributed(totalPointsDistributed)
                .totalVouchersExchanged(totalVouchersExchanged)
                .build();
    }

    /**
     * Get all users for admin management
     */
    public List<UserManagementDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToUserManagementDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get filtered users with pagination
     */
    public Page<UserManagementDTO> getFilteredUsers(UserFilterDTO filter, Pageable pageable) {
        Specification<User> spec = Specification.where(null);

        if (filter != null) {
            if (filter.getEmail() != null && !filter.getEmail().isEmpty()) {
                spec = spec.and(UserSpecifications.emailContains(filter.getEmail()));
            }
            if (filter.getFullName() != null && !filter.getFullName().isEmpty()) {
                spec = spec.and(UserSpecifications.fullNameContains(filter.getFullName()));
            }
            if (filter.getRole() != null && !filter.getRole().isEmpty()) {
                spec = spec.and(UserSpecifications.hasRole(filter.getRole()));
            }
            if (filter.getMinPoints() != null) {
                spec = spec.and(UserSpecifications.hasPointsGreaterThanOrEqual(filter.getMinPoints()));
            }
            if (filter.getMaxPoints() != null) {
                spec = spec.and(UserSpecifications.hasPointsLessThanOrEqual(filter.getMaxPoints()));
            }
            if (filter.getCreatedFrom() != null) {
                spec = spec.and(UserSpecifications.createdAfter(filter.getCreatedFrom().atStartOfDay()));
            }
            if (filter.getCreatedTo() != null) {
                spec = spec.and(UserSpecifications.createdBefore(filter.getCreatedTo().atTime(23, 59, 59)));
            }
        }

        Page<User> users = userRepository.findAll(spec, pageable);
        return users.map(this::convertToUserManagementDTO);
    }

    /**
     * Get a single user by ID
     */
    public UserManagementDTO getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        return convertToUserManagementDTO(user);
    }

    /**
     * Update user role
     */
    @Transactional
    public UserManagementDTO updateUserRole(Long userId, Role newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        Role oldRole = user.getRole();
        log.info("{} {} {} ID={} - Role change: {} -> {}",
                LoggingConstants.ADMIN_PANEL,
                LoggingConstants.UPDATE,
                LoggingConstants.USER_ENTITY,
                userId,
                oldRole,
                newRole);

        user.setRole(newRole);
        User updatedUser = userRepository.save(user);

        log.info("{} Successfully updated User ID={} role to {}",
                LoggingConstants.ADMIN_PANEL, userId, newRole);

        return convertToUserManagementDTO(updatedUser);
    }

    /**
     * Update user points (add or subtract)
     */
    @Transactional
    public UserManagementDTO updateUserPoints(Long userId, Integer pointsToAdd) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        int currentPoints = user.getTotalPoints() != null ? user.getTotalPoints() : 0;
        int newPoints = currentPoints + pointsToAdd;

        // Ensure points don't go below 0
        if (newPoints < 0) {
            newPoints = 0;
        }

        log.info("{} {} {} ID={} - Points adjustment: {} (total: {} -> {})",
                LoggingConstants.ADMIN_PANEL,
                LoggingConstants.UPDATE,
                LoggingConstants.USER_ENTITY,
                userId,
                pointsToAdd > 0 ? "+" + pointsToAdd : pointsToAdd,
                currentPoints,
                newPoints);

        user.setTotalPoints(newPoints);
        User updatedUser = userRepository.save(user);

        log.info("{} Successfully updated User ID={} points to {}",
                LoggingConstants.ADMIN_PANEL, userId, newPoints);

        return convertToUserManagementDTO(updatedUser);
    }

    /**
     * Create a new user
     */
    @Transactional
    public User createUser(UserFormDTO userFormDTO) {
        log.info("{} {} {} - Email: {}, Role: {}",
                LoggingConstants.ADMIN_PANEL,
                LoggingConstants.CREATE,
                LoggingConstants.USER_ENTITY,
                userFormDTO.getEmail(),
                userFormDTO.getRole() != null ? userFormDTO.getRole() : Role.USER);

        // Check if email already exists
        if (userRepository.findByEmail(userFormDTO.getEmail()).isPresent()) {
            log.warn("{} {} attempt failed - Email already exists: {}",
                    LoggingConstants.ADMIN_PANEL,
                    LoggingConstants.CREATE,
                    userFormDTO.getEmail());
            throw new IllegalArgumentException("Email already exists");
        }

        User user = User.builder()
                .email(userFormDTO.getEmail())
                .password(userFormDTO.getPassword() != null && !userFormDTO.getPassword().isEmpty()
                    ? passwordEncoder.encode(userFormDTO.getPassword())
                    : null)
                .fullName(userFormDTO.getFullName())
                .age(userFormDTO.getAge())
                .mobileNumber(userFormDTO.getMobileNumber())
                .totalPoints(userFormDTO.getTotalPoints() != null ? userFormDTO.getTotalPoints() : 0)
                .eventCount(userFormDTO.getEventCount() != null ? userFormDTO.getEventCount() : 0)
                .workingHours(userFormDTO.getWorkingHours() != null ? userFormDTO.getWorkingHours() : 0)
                .aboutMe(userFormDTO.getAboutMe())
                .role(userFormDTO.getRole() != null ? userFormDTO.getRole() : Role.USER)
                .build();

        User savedUser = userRepository.save(user);

        log.info("{} Successfully created User ID={} - Email: {}",
                LoggingConstants.ADMIN_PANEL, savedUser.getId(), savedUser.getEmail());

        return savedUser;
    }

    /**
     * Update an existing user
     */
    @Transactional
    public User updateUser(Long userId, UserFormDTO userFormDTO) {
        log.info("{} {} {} ID={} - Email: {}, Role: {}",
                LoggingConstants.ADMIN_PANEL,
                LoggingConstants.UPDATE,
                LoggingConstants.USER_ENTITY,
                userId,
                userFormDTO.getEmail(),
                userFormDTO.getRole() != null ? userFormDTO.getRole() : Role.USER);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        if (!user.getEmail().equals(userFormDTO.getEmail())) {
            if (userRepository.findByEmail(userFormDTO.getEmail()).isPresent()) {
                log.warn("{} {} attempt failed - Email already exists: {}",
                        LoggingConstants.ADMIN_PANEL,
                        LoggingConstants.UPDATE,
                        userFormDTO.getEmail());
                throw new IllegalArgumentException("Email already exists");
            }
            log.debug("{} Email change for User ID={}: {} -> {}",
                    LoggingConstants.ADMIN_PANEL, userId, user.getEmail(), userFormDTO.getEmail());
            user.setEmail(userFormDTO.getEmail());
        }

        // Only update password if a new one is provided
        if (userFormDTO.getPassword() != null && !userFormDTO.getPassword().isEmpty()) {
            log.debug("{} Password updated for User ID={}", LoggingConstants.ADMIN_PANEL, userId);
            user.setPassword(passwordEncoder.encode(userFormDTO.getPassword()));
        }

        user.setFullName(userFormDTO.getFullName());
        user.setAge(userFormDTO.getAge());
        user.setMobileNumber(userFormDTO.getMobileNumber());
        user.setTotalPoints(userFormDTO.getTotalPoints() != null ? userFormDTO.getTotalPoints() : 0);
        user.setEventCount(userFormDTO.getEventCount() != null ? userFormDTO.getEventCount() : 0);
        user.setWorkingHours(userFormDTO.getWorkingHours() != null ? userFormDTO.getWorkingHours() : 0);
        user.setAboutMe(userFormDTO.getAboutMe());
        user.setRole(userFormDTO.getRole() != null ? userFormDTO.getRole() : Role.USER);

        User updatedUser = userRepository.save(user);

        log.info("{} Successfully updated User ID={} - Email: {}",
                LoggingConstants.ADMIN_PANEL, updatedUser.getId(), updatedUser.getEmail());

        return updatedUser;
    }

    /**
     * Delete user
     */
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("{} {} attempt failed - User ID={} not found",
                            LoggingConstants.ADMIN_PANEL,
                            LoggingConstants.DELETE,
                            userId);
                    return new UserNotFoundException("User not found with id: " + userId);
                });

        log.info("{} {} {} ID={} - Email: {}",
                LoggingConstants.ADMIN_PANEL,
                LoggingConstants.DELETE,
                LoggingConstants.USER_ENTITY,
                userId,
                user.getEmail());

        userRepository.delete(user);

        log.info("{} Successfully deleted User ID={}", LoggingConstants.ADMIN_PANEL, userId);
    }

    /**
     * Convert User entity to UserManagementDTO
     */
    private UserManagementDTO convertToUserManagementDTO(User user) {
        return userMapper.toManagementDTO(user);
    }
}
