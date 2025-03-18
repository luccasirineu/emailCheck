package com.emailCheck.config;

import com.emailCheck.services.SessionService;
import com.emailCheck.services.TokenLoginService;
import com.emailCheck.services.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

   @Autowired
   private TokenLoginService tokenLoginService;

   @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private UserService userService;


    @Autowired
    public JwtFilter(TokenLoginService tokenLoginService, UserDetailsService userDetailsService) {
        this.tokenLoginService = tokenLoginService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        System.out.println("JwtFilter ativado - verificando requisição: " + request.getRequestURI());
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        String email = null;

        try {
            email = tokenLoginService.extractEmail(token);
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            // Se o token expirou, tenta renovar o token automaticamente
            String expiredEmail = e.getClaims().getSubject(); // Obtém o email do token expirado
            String firstName = e.getClaims().get("firstName", String.class);
            String lastName = e.getClaims().get("lastName", String.class);

            // Gera um novo token
            String newToken = tokenLoginService.generateToken(expiredEmail, firstName, lastName);

            // Retorna o novo token no cabeçalho da resposta
            response.setHeader("Authorization", "Bearer " + newToken);

            // Define o novo email para continuar a autenticação
            email = expiredEmail;
        }

        // Se o token não estiver ativo, rejeita a requisição
        if (!sessionService.isSessionValid(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Sessão inválida. Faça login novamente.");
            return;
        }

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            if (tokenLoginService.validateToken(token, userDetails.getUsername())) {


                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);

                Long userId = userService.getUserIdByEmail(userDetails.getUsername());
                sessionService.updateSession(userId);
            }
        }

        filterChain.doFilter(request, response);
    }
}
