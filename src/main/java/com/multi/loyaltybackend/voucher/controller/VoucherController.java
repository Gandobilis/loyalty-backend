package com.multi.loyaltybackend.voucher.controller;

import com.multi.loyaltybackend.voucher.dto.UserVoucherRequest;
import com.multi.loyaltybackend.voucher.dto.VoucherRequest;
import com.multi.loyaltybackend.voucher.dto.VoucherWithCompanyDTO;
import com.multi.loyaltybackend.voucher.model.UserVoucher;
import com.multi.loyaltybackend.voucher.model.Voucher;
import com.multi.loyaltybackend.voucher.service.VoucherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vouchers")
@RequiredArgsConstructor
public class VoucherController {
    private final VoucherService voucherService;

    @GetMapping
    public ResponseEntity<List<VoucherWithCompanyDTO>> getAllVouchers() {
        return ResponseEntity.ok(voucherService.getAllVouchers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VoucherWithCompanyDTO> getVoucherById(@PathVariable Long id) {
        return voucherService.getVoucherById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Voucher> createVoucher(@Valid @RequestBody VoucherRequest request) {
        Voucher created = voucherService.createVoucher(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Voucher> updateVoucher(@PathVariable Long id, @RequestBody Voucher voucher) {
        try {
            Voucher updated = voucherService.updateVoucher(id, voucher);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteVoucher(@PathVariable Long id) {
        voucherService.deleteVoucher(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/exchange")
    public ResponseEntity<UserVoucher> exchangeVoucher(
            Authentication authentication,
            @Valid @RequestBody UserVoucherRequest request) {
        UserVoucher created = voucherService.exchangeVoucher(
                authentication.getName(),
                request.getVoucherId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/redeem")
    public ResponseEntity<UserVoucher> redeemVoucher(
            Authentication authentication,
            @Valid @RequestBody UserVoucherRequest request) {
        UserVoucher created = voucherService.redeemVoucher(
                authentication.getName(),
                request.getVoucherId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
