package com.yourfavspot.user.Model;

import lombok.Data;
import java.util.List;

@Data
public class UserDto {
    private String firstName;      // Imię
    private String lastName;       // Nazwisko
    private String email;          // Adres e-mail
    private String username;       // Nazwa użytkownika (może być taka sama jak email)
    private boolean enabled;       // Czy użytkownik jest aktywny (true/false)
    private List<CredentialDto> credentials; // Lista danych uwierzytelniających (hasło)

    @Data
    public static class CredentialDto {
        private String type;       // Typ poświadczenia (zawsze "password")
        private String value;      // Wartość hasła
        private boolean temporary; // Czy hasło jest tymczasowe (false = nie wymaga zmiany)
    }
}