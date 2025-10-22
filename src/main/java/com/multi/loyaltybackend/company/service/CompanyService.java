package com.multi.loyaltybackend.company.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.multi.loyaltybackend.company.dto.CompanyFilterDTO;
import com.multi.loyaltybackend.company.dto.CompanyResponseDTO;
import com.multi.loyaltybackend.company.model.Company;
import com.multi.loyaltybackend.company.repository.CompanyRepository;
import com.multi.loyaltybackend.company.specification.CompanySpecifications;
import com.multi.loyaltybackend.exception.CompanyNotFoundException;
import com.multi.loyaltybackend.exception.FileStorageException;
import com.multi.loyaltybackend.service.ImageStorageService;
import com.multi.loyaltybackend.voucher.dto.VoucherDTO;
import com.multi.loyaltybackend.voucher.model.UserVoucher;
import com.multi.loyaltybackend.voucher.model.Voucher;
import com.multi.loyaltybackend.voucher.repository.UserVoucherRepository;
import com.multi.loyaltybackend.voucher.repository.VoucherRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CompanyService {
    private final ImageStorageService imageStorageService;
    private final CompanyRepository companyRepository;
    private final VoucherRepository voucherRepository;

    public Page<Company> getAllCompanies(Pageable pageable) {
        Page<Company> companies = companyRepository.findAll(pageable);
        companies.forEach(company -> {
            if (company.getLogoFileName() != null) {
                company.setLogoFileName(imageStorageService.getFilePath(company.getLogoFileName()));
            }
        });
        return companies;
    }

    public List<CompanyResponseDTO> getAllCompanies() {
        List<Company> companies = companyRepository.findAll();

        Set<Long> companyIds = companies.stream()
                .map(Company::getId)
                .collect(Collectors.toSet());

        Map<Long, List<Voucher>> vouchersByCompany = voucherRepository
                .findByCompanyIdIn(companyIds)
                .stream()
                .collect(Collectors.groupingBy(v -> v.getCompany().getId()));

        return companies.stream()
                .map(company -> mapToDTO(company, vouchersByCompany.get(company.getId())))
                .toList();
    }

    /**
     * Get filtered companies with pagination
     */
    public Page<CompanyResponseDTO> getFilteredCompanies(CompanyFilterDTO filter, Pageable pageable) {
        Specification<Company> spec = Specification.where(null);

        if (filter != null && filter.getName() != null && !filter.getName().isEmpty()) {
            spec = spec.and(CompanySpecifications.nameContains(filter.getName()));
        }

        Page<Company> companies = companyRepository.findAll(spec, pageable);

        // Get all company IDs from the page
        Set<Long> companyIds = companies.getContent().stream()
                .map(Company::getId)
                .collect(Collectors.toSet());

        // Fetch all vouchers for these companies
        Map<Long, List<Voucher>> vouchersByCompany = !companyIds.isEmpty()
                ? voucherRepository.findByCompanyIdIn(companyIds)
                    .stream()
                    .collect(Collectors.groupingBy(v -> v.getCompany().getId()))
                : Map.of();

        return companies.map(company -> mapToDTO(company, vouchersByCompany.get(company.getId())));
    }

    public Optional<CompanyResponseDTO> getCompanyById(Long id) {
        return companyRepository.findById(id)
                .map(company -> {
                    List<Voucher> vouchers = voucherRepository.findByCompanyId(company.getId());
                    return mapToDTO(company, vouchers);
                });
    }

    @Transactional
    public Company createCompany(Company company, MultipartFile file) {
        String fileName = null;
        try {
            if (file != null && !file.isEmpty()) {
                fileName = imageStorageService.storeFile(file);
                company.setLogoFileName(fileName);
            }
            return companyRepository.save(company);
        } catch (Exception e) {
            if (fileName != null) {
                imageStorageService.deleteFile(fileName);
            }
            throw new FileStorageException("Failed to create company", e);
        }
    }

    @Transactional
    public Company updateCompany(Long id, Company companyDetails, MultipartFile file) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new CompanyNotFoundException(id));
        String oldFileName = company.getLogoFileName();
        String newFileName = null;
        try {
            if (file != null && !file.isEmpty()) {
                newFileName = imageStorageService.storeFile(file);
                company.setLogoFileName(newFileName);
            }
            company.setName(companyDetails.getName());
            Company savedCompany = companyRepository.save(company);
            if (newFileName != null && oldFileName != null) {
                imageStorageService.deleteFile(oldFileName);
            }
            return savedCompany;
        } catch (Exception e) {
            if (newFileName != null) {
                imageStorageService.deleteFile(newFileName);
            }
            throw new FileStorageException("Failed to update company", e);
        }
    }

    @Transactional
    public void deleteCompany(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new CompanyNotFoundException(id));
        String logoFileName = company.getLogoFileName();
        companyRepository.delete(company);
        if (logoFileName != null) {
            try {
                imageStorageService.deleteFile(logoFileName);
            } catch (Exception e) {
                // Log the error but don't fail the operation
                // File can be cleaned up later
            }
        }
    }

    private CompanyResponseDTO mapToDTO(Company company, List<Voucher> vouchers) {
        String logoPath = company.getLogoFileName() != null
                ? imageStorageService.getFilePath(company.getLogoFileName())
                : null;

        List<VoucherDTO> voucherDTOs = vouchers == null ? List.of() :
                vouchers.stream()
                        .map(this::mapVoucherToDTO)
                        .toList();

        return CompanyResponseDTO.builder()
                .id(company.getId())
                .name(company.getName())
                .logoFileName(logoPath)
                .vouchers(voucherDTOs)
                .build();
    }

    private VoucherDTO mapVoucherToDTO(Voucher voucher) {
        return VoucherDTO.builder()
                .id(voucher.getId())
                .title(voucher.getTitle())
                .description(voucher.getDescription())
                .expiry(voucher.getExpiry())
                .points(voucher.getPoints())
                .build();
    }
}