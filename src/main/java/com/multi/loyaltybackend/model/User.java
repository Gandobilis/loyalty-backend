package com.multi.loyaltybackend.model;

import com.multi.loyaltybackend.voucher.model.UserVoucher;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
@Builder
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role = Role.USER;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Email cannot exceed 255 characters")
    @Column(unique = true, nullable = false)
    private String email;

    @Size(min = 8, max = 255, message = "Password must be between 8 and 255 characters")
    @Column(nullable = true)
    private String password;

    private String passwordResetToken;

    private LocalDateTime passwordResetTokenExpiry;

    private String fileName;

    @Builder.Default
    @Min(value = 0, message = "Total points cannot be negative")
    @Column(nullable = false)
    private Integer totalPoints = 0;

    @Builder.Default
    @Min(value = 0, message = "Event count cannot be negative")
    @Column(nullable = false)
    private Integer eventCount = 0;

    @Builder.Default
    @Min(value = 0, message = "Working hours cannot be negative")
    @Column(nullable = false)
    private Integer workingHours = 0;

    @Size(max = 2000, message = "About me cannot exceed 2000 characters")
    private String aboutMe;

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    @Column(nullable = false, length = 100)
    private String fullName;

    @Min(value = 0, message = "Age must be at least 0")
    @Max(value = 150, message = "Age cannot exceed 150")
    private Integer age;

    @Pattern(
            regexp = "^\\+?[1-9]\\d{1,14}$",
            message = "Invalid mobile number format. Must be E.164 format"
    )
    @Column(length = 15, unique = true)
    private String mobileNumber;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.role.name()));
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Registration> registrations = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserVoucher> userVouchers = new ArrayList<>();

    public void incrementPoints(int points) {
        if (points < 0) {
            throw new IllegalArgumentException("Points increment must be non-negative");
        }
        this.totalPoints += points;
    }

    public void incrementEventCount() {
        this.eventCount++;
    }

    public void addWorkingHours(int hours) {
        if (hours < 0) {
            throw new IllegalArgumentException("Working hours must be non-negative");
        }
        this.workingHours += hours;
    }
}