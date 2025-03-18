package com.emailCheck.controllers;


import com.emailCheck.models.EmailValidationResponse;
import com.emailCheck.services.EmailValidationService;
import com.emailCheck.services.TokenLoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/email")
@CrossOrigin(origins = "*")
public class EmailCheckController {

   @Autowired
   EmailValidationService emailService;

    @Autowired
    private TokenLoginService tokenLoginService;

    @GetMapping("/validate")
    public ResponseEntity<?> validateEmail(@RequestParam String email, @RequestHeader(value = "Authorization", required = false) String token){
        boolean podeConsultar = tokenLoginService.verificarEmail(email, token);

        if (!podeConsultar) {
            return ResponseEntity.status(429).body("Limite de consultas atingido. Faça login para mais verificações.");
        }

        EmailValidationResponse response = emailService.validateEmail(email);
        System.out.println("token: " +  token);
        System.out.println("Recebida requisição para validar: " + email + " ---------- existe: " + response.isEmailExists());

        return ResponseEntity.ok(response);
    }
    }
