package com.multi.loyaltybackend.company.service;

import java.util.List;
import java.util.Optional;

import com.multi.loyaltybackend.company.model.Company;
import com.multi.loyaltybackend.company.repository.CompanyRepository;
import com.multi.loyaltybackend.service.ImageStorageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CompanyService {
    private final ImageStorageService imageStorageService;
    private final CompanyRepository companyRepository;

    public List<Company> getAllCompanies() {
        List<Company> companies = companyRepository.findAll();
        companies.forEach(company -> {
            if (company.getLogoFileName() != null) {
                company.setLogoFileName(imageStorageService.getFilePath(company.getLogoFileName()));
            }
        });
        return companies;
    }

    public Optional<Company> getCompanyById(Long id) {
        return companyRepository.findById(id);
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
            throw new RuntimeException("Failed to create company", e);
        }
    }

    @Transactional
    public Company updateCompany(Long id, Company companyDetails, MultipartFile file) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Company not found with id: " + id));
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
            throw new RuntimeException("Failed to update company", e);
        }
    }

    @Transactional
    public void deleteCompany(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Company not found with id: " + id));
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
}