# Google OAuth Setup Guide

This guide will help you configure Google OAuth2 authentication for the Loyalty Backend application.

## Prerequisites

- A Google Cloud Platform account
- Access to the [Google Cloud Console](https://console.cloud.google.com/)

## Step 1: Create a Google Cloud Project

1. Go to the [Google Cloud Console](https://console.cloud.google.com/)
2. Click on the project dropdown at the top of the page
3. Click "New Project"
4. Enter a project name (e.g., "Loyalty App")
5. Click "Create"

## Step 2: Enable Google+ API

1. In your project, navigate to "APIs & Services" > "Library"
2. Search for "Google+ API"
3. Click on it and press "Enable"

## Step 3: Configure OAuth Consent Screen

1. Go to "APIs & Services" > "OAuth consent screen"
2. Select "External" user type (unless you have a Google Workspace)
3. Click "Create"
4. Fill in the required information:
   - **App name**: Your application name (e.g., "Loyalty App")
   - **User support email**: Your email address
   - **Developer contact information**: Your email address
5. Click "Save and Continue"
6. On the "Scopes" page, click "Add or Remove Scopes"
7. Add the following scopes:
   - `userinfo.email`
   - `userinfo.profile`
8. Click "Update" and then "Save and Continue"
9. On "Test users" page, add your email for testing
10. Click "Save and Continue"
11. Review and click "Back to Dashboard"

## Step 4: Create OAuth 2.0 Credentials

1. Go to "APIs & Services" > "Credentials"
2. Click "+ CREATE CREDENTIALS" > "OAuth client ID"
3. Select "Web application" as the application type
4. Enter a name (e.g., "Loyalty Backend OAuth Client")
5. Add Authorized JavaScript origins:
   ```
   http://localhost:8080
   http://localhost:3000
   http://localhost:8081
   ```
6. Add Authorized redirect URIs:
   ```
   http://localhost:8080/login/oauth2/code/google
   http://localhost:8080/oauth2/callback/google
   ```
   (Add your production URLs when deploying)
7. Click "Create"
8. **IMPORTANT**: Copy the Client ID and Client Secret - you'll need these for your `.env` file

## Step 5: Configure Your Application

1. Open your `.env` file in the project root
2. Update the following variables with your credentials:

```env
# Google OAuth2 Client Configuration
GOOGLE_CLIENT_ID=your-actual-client-id-from-step-4
GOOGLE_CLIENT_SECRET=your-actual-client-secret-from-step-4

# Application Configuration
APP_FRONTEND_URL=http://localhost:8081
APP_SERVER_URL=http://localhost:8080
```

## Step 6: Test the Integration

1. Start your backend server
2. Navigate to the OAuth login endpoint or add a Google Sign-In button in your frontend
3. The OAuth flow should redirect to Google's login page
4. After successful authentication, users will be redirected to your frontend with a JWT token

## Frontend Integration

### Google Sign-In Button URL

Direct your users to:
```
http://localhost:8080/oauth2/authorization/google
```

### Handle OAuth Redirect

After successful authentication, Google will redirect to:
```
http://localhost:8081/oauth2/redirect?token=<JWT_TOKEN>
```

Your frontend should:
1. Extract the token from the URL query parameter
2. Store it (localStorage, sessionStorage, or cookies)
3. Use it for subsequent API requests in the Authorization header:
   ```
   Authorization: Bearer <JWT_TOKEN>
   ```

### Example Frontend Code (React/Vue/Plain JS)

```javascript
// Extract token from URL
const urlParams = new URLSearchParams(window.location.search);
const token = urlParams.get('token');

if (token) {
  // Store token
  localStorage.setItem('authToken', token);

  // Redirect to home or dashboard
  window.location.href = '/dashboard';
}
```

### Example Google Sign-In Button (HTML)

```html
<a href="http://localhost:8080/oauth2/authorization/google"
   class="btn btn-google">
  Sign in with Google
</a>
```

## API Endpoints

### OAuth Endpoints

- **Initiate Google Login**: `GET /oauth2/authorization/google`
- **OAuth Success Callback**: `GET /api/auth/oauth2/success` (internal)
- **OAuth Failure Callback**: `GET /api/auth/oauth2/failure` (internal)

## Security Notes

1. **Never commit** your `.env` file or expose your Client Secret
2. For production:
   - Use HTTPS for all URLs
   - Update authorized origins and redirect URIs in Google Cloud Console
   - Consider using environment-specific configuration
3. The OAuth implementation automatically:
   - Creates new users if they don't exist
   - Updates user information if it changes in Google
   - Generates JWT tokens for authenticated users

## Troubleshooting

### Error: "redirect_uri_mismatch"
- Ensure the redirect URI in Google Cloud Console exactly matches your application URL
- Check that the URL includes the correct port and protocol (http/https)

### Error: "invalid_client"
- Verify your Client ID and Client Secret are correct in the `.env` file
- Ensure there are no extra spaces or quotes

### Error: "email_not_found"
- User's Google account may not have email access granted
- Ensure the email scope is included in OAuth consent screen configuration

### Users not being created
- Check database connection
- Verify CustomOAuth2UserService is running without errors
- Check application logs for exceptions

## Production Deployment

When deploying to production:

1. Update Google Cloud Console with production URLs
2. Add production redirect URIs:
   ```
   https://yourdomain.com/login/oauth2/code/google
   ```
3. Update `.env` file with production URLs:
   ```env
   APP_FRONTEND_URL=https://yourfrontend.com
   APP_SERVER_URL=https://yourbackend.com
   ```
4. Change OAuth consent screen from "Testing" to "Published"
5. Use secure secret management (AWS Secrets Manager, Azure Key Vault, etc.)

## Support

For more information, see:
- [Google OAuth 2.0 Documentation](https://developers.google.com/identity/protocols/oauth2)
- [Spring Security OAuth2 Client](https://docs.spring.io/spring-security/reference/servlet/oauth2/client/index.html)
