package com.multi.loyaltybackend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UserVouchersWithPointsResponse {
    private Integer points;
    private List<UserVoucherResponse> vouchers;
}
