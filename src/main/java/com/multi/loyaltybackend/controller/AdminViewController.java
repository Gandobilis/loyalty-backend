package com.multi.loyaltybackend.controller;

import com.multi.loyaltybackend.company.model.Company;
import com.multi.loyaltybackend.company.service.CompanyService;
import com.multi.loyaltybackend.dto.UserFormDTO;
import com.multi.loyaltybackend.model.Role;
import com.multi.loyaltybackend.model.User;
import com.multi.loyaltybackend.service.AdminService;
import com.multi.loyaltybackend.voucher.dto.VoucherRequest;
import com.multi.loyaltybackend.voucher.model.Voucher;
import com.multi.loyaltybackend.voucher.service.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminViewController {

    private final AdminService adminService;
    private final CompanyService companyService;
    private final VoucherService voucherService;

    /**
     * Admin Dashboard - Main page with statistics
     */
    @GetMapping("")
    public String dashboard(Model model) {
        model.addAttribute("stats", adminService.getDashboardStats());
        return "admin/dashboard";
    }

    /**
     * Company Management Pages
     */
    @GetMapping("/companies")
    public String listCompanies(Model model) {
        model.addAttribute("companies", companyService.getAllCompanies());
        return "admin/companies/list";
    }

    @GetMapping("/companies/new")
    public String newCompanyForm(Model model) {
        model.addAttribute("company", new Company());
        return "admin/companies/form";
    }

    @PostMapping("/companies/new")
    public String createCompany(
            @ModelAttribute Company company,
            @RequestParam(value = "logoFile", required = false) MultipartFile logoFile,
            RedirectAttributes redirectAttributes) {
        try {
            companyService.createCompany(company, logoFile);
            redirectAttributes.addFlashAttribute("successMessage", "Company created successfully!");
            return "redirect:/admin/companies";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error creating company: " + e.getMessage());
            return "redirect:/admin/companies/new";
        }
    }

    @GetMapping("/companies/edit/{id}")
    public String editCompanyForm(@PathVariable Long id, Model model) {
        companyService.getCompanyById(id).ifPresentOrElse(
                company -> model.addAttribute("company", company),
                () -> model.addAttribute("errorMessage", "Company not found")
        );
        return "admin/companies/form";
    }

    @PostMapping("/companies/edit/{id}")
    public String updateCompany(
            @PathVariable Long id,
            @ModelAttribute Company company,
            @RequestParam(value = "logoFile", required = false) MultipartFile logoFile,
            RedirectAttributes redirectAttributes) {
        try {
            companyService.updateCompany(id, company, logoFile);
            redirectAttributes.addFlashAttribute("successMessage", "Company updated successfully!");
            return "redirect:/admin/companies";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating company: " + e.getMessage());
            return "redirect:/admin/companies/edit/" + id;
        }
    }

    @PostMapping("/companies/delete/{id}")
    public String deleteCompany(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            companyService.deleteCompany(id);
            redirectAttributes.addFlashAttribute("successMessage", "Company deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting company: " + e.getMessage());
        }
        return "redirect:/admin/companies";
    }

    /**
     * Voucher Management Pages
     */
    @GetMapping("/vouchers")
    public String listVouchers(Model model) {
        model.addAttribute("vouchers", voucherService.getAllVouchers());
        return "admin/vouchers/list";
    }

    @GetMapping("/vouchers/new")
    public String newVoucherForm(Model model) {
        model.addAttribute("voucher", new com.multi.loyaltybackend.voucher.dto.VoucherFormDTO());
        model.addAttribute("companies", companyService.getAllCompanies());
        return "admin/vouchers/form";
    }

    @PostMapping("/vouchers/new")
    public String createVoucher(@ModelAttribute com.multi.loyaltybackend.voucher.dto.VoucherFormDTO voucherForm, RedirectAttributes redirectAttributes) {
        try {
            VoucherRequest voucherRequest = new VoucherRequest(
                    voucherForm.getTitle(),
                    voucherForm.getDescription(),
                    voucherForm.getPoints(),
                    voucherForm.getExpiry(),
                    voucherForm.getCompanyId()
            );
            voucherService.createVoucher(voucherRequest);
            redirectAttributes.addFlashAttribute("successMessage", "Voucher created successfully!");
            return "redirect:/admin/vouchers";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error creating voucher: " + e.getMessage());
            return "redirect:/admin/vouchers/new";
        }
    }

    @GetMapping("/vouchers/edit/{id}")
    public String editVoucherForm(@PathVariable Long id, Model model) {
        voucherService.getVoucherById(id).ifPresentOrElse(
                voucher -> {
                    com.multi.loyaltybackend.voucher.dto.VoucherFormDTO formDTO = com.multi.loyaltybackend.voucher.dto.VoucherFormDTO.builder()
                            .id(voucher.getId())
                            .title(voucher.getTitle())
                            .description(voucher.getDescription())
                            .points(voucher.getPoints())
                            .expiry(voucher.getExpiry())
                            .companyId(voucher.getCompanyId())
                            .build();
                    model.addAttribute("voucher", formDTO);
                    model.addAttribute("companies", companyService.getAllCompanies());
                },
                () -> model.addAttribute("errorMessage", "Voucher not found")
        );
        return "admin/vouchers/form";
    }

    @PostMapping("/vouchers/edit/{id}")
    public String updateVoucher(
            @PathVariable Long id,
            @ModelAttribute com.multi.loyaltybackend.voucher.dto.VoucherFormDTO voucherForm,
            RedirectAttributes redirectAttributes) {
        try {
            Voucher voucher = new Voucher();
            voucher.setTitle(voucherForm.getTitle());
            voucher.setDescription(voucherForm.getDescription());
            voucher.setPoints(voucherForm.getPoints());
            voucher.setExpiry(voucherForm.getExpiry());
            voucherService.updateVoucher(id, voucher);
            redirectAttributes.addFlashAttribute("successMessage", "Voucher updated successfully!");
            return "redirect:/admin/vouchers";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating voucher: " + e.getMessage());
            return "redirect:/admin/vouchers/edit/" + id;
        }
    }

    @PostMapping("/vouchers/delete/{id}")
    public String deleteVoucher(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            voucherService.deleteVoucher(id);
            redirectAttributes.addFlashAttribute("successMessage", "Voucher deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting voucher: " + e.getMessage());
        }
        return "redirect:/admin/vouchers";
    }

    /**
     * User Management Pages
     */
    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("users", adminService.getAllUsers());
        return "admin/users/list";
    }

    @GetMapping("/users/new")
    public String newUserForm(Model model) {
        model.addAttribute("user", new UserFormDTO());
        return "admin/users/form";
    }

    @PostMapping("/users/new")
    public String createUser(@ModelAttribute UserFormDTO userForm, RedirectAttributes redirectAttributes) {
        try {
            adminService.createUser(userForm);
            redirectAttributes.addFlashAttribute("successMessage", "User created successfully!");
            return "redirect:/admin/users";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error creating user: " + e.getMessage());
            return "redirect:/admin/users/new";
        }
    }

    @GetMapping("/users/edit/{id}")
    public String editUserForm(@PathVariable Long id, Model model) {
        try {
            User user = adminService.getUserById(id) != null
                    ? convertToUser(adminService.getUserById(id))
                    : null;
            if (user == null) {
                model.addAttribute("errorMessage", "User not found");
                return "admin/users/list";
            }
            UserFormDTO formDTO = UserFormDTO.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .fullName(user.getFullName())
                    .age(user.getAge())
                    .mobileNumber(user.getMobileNumber())
                    .totalPoints(user.getTotalPoints())
                    .eventCount(user.getEventCount())
                    .workingHours(user.getWorkingHours())
                    .aboutMe(user.getAboutMe())
                    .role(user.getRole())
                    .build();
            model.addAttribute("user", formDTO);
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error loading user: " + e.getMessage());
        }
        return "admin/users/form";
    }

    @PostMapping("/users/edit/{id}")
    public String updateUser(
            @PathVariable Long id,
            @ModelAttribute UserFormDTO userForm,
            RedirectAttributes redirectAttributes) {
        try {
            adminService.updateUser(id, userForm);
            redirectAttributes.addFlashAttribute("successMessage", "User updated successfully!");
            return "redirect:/admin/users";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating user: " + e.getMessage());
            return "redirect:/admin/users/edit/" + id;
        }
    }

    @PostMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            adminService.deleteUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "User deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting user: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    /**
     * Helper method to convert UserManagementDTO to User for form editing
     */
    private User convertToUser(com.multi.loyaltybackend.dto.UserManagementDTO userDTO) {
        return User.builder()
                .id(userDTO.getId())
                .email(userDTO.getEmail())
                .fullName(userDTO.getFullName())
                .age(userDTO.getAge())
                .mobileNumber(userDTO.getMobileNumber())
                .totalPoints(userDTO.getTotalPoints())
                .eventCount(userDTO.getEventCount())
                .workingHours(userDTO.getWorkingHours())
                .role(userDTO.getRole())
                .build();
    }
}
