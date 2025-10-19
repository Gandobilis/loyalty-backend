package com.multi.loyaltybackend.voucher.controller;

import com.multi.loyaltybackend.model.VoucherStatus;
import com.multi.loyaltybackend.voucher.model.UserVoucher;
import com.multi.loyaltybackend.voucher.service.UserVoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/user-vouchers")
@RequiredArgsConstructor
public class UserVoucherController {
    private final UserVoucherService userVoucherService;

    @GetMapping
    public ResponseEntity<List<UserVoucher>> getAllUserVouchers() {
        return ResponseEntity.ok(userVoucherService.getAllUserVouchers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserVoucher> getUserVoucherById(@PathVariable Long id) {
        return userVoucherService.getUserVoucherById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<UserVoucher>> getUserVouchersByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(userVoucherService.getUserVouchersByUserId(userId));
    }

    @GetMapping("/user/{userId}/status/{status}")
    public ResponseEntity<List<UserVoucher>> getUserVouchersByUserIdAndStatus(
            @PathVariable Long userId,
            @PathVariable VoucherStatus status) {
        return ResponseEntity.ok(userVoucherService.getUserVouchersByUserIdAndStatus(userId, status));
    }

    @PostMapping
    public ResponseEntity<UserVoucher> assignVoucherToUser(@RequestBody UserVoucher userVoucher) {
        UserVoucher created = userVoucherService.assignVoucherToUser(userVoucher);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}/redeem")
    public ResponseEntity<UserVoucher> redeemVoucher(@PathVariable Long id) {
        try {
            UserVoucher redeemed = userVoucherService.redeemVoucher(id);
            return ResponseEntity.ok(redeemed);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUserVoucher(@PathVariable Long id) {
        userVoucherService.deleteUserVoucher(id);
        return ResponseEntity.noContent().build();
    }
}