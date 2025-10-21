package com.multi.loyaltybackend.voucher.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import com.multi.loyaltybackend.company.model.Company;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "company")
@EqualsAndHashCode(exclude = "company")
public class Voucher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotNull(message = "Title is required")
    private String title;

    @Column(nullable = false)
    @NotNull(message = "Description is required")
    private String description;

    @Column(nullable = false)
    @NotNull(message = "points is required")
    private Integer points;

    @Column(nullable = false)
    @NotNull(message = "expiry is required")
    private LocalDateTime expiry;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    @NotNull(message = "Company is required")
    private Company company;
}