package org.example.theminiprojectapijava.request;

/**
 * DTO (Data Transfer Object) — об'єкт запиту на автентифікацію.
 * Надсилається клієнтом на POST /api/auth/login у тілі запиту (JSON).
 *
 * Приклад JSON-запиту:
 * {
 *   "username": "admin",
 *   "password": "admin123"
 * }
 */
public class LoginRequest {

    private String username;
    private String password;

    /**
     * Безпараметрний конструктор — ОБОВ'ЯЗКОВИЙ для Spring (Jackson, Hibernate)
     */
    public LoginRequest() {
    }

    /**
     * Конструктор з параметрами
     */
    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // -----------------------------------------------------------------
    // Getter-и та setter-и (або використовуй @Data від Lombok)
    // -----------------------------------------------------------------
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "LoginRequest{" +
                "username='" + username + '\'' +
                ", password='" + "********" + '\'' +  // приховуємо пароль у логах!
                '}';
    }
}