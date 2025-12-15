package com.HazaribaghLibraries.security.oauth2;

import com.HazaribaghLibraries.entity.User;
import com.HazaribaghLibraries.repository.UserRepository;
import com.HazaribaghLibraries.security.jwt.JwtUtils;
import com.HazaribaghLibraries.security.services.UserDetailsServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import java.util.Random;

import java.io.IOException;

@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        // 1. Get User Info
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String picture = oAuth2User.getAttribute("picture");

        // 2. Check/Register User
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            user = new User();
            user.setEmail(email);
            user.setName(name);
            user.setProfilePicture(picture);
            user.setRole(User.Role.Student);
            user.setPassword(""); // Dummy Password

            // Random Phone Number logic
            long randomPhone = 1000000000L + new Random().nextInt(900000000);
            user.setPhoneNumber(String.valueOf(randomPhone));

            userRepository.save(user);
        }

        // 3. Generate JWT Cookie
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(userDetails);

        // 4.
        // We use jwtCookie.getName() here, which grabs "libhub-token-v3" from properties
        ResponseCookie cookieWithRootPath = ResponseCookie.from(jwtCookie.getName(), jwtCookie.getValue())
                .path("/")               // Available everywhere
                .maxAge(1 * 60 * 60)    // 1 hour
                .httpOnly(true)          // Secure
                .secure(false)           // ALLOW on Localhost
                .sameSite("Lax")         // ALLOW on Redirects
                .build();

        // 5. Add to Header
        response.addHeader(HttpHeaders.SET_COOKIE, cookieWithRootPath.toString());

        // 6.  Direct to Dashboard. The cookie is now global (path="/"), so this is safe.
        getRedirectStrategy().sendRedirect(request, response, "http://localhost:5173/dashboard");
    }
}