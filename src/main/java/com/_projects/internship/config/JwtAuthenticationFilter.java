package com._projects.internship.config;

import com._projects.internship.service.security.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // Vérifie si le header Authorization commence bien par "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response); // Poursuit la chaîne de filtres
            return;
        }

        // Extrait le JWT
        jwt = authHeader.substring(7);

        try {
            // Extrait l'email ou le username du token
            userEmail = jwtService.extractUsername(jwt);

            // Si l'utilisateur n'est pas encore authentifié
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // Récupère les informations de l'utilisateur
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

                // Vérifie si le token est valide
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    // Crée un objet d'authentification
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    // Ajoute les détails supplémentaires à partir de la requête HTTP
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    // Met à jour le contexte de sécurité de Spring
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Log l'erreur et vide le contexte de sécurité
            System.err.println("Erreur lors de l'authentification JWT : " + e.getMessage());
            e.printStackTrace();
            SecurityContextHolder.clearContext();
        }

        // Continue la chaîne de filtres
        filterChain.doFilter(request, response);
    }
}
