package com.multi.loyaltybackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatsDTO {
    private Long totalUsers;
    private Long totalAdmins;
    private Long totalCompanies;
    private Long totalVouchers;
    private Long totalEvents;
    private Long totalRegistrations;
    private Long activeVouchers;
    private Long expiredVouchers;
    private Long totalPointsDistributed;
    private Long totalVouchersExchanged;
}
