package com.lmkr.hesco.user.entity;

import com.lmkr.hesco.adminbound.entity.Circle;
import com.lmkr.hesco.adminbound.entity.Division;
import com.lmkr.hesco.adminbound.entity.SubDivision;
import jakarta.persistence.*;

@Entity
@Table(name = "app_user")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "contact_number", length = 20)
    private String contactNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "circle_id")
    private Circle circle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "division_id")
    private Division division;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_division_id")
    private SubDivision subDivision;

    @Column(length = 20)
    private String imei;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    protected AppUser() {
    }

    public AppUser(String username, String passwordHash, String firstName, String lastName,
                    String contactNumber, Role role) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.firstName = firstName;
        this.lastName = lastName;
        this.contactNumber = contactNumber;
        this.role = role;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public Circle getCircle() { return circle; }
    public void setCircle(Circle circle) { this.circle = circle; }
    public Division getDivision() { return division; }
    public void setDivision(Division division) { this.division = division; }
    public SubDivision getSubDivision() { return subDivision; }
    public void setSubDivision(SubDivision subDivision) { this.subDivision = subDivision; }
    public String getImei() { return imei; }
    public void setImei(String imei) { this.imei = imei; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
