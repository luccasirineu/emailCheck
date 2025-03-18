package com.emailCheck.controllers;

import com.emailCheck.dtos.LoginRequestDTO;
import com.emailCheck.dtos.RegisterRequestDTO;
import com.emailCheck.dtos.TokenResponseDTO;
import com.emailCheck.models.User;
import com.emailCheck.models.UserSession;
import com.emailCheck.services.SessionService;
import com.emailCheck.services.TokenLoginService;
import com.emailCheck.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private TokenLoginService tokenLoginService;

    @Autowired
    private SessionService sessionService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequestDTO registerRequestDTO) {
        if (!registerRequestDTO.getPassword().equals(registerRequestDTO.getConfirmPassword())) {
            return ResponseEntity.badRequest().body("Passwords do not match");
        }

        User user = new User();
        user.setFirstName(registerRequestDTO.getFirstName());
        user.setLastName(registerRequestDTO.getLastName());
        user.setEmail(registerRequestDTO.getEmail());
        user.setPassword(registerRequestDTO.getPassword());

        userService.saveUser(user);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO loginRequestDTO) {
        try {
            System.out.println("Recebendo requisição de login para: " + loginRequestDTO.getEmail());

            User findUser = userService.getUserByEmail(loginRequestDTO.getEmail()).orElse(null);

            if (findUser == null) {
                System.out.println("Usuário não encontrado");
                return ResponseEntity.status(401).body("Credenciais inválidas");
            }

            if (!userService.passwordMatches(loginRequestDTO.getPassword(), findUser.getPassword())) {
                System.out.println("Senha incorreta");
                return ResponseEntity.status(401).body("Senha incorreta");
            }
            // Verifica se já existe um token desativado para esse usuário
            Optional<UserSession> existingSession = sessionService.findInactiveSessionByUserId(findUser.getId());

            String token;
            if (existingSession.isPresent()) {
                // Se já existir um token desativado, reativa ele
                UserSession session = existingSession.get();
                session.setActive(true);
                sessionService.updateSession(session.getId());
                token = session.getToken();  // Reutiliza o mesmo token
            } else {
                // Caso contrário, gera um novo token e cria uma nova sessão
                token = tokenLoginService.generateToken(findUser.getEmail(), findUser.getFirstName(), findUser.getLastName());
                sessionService.createSession(findUser, token);
            }

            return ResponseEntity.ok(new TokenResponseDTO(token));
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciais inválidas");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {


        String token = authHeader.substring(7);
        sessionService.invalidateSession(token);

        return ResponseEntity.ok("Logout realizado com sucesso");
    }

}