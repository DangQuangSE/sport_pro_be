package com.sport_pro_be.modules.auth.security;

import com.sport_pro_be.modules.auth.domain.User;
import com.sport_pro_be.modules.auth.interfaces.IJwtService;
import com.sport_pro_be.modules.auth.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final IJwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = authHeader.substring(7);
            String email = jwtService.extractEmailFromAccessToken(token);

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                Integer tokenVersion = jwtService.extractTokenVersionFromAccessToken(token);
                
                // Query user to verify token version has not been revoked
                User user = userRepository.findByEmailIgnoreCase(email).orElseThrow(() -> new RuntimeException("User not found"));

                if (!user.getTokenVersion().equals(tokenVersion)) {
                    throw new RuntimeException("Token has been revoked or expired context.");
                }

                List<String> roles = jwtService.extractRolesFromAccessToken(token);
                List<GrantedAuthority> authorities = roles.stream()
                        .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                        .collect(Collectors.toList());

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(user, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        } catch (Exception ex) {
            // For invalid token or revoked token, we clear the context and let it pass anonymously
            // The AuthenticationEntryPoint will catch and throw 401 later in the chain.
            SecurityContextHolder.clearContext();
            
            // Optionally, we could set request attribute to pass the error reason down
            request.setAttribute("jwtError", ex.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}

