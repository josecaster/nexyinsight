package sr.we.entity.eclipsestore.tables;


import java.time.LocalDateTime;

public record LoyStore(

        String id, String name, String address, String city, String region, String postal_code, String country_code,
        String phone_number, String description, LocalDateTime created_at, LocalDateTime updated_at,
        LocalDateTime deleted_at) {
}
