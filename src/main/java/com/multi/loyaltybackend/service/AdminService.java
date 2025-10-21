package com.multi.loyaltybackend.service;

import com.multi.loyaltybackend.company.repository.CompanyRepository;
import com.multi.loyaltybackend.dto.DashboardStatsDTO;
import com.multi.loyaltybackend.dto.UserManagementDTO;
import com.multi.loyaltybackend.exception.UserNotFoundException;
import com.multi.loyaltybackend.model.Role;
import com.multi.loyaltybackend.model.User;
import com.multi.loyaltybackend.repository.EventRepository;
import com.multi.loyaltybackend.repository.RegistrationRepository;
import com.multi.loyaltybackend.repository.UserRepository;
import com.multi.loyaltybackend.voucher.repository.UserVoucherRepository;
import com.multi.loyaltybackend.voucher.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final VoucherRepository voucherRepository;
    private final EventRepository eventRepository;
    private final RegistrationRepository registrationRepository;
    private final UserVoucherRepository userVoucherRepository;

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
        user.setRole(newRole);
        User updatedUser = userRepository.save(user);
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

        user.setTotalPoints(newPoints);
        User updatedUser = userRepository.save(user);
        return convertToUserManagementDTO(updatedUser);
    }

    /**
     * Delete user
     */
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));
        userRepository.delete(user);
    }

    /**
     * Convert User entity to UserManagementDTO
     */
    private UserManagementDTO convertToUserManagementDTO(User user) {
        return UserManagementDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .totalPoints(user.getTotalPoints())
                .eventCount(user.getEventCount())
                .workingHours(user.getWorkingHours())
                .mobileNumber(user.getMobileNumber())
                .age(user.getAge())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
