package edu.uniquindio.stayhub_v2.config;

import edu.uniquindio.stayhub_v2.model.User;
import edu.uniquindio.stayhub_v2.repository.UserRepository;
import edu.uniquindio.stayhub_v2.service.JWTService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT Authentication Filter that intercepts HTTP requests to validate JWT tokens.
 *
 * <p>This filter executes once per request and is responsible for:
 * <ul>
 *   <li>Extracting the JWT token from the Authorization header</li>
 *   <li>Validating the token's format (Bearer scheme)</li>
 *   <li>Extracting user information from valid tokens</li>
 *   <li>Loading the corresponding user from the database</li>
 *   <li>Setting the authentication context for the current request</li>
 * </ul>
 *
 * <p><b>Request Flow:</b></p>
 * <pre>
 * 1. Extract Authorization header
 * 2. Validate "Bearer " prefix
 * 3. Extract email from JWT token
 * 4. Load user from database
 * 5. Set authentication in SecurityContext
 * 6. Continue a filter chain
 * </pre>
 *
 * @author StayHub Dev Team
 * @version 1.0
 * @since 1.0
 * @see OncePerRequestFilter
 * @see JWTService
 * @see org.springframework.security.core.context.SecurityContextHolder
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JWTService jwtService;
    private final UserRepository userRepository;

    /**
     * Processes the incoming request to authenticate the user via JWT token.
     *
     * <p>This method is invoked by the Spring Security filter chain for each HTTP request.
     * It extracts and validates the JWT token from the Authorization header, loads the
     * corresponding user from the database, and establishes the authentication context
     * if the token is valid.</p>
     *
     * <p><b>Filter Bypass Conditions:</b></p>
     * The filter will skip authentication and continue the chain when:
     * <ul>
     *   <li>Authorization header is missing</li>
     *   <li>Authorization header does not start with "Bearer "</li>
     *   <li>Authentication is already set in the SecurityContext</li>
     * </ul>
     *
     * @param request The HTTP request being processed
     * @param response The HTTP response to be sent
     * @param filterChain The filter chain to continue processing after authentication
     * @throws ServletException If a servlet-specific error occurs
     * @throws IOException If an I/O error occurs during request processing
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // Skip authentication if no Authorization header or not Bearer token
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extract token (remove "Bearer " prefix)
        String token = authHeader.substring(7);
        String email = jwtService.getEmailFromToken(token);

        // Only authenticate if email was extracted and no existing authentication
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            User user = userRepository.findByEmailAndDeletedFalseWithRoles(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            List<GrantedAuthority> authorities = user.getRoles().stream()
                    .<GrantedAuthority>map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                    .toList();

            // Create authentication token with user details and resolved authorities
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            user,
                            null,  // No credentials needed for authenticated user
                            authorities
                    );

            // Attach request details for auditing purposes
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // Set authentication in security context
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        // Continue with the filter chain
        filterChain.doFilter(request, response);
    }
}
