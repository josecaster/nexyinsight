package sr.we.entity.eclipsestore.tables;

import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document("customer")
public class Customer extends SuperDao {
    private Long businessId;
    public String id;
    public String name;
    public String email;
    public String phone_number;
    public String address;
    public String city;
    public String region;
    public String postal_code;
    public String country_code;
    public String note;
    public String customer_code;
    public LocalDateTime first_visit;
    public LocalDateTime last_visit;
    public int total_visits;
    public BigDecimal total_spent;
    public BigDecimal total_points;
    public LocalDateTime permanent_deletion_at;
    public LocalDateTime created_at;
    public LocalDateTime updated_at;
    public LocalDateTime deleted_at;

    public Long getBusinessId() {
        return businessId;
    }

    public void setBusinessId(Long businessId) {
        this.businessId = businessId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getPostal_code() {
        return postal_code;
    }

    public void setPostal_code(String postal_code) {
        this.postal_code = postal_code;
    }

    public String getCountry_code() {
        return country_code;
    }

    public void setCountry_code(String country_code) {
        this.country_code = country_code;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getCustomer_code() {
        return customer_code;
    }

    public void setCustomer_code(String customer_code) {
        this.customer_code = customer_code;
    }

    public LocalDateTime getFirst_visit() {
        return first_visit;
    }

    public void setFirst_visit(LocalDateTime first_visit) {
        this.first_visit = first_visit;
    }

    public LocalDateTime getLast_visit() {
        return last_visit;
    }

    public void setLast_visit(LocalDateTime last_visit) {
        this.last_visit = last_visit;
    }

    public int getTotal_visits() {
        return total_visits;
    }

    public void setTotal_visits(int total_visits) {
        this.total_visits = total_visits;
    }

    public BigDecimal getTotal_spent() {
        return total_spent;
    }

    public void setTotal_spent(BigDecimal total_spent) {
        this.total_spent = total_spent;
    }

    public BigDecimal getTotal_points() {
        return total_points;
    }

    public void setTotal_points(BigDecimal total_points) {
        this.total_points = total_points;
    }

    public LocalDateTime getPermanent_deletion_at() {
        return permanent_deletion_at;
    }

    public void setPermanent_deletion_at(LocalDateTime permanent_deletion_at) {
        this.permanent_deletion_at = permanent_deletion_at;
    }

    public LocalDateTime getCreated_at() {
        return created_at;
    }

    public void setCreated_at(LocalDateTime created_at) {
        this.created_at = created_at;
    }

    public LocalDateTime getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(LocalDateTime updated_at) {
        this.updated_at = updated_at;
    }

    public LocalDateTime getDeleted_at() {
        return deleted_at;
    }

    public void setDeleted_at(LocalDateTime deleted_at) {
        this.deleted_at = deleted_at;
    }
}
