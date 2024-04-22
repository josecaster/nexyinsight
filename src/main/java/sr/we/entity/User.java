package sr.we.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "application_user")
public class User extends AbstractEntity{

    private Long businessId;

    private String username;
    private String name;
    @JsonIgnore
    private String hashedPassword;
    @Enumerated(EnumType.STRING)
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<Role> roles;
    @Lob
    @Column(length = 1000000)
    private byte[] profilePicture;
    private String email;
    private Set<String> linkSections;
    private transient Collection<GrantedAuthority> authorities;
//
//    @Convert(converter= BooleanToYNStringConverter.class)
//    @Column(name = "ACTIVE")
//    private Boolean active;
//    @Convert(converter= BooleanToYNStringConverter.class)
//    @Column(name = "ACCOUNT_NON_LOCKED")
//    private Boolean accountNonLocked;
//    @Convert(converter= BooleanToYNStringConverter.class)
//    @Column(name = "CREDENTIALS_NON_LOCKED_EXPIRED")
//    private Boolean credentialsNonLockedExpired;
//    @Convert(converter= BooleanToYNStringConverter.class)
    @Column(name = "ENABLED")
    private Boolean enabled;


    public String getPassword() {
        return null;
    }

    public String getUsername() {
        return username;
    }

    public boolean isAccountNonExpired() {
        return true;
    }

    public boolean isAccountNonLocked() {
        return true;
    }

    public boolean isCredentialsNonExpired() {
        return true;
    }

    public boolean isEnabled() {
        return enabled != null && enabled;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getHashedPassword() {
        return hashedPassword;
    }
    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }
    public Set<Role> getRoles() {
        return roles;
    }
    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }
    public byte[] getProfilePicture() {
        return profilePicture;
    }
    public void setProfilePicture(byte[] profilePicture) {
        this.profilePicture = profilePicture;
    }

    public Set<String> getLinkSections() {
        return this.linkSections;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setLinkSections(Set<String> linkSections) {
        this.linkSections = linkSections;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public void setBusinessId(Long businessId) {
        this.businessId = businessId;
    }

    public Long getBusinessId() {
        return businessId;
    }

    public void setAuthorities(Collection<GrantedAuthority> authorities) {
        this.authorities = authorities;
    }
}
