package com.yourfavspot.user.Service;

import com.yourfavspot.user.Model.LoginResponse;
import com.yourfavspot.user.Model.UserDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class KeycloakAdminClient {

    private final WebClient webClient;
    private final WebClient tokenClient;
    private final String tokenUrl;
    private final String adminUsername;
    private final String adminPassword;
    private final String clientId;

    //parametry admina wstrzykujemy za pomocą value z application.yml
    public KeycloakAdminClient(@Value("${keycloak.admin-url}") String adminUrl,
                               @Value("${keycloak.token-url}") String tokenUrl,
                               @Value("${keycloak.admin-username}") String adminUsername,
                               @Value("${keycloak.admin-password}") String adminPassword,
                               @Value("${keycloak.client-id}") String clientId,
                               WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl(adminUrl).build(); //do komunikacji z administracyjnym api keyCloak
        this.tokenClient = webClientBuilder.build(); // używany do pobrania tokena
        this.tokenUrl = tokenUrl;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
        this.clientId = clientId;
    }

    private String getAccessToken() {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "password");
        formData.add("client_id", clientId);
        formData.add("username", adminUsername);
        formData.add("password", adminPassword);

        return tokenClient.post()
                .uri(tokenUrl)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> (String) response.get("access_token"))
                .block();
    }

    public boolean createUser(UserDto userDto) {
        try {
            String accessToken = getAccessToken();
            log.info("Udało się pobrać token dostępu z Keycloak");

            // Tworzenie mapy zgodnej z UserRepresentation
            Map<String, Object> keycloakUser = new HashMap<>();
            keycloakUser.put("firstName", userDto.getFirstName());
            keycloakUser.put("lastName", userDto.getLastName());
            keycloakUser.put("email", userDto.getEmail());
            keycloakUser.put("username", userDto.getUsername() != null ? userDto.getUsername() : userDto.getEmail());
            keycloakUser.put("enabled", userDto.isEnabled());

            // Dodanie credentials, jeśli istnieją
            if (userDto.getCredentials() != null && !userDto.getCredentials().isEmpty()) {
                keycloakUser.put("credentials", userDto.getCredentials());
            }

            // Wysłanie żądania do Keycloak
            webClient.post()
                    .uri(uriBuilder -> uriBuilder.path("/admin/realms/{realm}/users").build("your-realm"))
                    .header("Authorization", "Bearer " + accessToken)
                    .bodyValue(keycloakUser)
                    .retrieve()
                    .toBodilessEntity()
                    .block();

            log.info("Użytkownik {} pomyślnie utworzony w Keycloak", userDto.getEmail());
            return true;
        } catch (WebClientResponseException e) {
            log.error("Błąd HTTP: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("Nieoczekiwany błąd: ", e);
        }
        return false;
    }

    public LoginResponse login(String username, String password) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "password");
        formData.add("client_id", clientId);
        formData.add("username", username);
        formData.add("password", password);

        try {
            Map response = tokenClient.post()
                    .uri(tokenUrl)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .body(BodyInserters.fromFormData(formData))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            return LoginResponse.builder()
                    .accessToken((String) response.get("access_token"))
                    .tokenType((String) response.get("token_type"))
                    .build();
        } catch (WebClientResponseException e) {
            log.error("Login failed: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Authentication failed");
        }
    }



}
