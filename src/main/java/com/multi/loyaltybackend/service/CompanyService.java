package com.multi.loyaltybackend.service;

import com.multi.loyaltybackend.model.Company;
import com.multi.loyaltybackend.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CompanyService {
    private final CompanyRepository companyRepository;

    public List<Company> getAllCompanies() {
        return companyRepository.findAll();
    }

    public Optional<Company> getCompanyById(Long id) {
        return companyRepository.findById(id);
    }

    @Transactional
    public Company createCompany(Company company) {
        return companyRepository.save(company);
    }

    @Transactional
    public Company updateCompany(Long id, Company companyDetails) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        company.setName(companyDetails.getName());
        company.setFileName(companyDetails.getFileName());
        company.setDescription(companyDetails.getDescription());

        return companyRepository.save(company);
    }

    @Transactional
    public void deleteCompany(Long id) {
        companyRepository.deleteById(id);
    }
}