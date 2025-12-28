package com.HazaribaghLibraries.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtils {

    // [IMPORTANT] Add these to your application.properties file later:
    // library.app.jwtSecret=YourSecretKeyMustBeAtLeast64BytesLongForHS512Security
    // library.app.jwtExpirationMs=86400000

    @Value("${library.app.jwtSecret}")
    private String jwtSecret;

    @Value("${library.app.jwtExpirationMs:86400000}")
    private int jwtExpirationMs;

    @Value("${library.app.jwtCookieName:libhub-jwt}")
    private String jwtCookie;
    // 1. Get JWT from Cookies
    public String getJwtFromCookies(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, jwtCookie);
        if (cookie != null) {
            return cookie.getValue();
        }
        return null;
    }

    // 2. Generate Cookie with JWT
    public ResponseCookie generateJwtCookie(UserDetails userPrincipal) {
        String jwt = generateTokenFromUsername(userPrincipal.getUsername());
        return ResponseCookie.from(jwtCookie, jwt)
                .path("/")       // [CHANGED] Changed from "/api" to "/" so you can SEE it in the browser inspector
                .maxAge(24 * 60 * 60 ) //  30 minute
                .httpOnly(true)     // [SECURITY] JavaScript cannot access this
                .secure(true)      // Set to true in Production (HTTPS)
                .sameSite("None") // CSRF protection  use // [CHANGED] Changed from "Strict" to "Lax". "Lax" is friendlier for localhost development.
                .build();
    }

    // 3. Clean Cookie (Logout)
    public ResponseCookie getCleanJwtCookie() {
        return ResponseCookie.from(jwtCookie, "")
                .path("/")
                .maxAge(0)

                .httpOnly(true)
                .secure(true)   // [IMPORTANT] Must match generateJwtCookie
                .sameSite("None")
                .build();
    }

    // 4. Extract Email from JWT
    public String getUserNameFromJwtToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key()).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    // Helper: Generate Token
    public String generateTokenFromUsername(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                .signWith(key(), SignatureAlgorithm.HS512)
                .compact();
    }

    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    // 5. Validate Token
    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(key()).build().parse(authToken);
            return true;
        } catch (Exception e) {
            System.err.println("Invalid JWT token: " + e.getMessage());
        }
        return false;
    }
}