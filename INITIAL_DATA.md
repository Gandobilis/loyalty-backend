# Initial Data Documentation

This document describes the sample data automatically created when the application starts in development/test mode.

## Overview

The `DataInitializer` component automatically populates the database with realistic sample data on application startup. This is useful for:

- **Development**: Test features with realistic data
- **Demonstrations**: Show the application with meaningful content
- **Testing**: Verify functionality across different scenarios

## Activation

The data initializer runs automatically when:
- Spring profile is `dev`, `test`, or `default`
- The database is empty (no existing users)

To disable: Set active profile to `prod` or delete existing data first.

## Sample Data Created

### 1. Users (7 total)

#### Admin Account
- **Email**: admin@loyalty.com
- **Password**: admin123
- **Name**: System Administrator
- **Role**: ADMIN
- **Points**: 0
- **Description**: System administrator account

#### Regular Users (6 total)

All regular users use password: **password123**

| Name | Email | Points | Events | Hours | Description |
|------|-------|--------|--------|-------|-------------|
| John Doe | john.doe@example.com | 150 | 3 | 15 | Passionate about community service |
| Jane Smith | jane.smith@example.com | 320 | 8 | 40 | Active volunteer in sports/youth |
| Michael Brown | michael.brown@example.com | 85 | 2 | 10 | Education enthusiast |
| Sarah Wilson | sarah.wilson@example.com | 580 | 15 | 75 | Dedicated volunteer coordinator |
| David Garcia | david.garcia@example.com | 45 | 1 | 5 | New to volunteering |
| Emma Martinez | emma.martinez@example.com | 420 | 11 | 55 | Sports enthusiast |

### 2. Companies (5 total)

1. **TechCorp Solutions** - Technology company
2. **Green Earth Foundation** - Environmental organization
3. **Fitness Plus** - Health and fitness center
4. **BookWorld** - Bookstore chain
5. **Coffee Culture** - Coffee shop chain

### 3. Events (13 total)

#### Culture Events (3)
1. **Art Exhibition: Modern Georgian Artists**
   - Date: 10 days from now
   - Location: Rustaveli Avenue, Tbilisi
   - Points: 50
   - Status: Upcoming

2. **Traditional Dance Performance**
   - Date: 15 days from now
   - Location: Rustaveli Theatre
   - Points: 40
   - Status: Upcoming

3. **Wine Tasting and Cultural Heritage**
   - Date: 5 days ago (completed)
   - Location: Wine Museum, Kakheti
   - Points: 45
   - Status: Past event

#### Sport Events (3)
4. **Community Marathon 2025**
   - Date: 20 days from now
   - Location: Vake Park
   - Points: 100
   - Distance: 5km, 10km, 21km options

5. **Basketball Tournament: Youth Championship**
   - Date: 12 days from now
   - Location: Sports Palace
   - Points: 30

6. **Mountain Hiking Adventure**
   - Date: 3 days ago (completed)
   - Location: Kazbegi Region
   - Points: 80

#### Education Events (3)
7. **Digital Skills Workshop: Web Development**
   - Date: 8 days from now
   - Location: Tech Hub, Freedom Square
   - Points: 60
   - Duration: 4 hours

8. **Financial Literacy Seminar**
   - Date: 18 days from now
   - Location: Business Center, Vake
   - Points: 55

9. **Environmental Science Lecture Series**
   - Date: 10 days ago (completed)
   - Location: Ilia State University
   - Points: 70

#### Youth Events (4)
10. **Youth Leadership Camp**
    - Date: 25 days from now
    - Location: Kojori Youth Camp
    - Points: 120
    - Duration: 3 days

11. **Teen Coding Club: Game Development**
    - Date: 5 days from now
    - Location: Innovation Lab, Saburtalo
    - Points: 40

12. **Youth Debate Championship**
    - Date: 7 days ago (completed)
    - Location: Tbilisi State University
    - Points: 35

13. **Music Festival: Young Talents**
    - Date: 30 days from now
    - Location: Open Air Theatre, Mtatsminda
    - Points: 50

### 4. Vouchers (12 total)

#### TechCorp Solutions (2 vouchers)
1. **Tech Gadget Discount - 20% Off** (200 points)
   - 20% off on tech gadget purchases up to $100
   - Expires: 3 months from now

2. **Free Tech Support Session** (150 points)
   - 1-hour free technical support
   - Expires: 2 months from now

#### Green Earth Foundation (2 vouchers)
3. **Eco-Friendly Product Bundle** (180 points)
   - Sustainable products bundle
   - Expires: 4 months from now

4. **Tree Planting Certificate** (100 points)
   - Plant a tree in your name
   - Expires: 6 months from now

#### Fitness Plus (3 vouchers)
5. **1 Month Gym Membership** (300 points)
   - Full gym access for 30 days
   - Expires: 2 months from now

6. **Personal Training Session** (120 points)
   - One-on-one training session
   - Expires: 3 months from now

7. **Fitness Class Pack - 5 Sessions** (180 points)
   - 5 group fitness classes
   - Expires: 2 months from now

#### BookWorld (2 vouchers)
8. **Book Voucher - $20** (150 points)
   - $20 credit for book purchases
   - Expires: 5 months from now

9. **Audiobook Subscription - 3 Months** (250 points)
   - 3 months premium audiobook access
   - Expires: 3 months from now

#### Coffee Culture (3 vouchers)
10. **Free Coffee for a Week** (80 points)
    - One free coffee per day for 7 days
    - Expires: 1 month from now

11. **Coffee Tasting Experience** (120 points)
    - Premium coffee tasting with expert barista
    - Expires: 2 months from now

12. **Coffee + Pastry Combo - 5 Times** (100 points)
    - 5 coffee and pastry combos
    - Expires: 2 months from now

### 5. Event Registrations (28 total)

#### By Status
- **Pending**: 4 registrations (awaiting approval)
- **Registered**: 13 registrations (approved, event upcoming)
- **Completed**: 11 registrations (event finished)

#### Most Active Participants
1. **Sarah Wilson**: 10 events registered
2. **Jane Smith**: 8 events registered
3. **Emma Martinez**: 6 events registered

### 6. User Voucher Exchanges (8 total)

#### Active Vouchers (5)
- Jane Smith: Coffee for a Week
- Sarah Wilson: Audiobook Subscription, Tree Planting
- John Doe: Tech Support Session
- Emma Martinez: Personal Training, Fitness Classes

#### Redeemed Vouchers (3)
- Jane Smith: Gym Membership
- Sarah Wilson: Gym Membership
- Emma Martinez: Coffee + Pastry Combo

## Data Relationships

### Registration Distribution
- Culture events: 7 registrations
- Sport events: 9 registrations
- Education events: 5 registrations
- Youth events: 7 registrations

### User Point Balances After Vouchers

| User | Initial Points | Vouchers Exchanged | Points Cost | Remaining Points |
|------|----------------|-------------------|-------------|------------------|
| John Doe | 150 | 1 | 150 | ~0 |
| Jane Smith | 320 | 2 | 380 | Deficit (needs more events) |
| Michael Brown | 85 | 0 | 0 | 85 |
| Sarah Wilson | 580 | 3 | 650 | Deficit (very active) |
| David Garcia | 45 | 0 | 0 | 45 |
| Emma Martinez | 420 | 3 | 400 | ~20 |

*Note: Some users have point deficits, reflecting real-world scenarios where users may exchange vouchers expecting future points from registered events.*

## Testing Scenarios

The initial data supports testing various scenarios:

### User Management
- ✅ Admin vs regular user roles
- ✅ Users with different activity levels
- ✅ Users with various point balances

### Event Management
- ✅ Events across all categories
- ✅ Past, present, and future events
- ✅ Events with different point values
- ✅ Events with geographic coordinates

### Registration System
- ✅ All registration statuses (pending, registered, completed, cancelled)
- ✅ Multiple users per event
- ✅ Multiple events per user
- ✅ Registration comments

### Voucher System
- ✅ Various voucher types and point costs
- ✅ Multiple companies offering vouchers
- ✅ Active and redeemed voucher statuses
- ✅ Voucher expiration dates
- ✅ Users with insufficient points scenarios

### Business Logic
- ✅ Point accumulation from events
- ✅ Point redemption for vouchers
- ✅ Event count tracking
- ✅ Working hours tracking
- ✅ Duplicate registration prevention (unique constraint)

## Customization

To modify the initial data:

1. **Edit DataInitializer.java**: Located at `/src/main/java/com/multi/loyaltybackend/config/DataInitializer.java`
2. **Adjust methods**:
   - `createUsers()` - Add/modify users
   - `createCompanies()` - Add/modify companies
   - `createEvents()` - Add/modify events
   - `createVouchers()` - Add/modify vouchers
   - `createRegistrations()` - Add/modify registrations
   - `createUserVouchers()` - Add/modify voucher exchanges

3. **Delete existing data**: Clear the database to trigger re-initialization

## Disabling Data Initialization

To disable automatic data initialization:

```properties
# application.properties
spring.profiles.active=prod
```

Or delete the `@Profile` annotation from `DataInitializer.java`.

## Verification

After application startup, verify data creation by checking the logs:

```
=== Data Initialization Summary ===
Users created: 7
  - Admins: 1
  - Regular users: 6
Companies created: 5
Events created: 13
  - Culture: 3
  - Sport: 3
  - Education: 3
  - Youth: 4
Vouchers created: 12
Registrations created: 28
  - Pending: 4
  - Registered: 13
  - Completed: 11
User vouchers created: 8
  - Active: 5
  - Redeemed: 3
```

## API Endpoints to Test With

### Authentication
```bash
# Login as admin
POST /api/auth/login
{
  "email": "admin@loyalty.com",
  "password": "admin123"
}

# Login as regular user
POST /api/auth/login
{
  "email": "jane.smith@example.com",
  "password": "password123"
}
```

### Query Data
```bash
# Get all events
GET /api/events

# Get user profile
GET /api/users/{userId}

# Get available vouchers
GET /api/vouchers

# Get user registrations
GET /api/registrations/user/{userId}
```

## Data Consistency

The initializer ensures:
- ✅ All foreign key relationships are valid
- ✅ Unique constraints are respected
- ✅ Enum values are valid
- ✅ Date/time values are logical
- ✅ Point calculations are accurate
- ✅ No orphaned records

## Troubleshooting

### Data Not Created
- **Check profile**: Ensure you're running with `dev`, `test`, or `default` profile
- **Check database**: Data only created if database is empty
- **Check logs**: Look for "Data Initialization" messages

### Errors During Initialization
- **Constraint violations**: Check for duplicate emails/mobile numbers
- **Foreign key errors**: Ensure companies exist before creating vouchers
- **Validation errors**: Verify all required fields are provided

### Resetting Data
1. Stop the application
2. Clear the database: `DROP DATABASE loyalty_db; CREATE DATABASE loyalty_db;`
3. Restart the application
4. Data will be recreated automatically

## Security Notes

⚠️ **Important**: This data initializer is for **development/testing only**.

- Default passwords are weak and should **never** be used in production
- All sample data is public and contains no sensitive information
- Disable data initialization in production environments
- Change all default credentials before deploying

## See Also

- [Entity Models Documentation](src/main/java/com/multi/loyaltybackend/model/)
- [Repository Layer](src/main/java/com/multi/loyaltybackend/repository/)
- [Service Layer](src/main/java/com/multi/loyaltybackend/service/)
