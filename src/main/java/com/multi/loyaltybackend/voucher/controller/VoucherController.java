package com.multi.loyaltybackend.voucher.controller;

import com.multi.loyaltybackend.voucher.dto.UserVoucherRequest;
import com.multi.loyaltybackend.voucher.dto.VoucherRequest;
import com.multi.loyaltybackend.voucher.model.UserVoucher;
import com.multi.loyaltybackend.voucher.model.Voucher;
import com.multi.loyaltybackend.voucher.service.VoucherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vouchers")
@RequiredArgsConstructor
public class VoucherController {
    private final VoucherService voucherService;

    @GetMapping
    public ResponseEntity<List<Voucher>> getAllVouchers() {
        return ResponseEntity.ok(voucherService.getAllVouchers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Voucher> getVoucherById(@PathVariable Long id) {
        return voucherService.getVoucherById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Voucher> createVoucher(@Valid @RequestBody VoucherRequest request) {
        Voucher created = voucherService.createVoucher(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Voucher> updateVoucher(@PathVariable Long id, @RequestBody Voucher voucher) {
        try {
            Voucher updated = voucherService.updateVoucher(id, voucher);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVoucher(@PathVariable Long id) {
        voucherService.deleteVoucher(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/exchange")
    public ResponseEntity<UserVoucher> exchangeVoucher(@Valid @RequestBody UserVoucherRequest request) {
        UserVoucher created = voucherService.exchangeVoucher(
                request.getUserId(),
                request.getVoucherId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
