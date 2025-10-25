package com.multi.loyaltybackend.config;

import com.multi.loyaltybackend.company.model.Company;
import com.multi.loyaltybackend.company.repository.CompanyRepository;
import com.multi.loyaltybackend.model.*;
import com.multi.loyaltybackend.repository.EventRepository;
import com.multi.loyaltybackend.repository.RegistrationRepository;
import com.multi.loyaltybackend.repository.UserRepository;
import com.multi.loyaltybackend.voucher.model.UserVoucher;
import com.multi.loyaltybackend.voucher.model.Voucher;
import com.multi.loyaltybackend.voucher.repository.UserVoucherRepository;
import com.multi.loyaltybackend.voucher.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data initialization component that creates sample data on application startup.
 * Only runs in development and test profiles.
 * <p>
 * Creates:
 * - Admin and regular users
 * - Companies (sponsors)
 * - Events across different categories
 * - Vouchers from different companies
 * - User registrations for events
 * - User voucher exchanges
 */
@Slf4j
@Component
@Profile({"dev", "test", "default"})
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final RegistrationRepository registrationRepository;
    private final CompanyRepository companyRepository;
    private final VoucherRepository voucherRepository;
    private final UserVoucherRepository userVoucherRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        // Check if data already exists
        if (userRepository.count() > 0) {
            log.info("Database already contains data. Skipping initialization.");
            return;
        }

        log.info("=== Starting Data Initialization ===");

        // Create data in order (respecting foreign key dependencies)
        List<User> users = createUsers();
        List<Company> companies = createCompanies();
        List<Event> events = createEvents();
        List<Voucher> vouchers = createVouchers(companies);
        createRegistrations(users, events);
        createUserVouchers(users, vouchers);

        log.info("=== Data Initialization Complete ===");
        logSummary();
    }

    /**
     * Creates sample users with different roles and profiles.
     */
    private List<User> createUsers() {
        log.info("Creating users...");
        List<User> users = new ArrayList<>();

        // Admin user
        User admin = User.builder()
                .email("admin@loyalty.com")
                .password(passwordEncoder.encode("admin123"))
                .fullName("System Administrator")
                .role(Role.ADMIN)
                .mobileNumber("+995555000001")
                .age(35)
                .totalPoints(0)
                .eventCount(0)
                .workingHours(0)
                .aboutMe("System administrator account for managing the loyalty platform")
                .fileName("default-profile.png")
                .build();
        users.add(admin);

        // Regular users with diverse profiles
        User user1 = User.builder()
                .email("john.doe@example.com")
                .password(passwordEncoder.encode("password123"))
                .fullName("John Doe")
                .role(Role.USER)
                .mobileNumber("+995555000002")
                .age(28)
                .totalPoints(150)
                .eventCount(3)
                .workingHours(15)
                .aboutMe("Passionate about community service and cultural events")
                .fileName("default-profile.png")
                .build();
        users.add(user1);

        User user2 = User.builder()
                .email("jane.smith@example.com")
                .password(passwordEncoder.encode("password123"))
                .fullName("Jane Smith")
                .role(Role.USER)
                .mobileNumber("+995555000003")
                .age(24)
                .totalPoints(320)
                .eventCount(8)
                .workingHours(40)
                .aboutMe("Active volunteer interested in sports and youth programs")
                .fileName("default-profile.png")
                .build();
        users.add(user2);

        User user3 = User.builder()
                .email("michael.brown@example.com")
                .password(passwordEncoder.encode("password123"))
                .fullName("Michael Brown")
                .role(Role.USER)
                .mobileNumber("+995555000004")
                .age(31)
                .totalPoints(85)
                .eventCount(2)
                .workingHours(10)
                .aboutMe("Education enthusiast and occasional volunteer")
                .fileName("default-profile.png")
                .build();
        users.add(user3);

        User user4 = User.builder()
                .email("sarah.wilson@example.com")
                .password(passwordEncoder.encode("password123"))
                .fullName("Sarah Wilson")
                .role(Role.USER)
                .mobileNumber("+995555000005")
                .age(26)
                .totalPoints(580)
                .eventCount(15)
                .workingHours(75)
                .aboutMe("Dedicated volunteer coordinator with focus on youth development")
                .fileName("default-profile.png")
                .build();
        users.add(user4);

        User user5 = User.builder()
                .email("david.garcia@example.com")
                .password(passwordEncoder.encode("password123"))
                .fullName("David Garcia")
                .role(Role.USER)
                .mobileNumber("+995555000006")
                .age(29)
                .totalPoints(45)
                .eventCount(1)
                .workingHours(5)
                .aboutMe("New to volunteering, interested in cultural activities")
                .fileName("default-profile.png")
                .build();
        users.add(user5);

        User user6 = User.builder()
                .email("emma.martinez@example.com")
                .password(passwordEncoder.encode("password123"))
                .fullName("Emma Martinez")
                .role(Role.USER)
                .mobileNumber("+995555000007")
                .age(22)
                .totalPoints(420)
                .eventCount(11)
                .workingHours(55)
                .aboutMe("Sports enthusiast and community organizer")
                .fileName("default-profile.png")
                .build();
        users.add(user6);

        userRepository.saveAll(users);
        log.info("Created {} users", users.size());
        return users;
    }

    /**
     * Creates sample companies (sponsors).
     */
    private List<Company> createCompanies() {
        log.info("Creating companies...");
        List<Company> companies = new ArrayList<>();

        companies.add(Company.builder()
                .name("TechCorp Solutions")
                .logoFileName("logo1.png")
                .build());

        companies.add(Company.builder()
                .name("Green Earth Foundation")
                .logoFileName("logo2.png")
                .build());

        companies.add(Company.builder()
                .name("Fitness Plus")
                .logoFileName("logo3.png")
                .build());

        companies.add(Company.builder()
                .name("BookWorld")
                .logoFileName("logo4.png")
                .build());

        companies.add(Company.builder()
                .name("Coffee Culture")
                .logoFileName("logo5.png")
                .build());

        companyRepository.saveAll(companies);
        log.info("Created {} companies", companies.size());
        return companies;
    }

    /**
     * Creates sample events across different categories.
     */
    private List<Event> createEvents() {
        log.info("Creating events...");
        List<Event> events = new ArrayList<>();

        // CULTURE events
        events.add(Event.builder()
                .title("Art Exhibition: Modern Georgian Artists")
                .shortDescription("Explore contemporary Georgian art at the National Gallery")
                .description("Join us for an exclusive exhibition featuring the works of 20 modern Georgian artists. " +
                        "The exhibition showcases paintings, sculptures, and installations that reflect contemporary Georgian culture.")
                .category(EventCategory.CULTURE)
                .address("Rustaveli Avenue 11, Tbilisi")
                .latitude(41.6938)
                .longitude(44.8015)
                .dateTime(LocalDateTime.now().plusDays(10).withHour(18).withMinute(0))
                .points(50)
                        .fileName("cover1.png")
                .build());

        events.add(Event.builder()
                .title("Traditional Dance Performance")
                .shortDescription("Experience the beauty of Georgian traditional dance")
                .description("Watch a captivating performance by the National Georgian Dance Ensemble. " +
                        "The show features traditional dances from different regions of Georgia.")
                .category(EventCategory.CULTURE)
                .address("Rustaveli Theatre, Tbilisi")
                .latitude(41.6973)
                .longitude(44.8036)
                .dateTime(LocalDateTime.now().plusDays(15).withHour(19).withMinute(30))
                .points(40)
                        .fileName("cover2.png")
                .build());

        events.add(Event.builder()
                .title("Wine Tasting and Cultural Heritage")
                .shortDescription("Discover Georgian wine-making traditions")
                .description("Learn about 8,000 years of Georgian wine-making history while tasting premium wines from Kakheti region.")
                .category(EventCategory.CULTURE)
                .address("Wine Museum, Kakheti Highway")
                .latitude(41.7151)
                .longitude(44.8271)
                .dateTime(LocalDateTime.now().minusDays(5).withHour(15).withMinute(0))
                .points(45)
                        .fileName("cover3.png")
                .build());

        // SPORT events
        events.add(Event.builder()
                .title("Community Marathon 2025")
                .shortDescription("10km charity run for local schools")
                .description("Join hundreds of runners in this annual charity marathon. All proceeds go to improving " +
                        "sports facilities in local schools. Various distances available: 5km, 10km, and 21km.")
                .category(EventCategory.SPORT)
                .address("Vake Park, Tbilisi")
                .latitude(41.6977)
                .longitude(44.7736)
                .dateTime(LocalDateTime.now().plusDays(20).withHour(8).withMinute(0))
                .points(100)
                        .fileName("cover4.png")
                .build());

        events.add(Event.builder()
                .title("Basketball Tournament: Youth Championship")
                .shortDescription("Under-18 basketball championship finals")
                .description("Watch the final games of this year's youth basketball championship. " +
                        "Support young athletes and enjoy exciting matches.")
                .category(EventCategory.SPORT)
                .address("Sports Palace, Tbilisi")
                .latitude(41.7225)
                .longitude(44.7514)
                .dateTime(LocalDateTime.now().plusDays(12).withHour(16).withMinute(0))
                .points(30)
                        .fileName("cover5.png")
                .build());

        events.add(Event.builder()
                .title("Mountain Hiking Adventure")
                .shortDescription("Guided hike to Kazbegi summit")
                .description("Join experienced guides for a challenging but rewarding hike to Kazbegi mountain. " +
                        "Suitable for intermediate to advanced hikers.")
                .category(EventCategory.SPORT)
                .address("Kazbegi Region")
                .latitude(42.6569)
                .longitude(44.6459)
                .dateTime(LocalDateTime.now().minusDays(3).withHour(6).withMinute(0))
                .points(80)
                        .fileName("cover6.png")
                .build());

        // EDUCATION events
        events.add(Event.builder()
                .title("Digital Skills Workshop: Web Development Basics")
                .shortDescription("Learn HTML, CSS, and JavaScript fundamentals")
                .description("A 4-hour intensive workshop for beginners interested in web development. " +
                        "Participants will create their first website by the end of the session.")
                .category(EventCategory.EDUCATION)
                .address("Tech Hub, Freedom Square")
                .latitude(41.6925)
                .longitude(44.8075)
                .dateTime(LocalDateTime.now().plusDays(8).withHour(10).withMinute(0))
                .points(60)
                        .fileName("cover7.png")
                .build());

        events.add(Event.builder()
                .title("Financial Literacy Seminar")
                .shortDescription("Personal finance management for young adults")
                .description("Learn essential financial skills including budgeting, saving, investing, and managing debt. " +
                        "Interactive session with Q&A.")
                .category(EventCategory.EDUCATION)
                .address("Business Center, Vake")
                .latitude(41.6977)
                .longitude(44.7736)
                .dateTime(LocalDateTime.now().plusDays(18).withHour(14).withMinute(0))
                .points(55)
                        .fileName("cover8.png")
                .build());

        events.add(Event.builder()
                .title("Environmental Science Lecture Series")
                .shortDescription("Climate change and sustainable solutions")
                .description("Three-part lecture series on climate change, renewable energy, and sustainable living practices. " +
                        "Led by environmental scientists and activists.")
                .category(EventCategory.EDUCATION)
                .address("Ilia State University")
                .latitude(41.7098)
                .longitude(44.7736)
                .dateTime(LocalDateTime.now().minusDays(10).withHour(17).withMinute(0))
                .points(70)
                        .fileName("cover9.png")
                .build());

        // YOUTH events
        events.add(Event.builder()
                .title("Youth Leadership Camp")
                .shortDescription("3-day leadership development program")
                .description("Intensive leadership training camp for teenagers aged 14-18. " +
                        "Includes team building activities, workshops, and mentorship sessions.")
                .category(EventCategory.YOUTH)
                .address("Kojori Youth Camp")
                .latitude(41.6382)
                .longitude(44.7358)
                .dateTime(LocalDateTime.now().plusDays(25).withHour(9).withMinute(0))
                .points(120)
                        .fileName("cover10.png")
                .build());

        events.add(Event.builder()
                .title("Teen Coding Club: Game Development")
                .shortDescription("Learn to create your own video games")
                .description("Weekly coding club where teens learn game development using Unity. " +
                        "No prior experience required. All materials provided.")
                .category(EventCategory.YOUTH)
                .address("Innovation Lab, Saburtalo")
                .latitude(41.7225)
                .longitude(44.7514)
                .dateTime(LocalDateTime.now().plusDays(5).withHour(15).withMinute(0))
                .points(40)
                        .fileName("cover10.png")
                .build());

        events.add(Event.builder()
                .title("Youth Debate Championship")
                .shortDescription("Annual inter-school debate competition")
                .description("Watch talented young debaters compete on current social and political topics. " +
                        "Finals of the national youth debate championship.")
                .category(EventCategory.YOUTH)
                .address("Tbilisi State University")
                .latitude(41.7087)
                .longitude(44.7831)
                .dateTime(LocalDateTime.now().minusDays(7).withHour(13).withMinute(0))
                .points(35)
                        .fileName("cover10.png")
                .build());

        events.add(Event.builder()
                .title("Music Festival: Young Talents")
                .shortDescription("Showcase of emerging young musicians")
                .description("Annual music festival featuring performances by talented young musicians from across the country. " +
                        "Various genres including classical, jazz, and contemporary.")
                .category(EventCategory.YOUTH)
                .address("Open Air Theatre, Mtatsminda")
                .latitude(41.6944)
                .longitude(44.7872)
                .dateTime(LocalDateTime.now().plusDays(30).withHour(18).withMinute(0))
                .points(50)
                .fileName("default-event.png")
                .build());

        eventRepository.saveAll(events);
        log.info("Created {} events", events.size());
        return events;
    }

    /**
     * Creates sample vouchers from different companies.
     */
    private List<Voucher> createVouchers(List<Company> companies) {
        log.info("Creating vouchers...");
        List<Voucher> vouchers = new ArrayList<>();

        // TechCorp Solutions vouchers
        vouchers.add(Voucher.builder()
                .title("Tech Gadget Discount - 20% Off")
                .points(200)
                .expiry(LocalDateTime.now().plusMonths(3))
                .company(companies.get(0))
                .build());

        vouchers.add(Voucher.builder()
                .title("Free Tech Support Session")
                .points(150)
                .expiry(LocalDateTime.now().plusMonths(2))
                .company(companies.get(0))
                .build());

        // Green Earth Foundation vouchers
        vouchers.add(Voucher.builder()
                .title("Eco-Friendly Product Bundle")
                .points(180)
                .expiry(LocalDateTime.now().plusMonths(4))
                .company(companies.get(1))
                .build());

        vouchers.add(Voucher.builder()
                .title("Tree Planting Certificate")
                .points(100)
                .expiry(LocalDateTime.now().plusMonths(6))
                .company(companies.get(1))
                .build());

        // Fitness Plus vouchers
        vouchers.add(Voucher.builder()
                .title("1 Month Gym Membership")
                .points(300)
                .expiry(LocalDateTime.now().plusMonths(2))
                .company(companies.get(2))
                .build());

        vouchers.add(Voucher.builder()
                .title("Personal Training Session")
                .points(120)
                .expiry(LocalDateTime.now().plusMonths(3))
                .company(companies.get(2))
                .build());

        vouchers.add(Voucher.builder()
                .title("Fitness Class Pack - 5 Sessions")
                .points(180)
                .expiry(LocalDateTime.now().plusMonths(2))
                .company(companies.get(2))
                .build());

        // BookWorld vouchers
        vouchers.add(Voucher.builder()
                .title("Book Voucher - $20")
                .points(150)
                .expiry(LocalDateTime.now().plusMonths(5))
                .company(companies.get(3))
                .build());

        vouchers.add(Voucher.builder()
                .title("Audiobook Subscription - 3 Months")
                .points(250)
                .expiry(LocalDateTime.now().plusMonths(3))
                .company(companies.get(3))
                .build());

        // Coffee Culture vouchers
        vouchers.add(Voucher.builder()
                .title("Free Coffee for a Week")
                .points(80)
                .expiry(LocalDateTime.now().plusMonths(1))
                .company(companies.get(4))
                .build());

        vouchers.add(Voucher.builder()
                .title("Coffee Tasting Experience")
                .points(120)
                .expiry(LocalDateTime.now().plusMonths(2))
                .company(companies.get(4))
                .build());

        vouchers.add(Voucher.builder()
                .title("Coffee + Pastry Combo - 5 Times")
                .points(100)
                .expiry(LocalDateTime.now().plusMonths(2))
                .company(companies.get(4))
                .build());

        voucherRepository.saveAll(vouchers);
        log.info("Created {} vouchers", vouchers.size());
        return vouchers;
    }

    /**
     * Creates sample event registrations.
     */
    private void createRegistrations(List<User> users, List<Event> events) {
        log.info("Creating registrations...");
        List<Registration> registrations = new ArrayList<>();

        // John Doe's registrations (user 1)
        registrations.add(createRegistration(users.get(1), events.get(0), RegistrationStatus.REGISTERED,
                "Excited to attend this art exhibition!"));
        registrations.add(createRegistration(users.get(1), events.get(3), RegistrationStatus.COMPLETED,
                "Looking forward to the marathon"));
        registrations.add(createRegistration(users.get(1), events.get(6), RegistrationStatus.COMPLETED,
                "Interested in learning web development"));

        // Jane Smith's registrations (user 2)
        registrations.add(createRegistration(users.get(2), events.get(2), RegistrationStatus.COMPLETED,
                "Love Georgian wine culture"));
        registrations.add(createRegistration(users.get(2), events.get(3), RegistrationStatus.COMPLETED,
                "Training for this for months!"));
        registrations.add(createRegistration(users.get(2), events.get(4), RegistrationStatus.REGISTERED,
                "Big basketball fan"));
        registrations.add(createRegistration(users.get(2), events.get(5), RegistrationStatus.COMPLETED,
                "Can't wait for the hike!"));
        registrations.add(createRegistration(users.get(2), events.get(9), RegistrationStatus.REGISTERED,
                "Volunteering as an organizer"));
        registrations.add(createRegistration(users.get(2), events.get(10), RegistrationStatus.REGISTERED,
                "Love helping teens learn to code"));
        registrations.add(createRegistration(users.get(2), events.get(11), RegistrationStatus.COMPLETED,
                "Great debate competition"));
        registrations.add(createRegistration(users.get(2), events.get(12), RegistrationStatus.PENDING,
                "Excited for the music festival"));

        // Michael Brown's registrations (user 3)
        registrations.add(createRegistration(users.get(3), events.get(6), RegistrationStatus.REGISTERED,
                "Want to learn web development"));
        registrations.add(createRegistration(users.get(3), events.get(8), RegistrationStatus.COMPLETED,
                "Very informative lectures"));

        // Sarah Wilson's registrations (user 4)
        registrations.add(createRegistration(users.get(4), events.get(0), RegistrationStatus.REGISTERED,
                "Always support local artists"));
        registrations.add(createRegistration(users.get(4), events.get(1), RegistrationStatus.REGISTERED,
                "Traditional dance is beautiful"));
        registrations.add(createRegistration(users.get(4), events.get(2), RegistrationStatus.COMPLETED,
                "Fantastic wine tasting experience"));
        registrations.add(createRegistration(users.get(4), events.get(4), RegistrationStatus.REGISTERED,
                "Supporting youth sports"));
        registrations.add(createRegistration(users.get(4), events.get(7), RegistrationStatus.REGISTERED,
                "Financial literacy is crucial"));
        registrations.add(createRegistration(users.get(4), events.get(8), RegistrationStatus.COMPLETED,
                "Climate change education matters"));
        registrations.add(createRegistration(users.get(4), events.get(9), RegistrationStatus.REGISTERED,
                "Coordinating this camp"));
        registrations.add(createRegistration(users.get(4), events.get(10), RegistrationStatus.REGISTERED,
                "Mentoring the coding club"));
        registrations.add(createRegistration(users.get(4), events.get(11), RegistrationStatus.COMPLETED,
                "Judging the debates"));
        registrations.add(createRegistration(users.get(4), events.get(12), RegistrationStatus.PENDING,
                "Helping organize the festival"));

        // David Garcia's registration (user 5)
        registrations.add(createRegistration(users.get(5), events.get(0), RegistrationStatus.REGISTERED,
                "First cultural event I'm attending"));

        // Emma Martinez's registrations (user 6)
        registrations.add(createRegistration(users.get(6), events.get(3), RegistrationStatus.COMPLETED,
                "Marathon was amazing!"));
        registrations.add(createRegistration(users.get(6), events.get(4), RegistrationStatus.REGISTERED,
                "Love basketball!"));
        registrations.add(createRegistration(users.get(6), events.get(5), RegistrationStatus.COMPLETED,
                "Great hiking experience"));
        registrations.add(createRegistration(users.get(6), events.get(9), RegistrationStatus.REGISTERED,
                "Leadership skills are important"));
        registrations.add(createRegistration(users.get(6), events.get(10), RegistrationStatus.REGISTERED,
                "Want to help teens learn coding"));
        registrations.add(createRegistration(users.get(6), events.get(12), RegistrationStatus.PENDING,
                "Looking forward to the music"));

        registrationRepository.saveAll(registrations);
        log.info("Created {} registrations", registrations.size());
    }

    private Registration createRegistration(User user, Event event, RegistrationStatus status, String comment) {
        return Registration.builder()
                .user(user)
                .event(event)
                .status(status)
                .comment(comment)
                .build();
    }

    /**
     * Creates sample user voucher exchanges.
     */
    private void createUserVouchers(List<User> users, List<Voucher> vouchers) {
        log.info("Creating user vouchers...");
        List<UserVoucher> userVouchers = new ArrayList<>();

        // Jane Smith (user 2, 320 points) - most active
        userVouchers.add(createUserVoucher(users.get(2), vouchers.get(4), VoucherStatus.REDEEMED)); // Gym membership (300 pts)
        userVouchers.add(createUserVoucher(users.get(2), vouchers.get(9), VoucherStatus.ACTIVE)); // Coffee week (80 pts - leaving her with ~-60, but she earned more)

        // Sarah Wilson (user 4, 580 points) - very active
        userVouchers.add(createUserVoucher(users.get(4), vouchers.get(4), VoucherStatus.REDEEMED)); // Gym membership (300 pts)
        userVouchers.add(createUserVoucher(users.get(4), vouchers.get(8), VoucherStatus.ACTIVE)); // Audiobook (250 pts)
        userVouchers.add(createUserVoucher(users.get(4), vouchers.get(3), VoucherStatus.ACTIVE)); // Tree planting (100 pts - leaving ~-70)

        // John Doe (user 1, 150 points)
        userVouchers.add(createUserVoucher(users.get(1), vouchers.get(1), VoucherStatus.ACTIVE)); // Tech support (150 pts)

        // Emma Martinez (user 6, 420 points)
        userVouchers.add(createUserVoucher(users.get(6), vouchers.get(5), VoucherStatus.ACTIVE)); // Personal training (120 pts)
        userVouchers.add(createUserVoucher(users.get(6), vouchers.get(6), VoucherStatus.ACTIVE)); // Fitness classes (180 pts)
        userVouchers.add(createUserVoucher(users.get(6), vouchers.get(10), VoucherStatus.REDEEMED)); // Coffee combo (100 pts)

        userVoucherRepository.saveAll(userVouchers);
        log.info("Created {} user vouchers", userVouchers.size());
    }

    private UserVoucher createUserVoucher(User user, Voucher voucher, VoucherStatus status) {
        UserVoucher userVoucher = UserVoucher.builder()
                .user(user)
                .voucher(voucher)
                .status(status)
                .build();

        if (status == VoucherStatus.REDEEMED) {
            userVoucher.setRedeemedAt(LocalDateTime.now().minusDays((long) (Math.random() * 30)));
        }

        return userVoucher;
    }

    /**
     * Logs a summary of created data.
     */
    private void logSummary() {
        log.info("=== Data Initialization Summary ===");
        log.info("Users created: {}", userRepository.count());
        log.info("  - Admins: {}", userRepository.findAll().stream().filter(u -> u.getRole() == Role.ADMIN).count());
        log.info("  - Regular users: {}", userRepository.findAll().stream().filter(u -> u.getRole() == Role.USER).count());
        log.info("Companies created: {}", companyRepository.count());
        log.info("Events created: {}", eventRepository.count());
        log.info("  - Culture: {}", eventRepository.findAll().stream().filter(e -> e.getCategory() == EventCategory.CULTURE).count());
        log.info("  - Sport: {}", eventRepository.findAll().stream().filter(e -> e.getCategory() == EventCategory.SPORT).count());
        log.info("  - Education: {}", eventRepository.findAll().stream().filter(e -> e.getCategory() == EventCategory.EDUCATION).count());
        log.info("  - Youth: {}", eventRepository.findAll().stream().filter(e -> e.getCategory() == EventCategory.YOUTH).count());
        log.info("Vouchers created: {}", voucherRepository.count());
        log.info("Registrations created: {}", registrationRepository.count());
        log.info("  - Pending: {}", registrationRepository.findAll().stream().filter(r -> r.getStatus() == RegistrationStatus.PENDING).count());
        log.info("  - Registered: {}", registrationRepository.findAll().stream().filter(r -> r.getStatus() == RegistrationStatus.REGISTERED).count());
        log.info("  - Completed: {}", registrationRepository.findAll().stream().filter(r -> r.getStatus() == RegistrationStatus.COMPLETED).count());
        log.info("User vouchers created: {}", userVoucherRepository.count());
        log.info("  - Active: {}", userVoucherRepository.findAll().stream().filter(v -> v.getStatus() == VoucherStatus.ACTIVE).count());
        log.info("  - Redeemed: {}", userVoucherRepository.findAll().stream().filter(v -> v.getStatus() == VoucherStatus.REDEEMED).count());
        log.info("===================================");

        // Log sample credentials for testing
        log.info("");
        log.info("=== Sample Login Credentials ===");
        log.info("Admin:");
        log.info("  Email: admin@loyalty.com");
        log.info("  Password: admin123");
        log.info("");
        log.info("Regular Users (all use password: password123):");
        log.info("  - john.doe@example.com");
        log.info("  - jane.smith@example.com");
        log.info("  - michael.brown@example.com");
        log.info("  - sarah.wilson@example.com");
        log.info("  - david.garcia@example.com");
        log.info("  - emma.martinez@example.com");
        log.info("================================");
    }
}
