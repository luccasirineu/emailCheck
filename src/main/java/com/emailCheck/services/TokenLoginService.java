package com.emailCheck.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class TokenLoginService {

    private static final int LIMITE_ANONIMO = 3;
    private static final int LIMITE_LOGADO = 5;
    private int consultasAnonimas = 0;
    private final Map<String, Integer> consultasPorUsuario = new HashMap<>();

    @Value("${jwt.secret}")
    private String SECRET_KEY;

    public boolean verificarEmail(String email, String token) {
        String usuario = "anonimo";

        // Verifica se é um usuário anônimo
        if (token == null || token.isBlank()) {
            if (consultasAnonimas >= LIMITE_ANONIMO) {
                return false; // Limite de anônimos atingido
            }
            consultasAnonimas++;
            return true;
        }

        // Remove "Bearer " do token
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // Verifica se o token é válido e extrai o e-mail do usuário
        if (isTokenExpired(token)) {
            return false; // Token expirado
        }

        usuario = extractEmail(token); // Identifica o usuário pelo e-mail

        // Obtém a contagem de consultas total do usuário autenticado
        int consultasFeitas = consultasPorUsuario.getOrDefault(usuario, 0);

        if (consultasFeitas >= LIMITE_LOGADO) {
            return false; // Se o usuário já usou as 5 requisições, bloqueia
        }

        // Atualiza a contagem de consultas para esse usuário (pelo e-mail)
        consultasPorUsuario.put(usuario, consultasFeitas + 1);
        return true;
    }




    public boolean validateToken(String token, String email) {
        String extractedEmail = extractEmail(token);
        return (email.equals(extractedEmail) && !isTokenExpired(token));
    }

    // Gera um token JWT
    public String generateToken(String email, String firstName, String lastName) {
        return Jwts.builder()
                .claim("firstName", firstName)
                .claim("lastName", lastName)
                .setSubject(email) // O email é salvo no "sub"
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24)) // 1 dia de validade
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    // Extrai o email do token (sub)
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    // Verifica se o token expirou
    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    // Extrai informações do token
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
        return claimsResolver.apply(claims);
    }
}
