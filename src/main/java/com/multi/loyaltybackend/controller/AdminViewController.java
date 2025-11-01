package com.multi.loyaltybackend.controller;

import com.multi.loyaltybackend.dto.CompanyFilterDTO;
import com.multi.loyaltybackend.dto.CompanyResponseDTO;
import com.multi.loyaltybackend.model.Company;
import com.multi.loyaltybackend.service.CompanyService;
import com.multi.loyaltybackend.config.LoggingConstants;
import com.multi.loyaltybackend.dto.*;
import com.multi.loyaltybackend.faq.dto.FAQFilterDTO;
import com.multi.loyaltybackend.faq.dto.FAQRequestDTO;
import com.multi.loyaltybackend.faq.dto.FAQResponseDTO;
import com.multi.loyaltybackend.faq.service.FAQService;
import com.multi.loyaltybackend.model.*;
import com.multi.loyaltybackend.repository.EventRepository;
import com.multi.loyaltybackend.service.AdminService;
import com.multi.loyaltybackend.service.EventService;
import com.multi.loyaltybackend.service.ImageStorageService;
import com.multi.loyaltybackend.service.RegistrationManagementService;
import com.multi.loyaltybackend.dto.VoucherFilterDTO;
import com.multi.loyaltybackend.dto.VoucherRequest;
import com.multi.loyaltybackend.model.Voucher;
import com.multi.loyaltybackend.service.VoucherService;
import com.multi.loyaltybackend.service.SupportMessageService;
import com.multi.loyaltybackend.dto.SupportMessageFilterDTO;
import com.multi.loyaltybackend.dto.SupportMessageResponse;
import com.multi.loyaltybackend.dto.RespondToSupportMessageRequest;
import com.multi.loyaltybackend.model.SupportMessageStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminViewController {

    private final AdminService adminService;
    private final CompanyService companyService;
    private final VoucherService voucherService;
    private final EventService eventService;
    private final EventRepository eventRepository;
    private final ImageStorageService imageStorageService;
    private final RegistrationManagementService registrationManagementService;
    private final FAQService faqService;
    private final SupportMessageService supportMessageService;



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
    public String listCompanies(
            @ModelAttribute CompanyFilterDTO filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction,
            Model model) {
        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        Page<?> companiesPage = companyService.getFilteredCompanies(filter, pageable);

        model.addAttribute("companies", companiesPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", companiesPage.getTotalPages());
        model.addAttribute("totalItems", companiesPage.getTotalElements());
        model.addAttribute("filter", filter);
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
            System.out.println(logoFile.getName());
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
    public String listVouchers(
            @ModelAttribute VoucherFilterDTO filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction,
            Model model) {
        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        Page<?> vouchersPage = voucherService.getFilteredVouchers(filter, pageable);

        model.addAttribute("vouchers", vouchersPage.getContent());
        model.addAttribute("companies", companyService.getAllCompanies());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", vouchersPage.getTotalPages());
        model.addAttribute("totalItems", vouchersPage.getTotalElements());
        model.addAttribute("filter", filter);
        return "admin/vouchers/list";
    }

    @GetMapping("/vouchers/new")
    public String newVoucherForm(Model model) {
        model.addAttribute("voucher", new VoucherFormDTO());
        model.addAttribute("companies", companyService.getAllCompanies());
        return "admin/vouchers/form";
    }

    @PostMapping("/vouchers/new")
    public String createVoucher(@ModelAttribute VoucherFormDTO voucherForm, RedirectAttributes redirectAttributes) {
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
                    VoucherFormDTO formDTO = VoucherFormDTO.builder()
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
            @ModelAttribute VoucherFormDTO voucherForm,
            RedirectAttributes redirectAttributes) {
        try {
            Voucher voucher = new Voucher();
            voucher.setTitle(voucherForm.getTitle());
            voucher.setPoints(voucherForm.getPoints());
            voucher.setExpiry(voucherForm.getExpiry());
            CompanyResponseDTO company = companyService.getCompanyById(voucherForm.getCompanyId())
                    .orElseThrow(
                            () -> new RuntimeException("Company not found with ID: " + voucherForm.getCompanyId()
                            )
                    );

            Company com = Company.builder()
                    .id(company.getId())
                    .name(company.getName())
                    .logoFileName(company.getLogoFileName())
                    .build();
            voucher.setCompany(com);
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
    public String listUsers(
            @ModelAttribute UserFilterDTO filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String direction,
            Model model) {
        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        Page<?> usersPage = adminService.getFilteredUsers(filter, pageable);
        model.addAttribute("users", usersPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", usersPage.getTotalPages());
        model.addAttribute("totalItems", usersPage.getTotalElements());
        model.addAttribute("filter", filter);
        return "admin/users/list";
    }

    @GetMapping("/users/new")
    public String newUserForm(Model model) {
        model.addAttribute("user", new UserFormDTO());
        return "admin/users/form";
    }

    @PostMapping("/users/new")
    public String createUser(
            @Valid @ModelAttribute("user") UserFormDTO userForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        // Custom validation: password is required for new users
        if (userForm.getPassword() == null || userForm.getPassword().trim().isEmpty()) {
            bindingResult.rejectValue("password", "error.password", "Password is required for new users");
        }

        // Check for validation errors
        if (bindingResult.hasErrors()) {
            model.addAttribute("user", userForm);
            model.addAttribute("errorMessage", "Please correct the validation errors.");
            return "admin/users/form";
        }

        try {
            adminService.createUser(userForm);
            redirectAttributes.addFlashAttribute("successMessage", "User created successfully!");
            return "redirect:/admin/users";
        } catch (Exception e) {
            model.addAttribute("user", userForm);
            model.addAttribute("errorMessage", "Error creating user: " + e.getMessage());
            return "admin/users/form";
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
            @Valid @ModelAttribute("user") UserFormDTO userForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            // Stay on the same page and show validation errors
            userForm.setId(id); // Ensure ID is set for the form
            model.addAttribute("user", userForm);
            model.addAttribute("errorMessage", "Please correct the validation errors.");
            return "admin/users/form";
        }

        try {
            adminService.updateUser(id, userForm);
            redirectAttributes.addFlashAttribute("successMessage", "User updated successfully!");
            return "redirect:/admin/users";
        } catch (Exception e) {
            e.printStackTrace();
            userForm.setId(id); // Ensure ID is set for the form
            model.addAttribute("user", userForm);
            model.addAttribute("errorMessage", "Error updating user: " + e.getMessage());
            return "admin/users/form";
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
                .aboutMe(userDTO.getAboutMe())
                .build();
    }

    /**
     * Event Management Pages
     */
    @GetMapping("/events")
    public String listEvents(
            @ModelAttribute EventFilterDTO filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "desc") String direction,
            Model model) {
        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        Page<Event> eventsPage = eventService.getFilteredEvents(filter, pageable);

        model.addAttribute("events",eventsPage.getContent());
        model.addAttribute("categories", EventCategory.values());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", eventsPage.getTotalPages());
        model.addAttribute("totalItems", eventsPage.getTotalElements());
        model.addAttribute("filter", filter);
        return "admin/events/list";
    }

    @GetMapping("/events/new")
    public String newEventForm(Model model) {
        model.addAttribute("event", new EventFormDTO());
        model.addAttribute("categories", EventCategory.values());
        return "admin/events/form";
    }

    @PostMapping("/events/new")
    public String createEvent(
            @ModelAttribute EventFormDTO eventForm,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            RedirectAttributes redirectAttributes) {
        try {
            log.info("{} {} {} - Title: {}, Category: {}",
                    LoggingConstants.ADMIN_PANEL,
                    LoggingConstants.CREATE,
                    LoggingConstants.EVENT_ENTITY,
                    eventForm.getTitle(),
                    eventForm.getCategory());

            Event event = Event.builder()
                    .title(eventForm.getTitle())
                    .shortDescription(eventForm.getShortDescription())
                    .description(eventForm.getDescription())
                    .category(EventCategory.valueOf(eventForm.getCategory()))
                    .address(eventForm.getAddress())
                    .latitude(eventForm.getLatitude())
                    .longitude(eventForm.getLongitude())
                    .dateTime(eventForm.getDateTime())
                    .points(eventForm.getPoints() != null ? eventForm.getPoints() : 0)
                    .build();

            // Handle image upload
            if (imageFile != null && !imageFile.isEmpty()) {
                String fileName = imageStorageService.storeFile(imageFile);
                event.setFileName(fileName);
            }

            eventRepository.save(event);

            log.info("{} Successfully created Event ID={} - Title: {}",
                    LoggingConstants.ADMIN_PANEL, event.getId(), event.getTitle());

            redirectAttributes.addFlashAttribute("successMessage", "Event created successfully!");
            return "redirect:/admin/events";
        } catch (Exception e) {
            log.error("{} {} failed - Error: {}",
                    LoggingConstants.ADMIN_PANEL,
                    LoggingConstants.CREATE,
                    e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Error creating event: " + e.getMessage());
            return "redirect:/admin/events/new";
        }
    }

    @GetMapping("/events/edit/{id}")
    public String editEventForm(@PathVariable Long id, Model model) {
        try {
            Event event = eventRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Event not found"));
            System.out.println(event.getDateTime());
            EventFormDTO formDTO = EventFormDTO.builder()
                    .id(event.getId())
                    .title(event.getTitle())
                    .shortDescription(event.getShortDescription())
                    .description(event.getDescription())
                    .category(event.getCategory().name())
                    .address(event.getAddress())
                    .latitude(event.getLatitude())
                    .longitude(event.getLongitude())
                    .dateTime(event.getDateTime())
                    .points(event.getPoints())
                    .fileName(event.getFileName() != null
                        ? imageStorageService.getFilePath(event.getFileName())
                        : null)
                    .build();

            model.addAttribute("event", formDTO);
            model.addAttribute("categories", EventCategory.values());
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error loading event: " + e.getMessage());
        }
        return "admin/events/form";
    }

    @PostMapping("/events/edit/{id}")
    public String updateEvent(
            @PathVariable Long id,
            @ModelAttribute EventFormDTO eventForm,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            RedirectAttributes redirectAttributes) {
        try {
            log.info("{} {} {} ID={} - Title: {}, Category: {}",
                    LoggingConstants.ADMIN_PANEL,
                    LoggingConstants.UPDATE,
                    LoggingConstants.EVENT_ENTITY,
                    id,
                    eventForm.getTitle(),
                    eventForm.getCategory());

            Event event = eventRepository.findById(id)
                    .orElseThrow(() -> {
                        log.warn("{} {} attempt failed - Event ID={} not found",
                                LoggingConstants.ADMIN_PANEL,
                                LoggingConstants.UPDATE,
                                id);
                        return new RuntimeException("Event not found");
                    });

            event.setTitle(eventForm.getTitle());
            event.setShortDescription(eventForm.getShortDescription());
            event.setDescription(eventForm.getDescription());
            event.setCategory(EventCategory.valueOf(eventForm.getCategory()));
            event.setAddress(eventForm.getAddress());
            event.setLatitude(eventForm.getLatitude());
            event.setLongitude(eventForm.getLongitude());
            event.setDateTime(eventForm.getDateTime());
            event.setPoints(eventForm.getPoints() != null ? eventForm.getPoints() : 0);

            // Handle image upload
            if (imageFile != null && !imageFile.isEmpty()) {
                // Delete old image if exists
                if (event.getFileName() != null) {
                    imageStorageService.deleteFile(event.getFileName());
                }
                // Save new image
                String fileName = imageStorageService.storeFile(imageFile);
                event.setFileName(fileName);
            }

            eventRepository.save(event);

            log.info("{} Successfully updated Event ID={} - Title: {}",
                    LoggingConstants.ADMIN_PANEL, event.getId(), event.getTitle());

            redirectAttributes.addFlashAttribute("successMessage", "Event updated successfully!");
            return "redirect:/admin/events";
        } catch (Exception e) {
            log.error("{} {} failed - Error: {}",
                    LoggingConstants.ADMIN_PANEL,
                    LoggingConstants.UPDATE,
                    e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating event: " + e.getMessage());
            return "redirect:/admin/events/edit/" + id;
        }
    }

    @PostMapping("/events/delete/{id}")
    public String deleteEvent(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            eventService.deleteEvent(id, LoggingConstants.ADMIN_PANEL);
            redirectAttributes.addFlashAttribute("successMessage", "Event deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting event: " + e.getMessage());
        }
        return "redirect:/admin/events";
    }

    /**
     * Registration Management Pages
     */
    @GetMapping("/registrations")
    public String listRegistrations(
            @ModelAttribute("filter") RegistrationFilterDTO filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "registeredAt") String sort,
            @RequestParam(defaultValue = "desc") String direction,
            Model model) {
        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        // Ensure filter is never null
        if (filter == null) {
            filter = new RegistrationFilterDTO();
        }

        Page<RegistrationManagementDTO> registrationsPage = registrationManagementService.getFilteredRegistrations(filter, pageable);

        model.addAttribute("registrations", registrationsPage.getContent());
        model.addAttribute("statuses", RegistrationStatus.values());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", registrationsPage.getTotalPages());
        model.addAttribute("totalItems", registrationsPage.getTotalElements());
        model.addAttribute("filter", filter);
        return "admin/registrations/list";
    }

    @PostMapping("/registrations/approve/{id}")
    public String approveRegistration(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            registrationManagementService.updateRegistrationStatus(id, RegistrationStatus.REGISTERED);
            redirectAttributes.addFlashAttribute("successMessage", "Registration approved successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error approving registration: " + e.getMessage());
        }
        return "redirect:/admin/registrations";
    }

    @PostMapping("/registrations/complete/{id}")
    public String completeRegistration(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            registrationManagementService.updateRegistrationStatus(id, RegistrationStatus.COMPLETED);
            redirectAttributes.addFlashAttribute("successMessage", "Registration marked as completed!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error completing registration: " + e.getMessage());
        }
        return "redirect:/admin/registrations";
    }

    @PostMapping("/registrations/cancel/{id}")
    public String cancelRegistration(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            registrationManagementService.updateRegistrationStatus(id, RegistrationStatus.CANCELLED);
            redirectAttributes.addFlashAttribute("successMessage", "Registration cancelled!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error cancelling registration: " + e.getMessage());
        }
        return "redirect:/admin/registrations";
    }

    @PostMapping("/registrations/delete/{id}")
    public String deleteRegistration(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            registrationManagementService.deleteRegistration(id);
            redirectAttributes.addFlashAttribute("successMessage", "Registration deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting registration: " + e.getMessage());
        }
        return "redirect:/admin/registrations";
    }

    /**
     * FAQ Management Pages
     */
    @GetMapping("/faqs")
    public String listFAQs(
            @ModelAttribute FAQFilterDTO filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "desc") String direction,
            Model model) {
        log.info("{} - Listing FAQs with filter: {}", LoggingConstants.ADMIN_PANEL, filter);

        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        Page<FAQResponseDTO> faqsPage = faqService.getAllFAQs(filter, pageable);

        model.addAttribute("faqs", faqsPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", faqsPage.getTotalPages());
        model.addAttribute("totalItems", faqsPage.getTotalElements());
        model.addAttribute("filter", filter);
        model.addAttribute("categories", faqService.getAllCategories(false));
        return "admin/faqs/list";
    }

    @GetMapping("/faqs/new")
    public String newFAQForm(Model model) {
        log.info("{} - Showing new FAQ form", LoggingConstants.ADMIN_PANEL);
        model.addAttribute("faq", new FAQRequestDTO());
        model.addAttribute("categories", faqService.getAllCategories(false));
        return "admin/faqs/form";
    }

    @PostMapping("/faqs/new")
    public String createFAQ(
            @Valid @ModelAttribute("faq") FAQRequestDTO faqRequest,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        log.info("{} - Creating new FAQ", LoggingConstants.ADMIN_PANEL);

        if (bindingResult.hasErrors()) {
            log.warn("{} - Validation errors in FAQ creation", LoggingConstants.ADMIN_PANEL);
            model.addAttribute("categories", faqService.getAllCategories(false));
            return "admin/faqs/form";
        }

        try {
            faqService.createFAQ(faqRequest);
            redirectAttributes.addFlashAttribute("successMessage", "FAQ created successfully!");
            return "redirect:/admin/faqs";
        } catch (Exception e) {
            log.error("{} - Error creating FAQ: {}", LoggingConstants.ADMIN_PANEL, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Error creating FAQ: " + e.getMessage());
            return "redirect:/admin/faqs/new";
        }
    }

    @GetMapping("/faqs/edit/{id}")
    public String editFAQForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        log.info("{} - Showing edit form for FAQ ID: {}", LoggingConstants.ADMIN_PANEL, id);
        try {
            FAQResponseDTO faq = faqService.getFAQById(id);

            // Convert response DTO to request DTO for the form
            FAQRequestDTO faqRequest = FAQRequestDTO.builder()
                    .category(faq.getCategory())
                    .question(faq.getQuestion())
                    .answer(faq.getAnswer())
                    .publish(faq.getPublish())
                    .build();

            model.addAttribute("faq", faqRequest);
            model.addAttribute("faqId", id);
            model.addAttribute("categories", faqService.getAllCategories(false));
            return "admin/faqs/form";
        } catch (Exception e) {
            log.error("{} - Error loading FAQ for edit: {}", LoggingConstants.ADMIN_PANEL, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "FAQ not found!");
            return "redirect:/admin/faqs";
        }
    }

    @PostMapping("/faqs/edit/{id}")
    public String updateFAQ(
            @PathVariable Long id,
            @Valid @ModelAttribute("faq") FAQRequestDTO faqRequest,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        log.info("{} - Updating FAQ ID: {}", LoggingConstants.ADMIN_PANEL, id);

        if (bindingResult.hasErrors()) {
            log.warn("{} - Validation errors in FAQ update", LoggingConstants.ADMIN_PANEL);
            model.addAttribute("faqId", id);
            model.addAttribute("categories", faqService.getAllCategories(false));
            return "admin/faqs/form";
        }

        try {
            faqService.updateFAQ(id, faqRequest);
            redirectAttributes.addFlashAttribute("successMessage", "FAQ updated successfully!");
            return "redirect:/admin/faqs";
        } catch (Exception e) {
            log.error("{} - Error updating FAQ: {}", LoggingConstants.ADMIN_PANEL, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating FAQ: " + e.getMessage());
            return "redirect:/admin/faqs";
        }
    }

    @PostMapping("/faqs/delete/{id}")
    public String deleteFAQ(
            @PathVariable Long id,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String searchQuery,
            @RequestParam(required = false) Boolean publish,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            RedirectAttributes redirectAttributes) {
        log.info("{} - Deleting FAQ ID: {}", LoggingConstants.ADMIN_PANEL, id);
        try {
            faqService.deleteFAQ(id);
            redirectAttributes.addFlashAttribute("successMessage", "FAQ deleted successfully!");
        } catch (Exception e) {
            log.error("{} - Error deleting FAQ: {}", LoggingConstants.ADMIN_PANEL, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting FAQ: " + e.getMessage());
        }

        // Preserve filter parameters in redirect
        StringBuilder redirectUrl = new StringBuilder("redirect:/admin/faqs?page=" + page + "&size=" + size);
        if (category != null && !category.isEmpty()) {
            redirectUrl.append("&category=").append(category);
        }
        if (searchQuery != null && !searchQuery.isEmpty()) {
            redirectUrl.append("&searchQuery=").append(searchQuery);
        }
        if (publish != null) {
            redirectUrl.append("&publish=").append(publish);
        }

        return redirectUrl.toString();
    }

    @PostMapping("/faqs/toggle-publish/{id}")
    public String toggleFAQPublish(
            @PathVariable Long id,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String searchQuery,
            @RequestParam(required = false) Boolean publish,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            RedirectAttributes redirectAttributes) {
        log.info("{} - Toggling publish status for FAQ ID: {}", LoggingConstants.ADMIN_PANEL, id);
        try {
            faqService.togglePublishStatus(id);
            redirectAttributes.addFlashAttribute("successMessage", "FAQ publish status updated!");
        } catch (Exception e) {
            log.error("{} - Error toggling FAQ publish status: {}", LoggingConstants.ADMIN_PANEL, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating FAQ: " + e.getMessage());
        }

        // Preserve filter parameters in redirect
        StringBuilder redirectUrl = new StringBuilder("redirect:/admin/faqs?page=" + page + "&size=" + size);
        if (category != null && !category.isEmpty()) {
            redirectUrl.append("&category=").append(category);
        }
        if (searchQuery != null && !searchQuery.isEmpty()) {
            redirectUrl.append("&searchQuery=").append(searchQuery);
        }
        if (publish != null) {
            redirectUrl.append("&publish=").append(publish);
        }

        return redirectUrl.toString();
    }

    /**
     * Support Message Management Pages
     */
    @GetMapping("/support/messages")
    public String listSupportMessages(
            @ModelAttribute SupportMessageFilterDTO filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "desc") String direction,
            Model model) {
        log.info("{} - Listing Support Messages with filter: {}", LoggingConstants.ADMIN_PANEL, filter);

        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        Page<SupportMessageResponse> messagesPage = supportMessageService.getAllMessages(page, size, filter.getStatus());

        model.addAttribute("messages", messagesPage.getContent());
        model.addAttribute("statuses", SupportMessageStatus.values());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", messagesPage.getTotalPages());
        model.addAttribute("totalItems", messagesPage.getTotalElements());
        model.addAttribute("filter", filter);

        // Add statistics
        model.addAttribute("openCount", supportMessageService.countMessagesByStatus(SupportMessageStatus.OPEN));
        model.addAttribute("inProgressCount", supportMessageService.countMessagesByStatus(SupportMessageStatus.IN_PROGRESS));
        model.addAttribute("resolvedCount", supportMessageService.countMessagesByStatus(SupportMessageStatus.RESOLVED));
        model.addAttribute("closedCount", supportMessageService.countMessagesByStatus(SupportMessageStatus.CLOSED));

        return "admin/support/list";
    }

    @PostMapping("/support/messages/{id}/respond")
    public String respondToSupportMessage(
            @PathVariable Long id,
            @RequestParam String response,
            @RequestParam SupportMessageStatus status,
            @AuthenticationPrincipal User admin,
            RedirectAttributes redirectAttributes) {
        log.info("{} - Responding to Support Message ID: {}", LoggingConstants.ADMIN_PANEL, id);
        try {
            RespondToSupportMessageRequest request = new RespondToSupportMessageRequest(response, status);
            supportMessageService.respondToMessage(id, request, admin);
            redirectAttributes.addFlashAttribute("successMessage", "Response sent successfully!");
        } catch (Exception e) {
            log.error("{} - Error responding to support message: {}", LoggingConstants.ADMIN_PANEL, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Error responding to message: " + e.getMessage());
        }
        return "redirect:/admin/support/messages";
    }

    @PostMapping("/support/messages/{id}/status")
    public String updateSupportMessageStatus(
            @PathVariable Long id,
            @RequestParam SupportMessageStatus status,
            @AuthenticationPrincipal User admin,
            RedirectAttributes redirectAttributes) {
        log.info("{} - Updating Support Message ID: {} to status: {}", LoggingConstants.ADMIN_PANEL, id, status);
        try {
            supportMessageService.updateMessageStatus(id, status, admin);
            redirectAttributes.addFlashAttribute("successMessage", "Message status updated successfully!");
        } catch (Exception e) {
            log.error("{} - Error updating support message status: {}", LoggingConstants.ADMIN_PANEL, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating status: " + e.getMessage());
        }
        return "redirect:/admin/support/messages";
    }
}
