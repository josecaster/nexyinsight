package sr.we.entity.eclipsestore.tables;

import jakarta.persistence.Column;
import jakarta.persistence.Lob;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;

import static java.util.Objects.requireNonNull;

/**
 * This store represents a business store and is saved in the eclipsestore database
 * As of now in the testing phase, the eclipsestore database does not provide any Audit.
 * <p>
 * The goal here is to create as many stores as one wants and link the loyverse created items with a store.
 * This will then give back an opportunity to create daily dashboard for different store owners
 */
public class Section extends SuperDao implements ISection {
    /**
     * This ID will be used to connect the store to the Particular business
     */
    Long businessId;
    /**
     * The ID is used to connect the store record to a particular loyverse registered store
     */
    String id;
    /**
     * Given name
     */
    String name, default_name;
    /**
     * Misc
     */
    String address;
    /**
     * Misc
     */
    String city;
    /**
     * Misc
     */
    String region;
    /**
     * Misc
     */
    String postalCode;
    /**
     * Misc
     */
    String countryCode;
    /**
     * Misc
     */
    String phoneNumber;
    /**
     * Misc
     */
    String description;
    /**
     * Little time audit
     */
    LocalDateTime createdAt;
    /**
     * Little time audit
     */
    LocalDateTime updatedAtt;
    /**
     * Little time audit, TODO: WE MIGHT NOT ADD SOFT DELETE
     */
    LocalDateTime deletedAt;
    private Set<String> categories, devices;
    private Form form;
    private Color color;

    @Lob
    @Column(length = 1000000)
    private byte[] profilePicture;

    public enum Color {
        GREY,RED,PINK,ORANGE,YELLOW,GREEN,BLUE,PURPLE
    }

    public enum Form {
        SQUARE,CIRCLE,SUN,OCTAGON
    }

    public Section() {
    }

    public Section(Long businessId, String id, String name) {
        super();
        this.businessId = businessId;
        this.id = id;
        this.name = name;
        requireNonNull(this.name, "Store name cannot be null");
        requireNonNull(this.businessId, "Store parent business cannot be null");
        this.createdAt = LocalDateTime.now();
        this.updatedAtt = this.createdAt;
        this.deletedAt = null;
    }

    public byte[] getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(byte[] profilePicture) {
        this.profilePicture = profilePicture;
    }

    public Form getForm() {
        return form;
    }

    public void setForm(Form form) {
        this.form = form;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public String getDefault_name() {
        return default_name;
    }

    public void setDefault_name(String default_name) {
        this.default_name = default_name;
    }

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

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAtt() {
        return updatedAtt;
    }

    public void setUpdatedAtt(LocalDateTime updatedAtt) {
        this.updatedAtt = updatedAtt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    @Override
    public String toString() {
        return "Store{" + "businessId=" + businessId + ", id='" + id + '\'' + ", name='" + name + '\'' + ", address='" + address + '\'' + ", city='" + city + '\'' + ", region='" + region + '\'' + ", postalCode='" + postalCode + '\'' + ", countryCode='" + countryCode + '\'' + ", phoneNumber='" + phoneNumber + '\'' + ", description='" + description + '\'' + ", createdAt=" + createdAt + ", updatedAtt=" + updatedAtt + ", deletedAt=" + deletedAt + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Section section)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(businessId, section.businessId) && Objects.equals(id, section.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id);
    }

    public boolean isDefault() {
        if (StringUtils.isBlank(id) || StringUtils.isBlank(uuId)) {
            return false;
        }
        return id.equalsIgnoreCase(uuId);
    }

    public Set<String> getCategories() {
        return categories;
    }

    public void setCategories(Set<String> categories) {
        this.categories = categories;
    }

    public Set<String> getDevices() {
        return devices;
    }

    public void setDevices(Set<String> devices) {
        this.devices = devices;
    }
}
