package com.emailCheck.services;



import com.emailCheck.models.EmailValidationResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;



@Service
public class EmailValidationService {

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String API_URL = "https://api.usebouncer.com/v1.1/email/verify?email={email}";

    @Value("${email.api.key}")
    private String API_KEY;

    public EmailValidationResponse validateEmail(String email) {
        try {
            // Configuração do cabeçalho com a API Key
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-api-key", API_KEY);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Realiza a requisição para a API
            ResponseEntity<EmailValidationResponse> response = restTemplate.exchange(
                    API_URL, HttpMethod.GET, entity, EmailValidationResponse.class, email);

            EmailValidationResponse emailResponse = response.getBody();

            if (emailResponse.getStatus() == null) {
                System.out.println("Erro ao obter resposta da API.");
                return new EmailValidationResponse();
            }

            return emailResponse;


        } catch (Exception e) {
            System.err.println("Erro ao validar e-mail: " + e.getMessage());
            return new EmailValidationResponse();
        }
    }
}