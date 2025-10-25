package com.multi.loyaltybackend.controller;

import com.multi.loyaltybackend.company.dto.CompanyFilterDTO;
import com.multi.loyaltybackend.company.model.Company;
import com.multi.loyaltybackend.company.service.CompanyService;
import com.multi.loyaltybackend.dto.EventFilterDTO;
import com.multi.loyaltybackend.dto.EventFormDTO;
import com.multi.loyaltybackend.dto.UserFilterDTO;
import com.multi.loyaltybackend.dto.UserFormDTO;
import com.multi.loyaltybackend.model.Event;
import com.multi.loyaltybackend.model.EventCategory;
import com.multi.loyaltybackend.model.Role;
import com.multi.loyaltybackend.model.User;
import com.multi.loyaltybackend.repository.EventRepository;
import com.multi.loyaltybackend.service.AdminService;
import com.multi.loyaltybackend.service.EventService;
import com.multi.loyaltybackend.voucher.dto.VoucherFilterDTO;
import com.multi.loyaltybackend.voucher.dto.VoucherRequest;
import com.multi.loyaltybackend.voucher.model.Voucher;
import com.multi.loyaltybackend.voucher.service.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    private final EventService eventService;
    private final EventRepository eventRepository;

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

        model.addAttribute("events", eventsPage.getContent());
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
    public String createEvent(@ModelAttribute EventFormDTO eventForm, RedirectAttributes redirectAttributes) {
        try {
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
            eventRepository.save(event);
            redirectAttributes.addFlashAttribute("successMessage", "Event created successfully!");
            return "redirect:/admin/events";
        } catch (Exception e) {
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
            RedirectAttributes redirectAttributes) {
        try {
            Event event = eventRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Event not found"));

            event.setTitle(eventForm.getTitle());
            event.setShortDescription(eventForm.getShortDescription());
            event.setDescription(eventForm.getDescription());
            event.setCategory(EventCategory.valueOf(eventForm.getCategory()));
            event.setAddress(eventForm.getAddress());
            event.setLatitude(eventForm.getLatitude());
            event.setLongitude(eventForm.getLongitude());
            event.setDateTime(eventForm.getDateTime());
            event.setPoints(eventForm.getPoints() != null ? eventForm.getPoints() : 0);

            eventRepository.save(event);
            redirectAttributes.addFlashAttribute("successMessage", "Event updated successfully!");
            return "redirect:/admin/events";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating event: " + e.getMessage());
            return "redirect:/admin/events/edit/" + id;
        }
    }

    @PostMapping("/events/delete/{id}")
    public String deleteEvent(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            eventService.deleteEvent(id);
            redirectAttributes.addFlashAttribute("successMessage", "Event deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting event: " + e.getMessage());
        }
        return "redirect:/admin/events";
    }
}
