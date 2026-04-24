package com.xuka.exam.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Table;

@Entity
@Table(name = "User_Account")
public class UserAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "uc_id")
    private int ucId;

    @Column(name = "username", nullable = false, unique = true, length = 25)
    private String username;

    @Column(name = "pwd_hash", nullable = false, length = 255)
    private String pwdHash;

    @Column(name = "salt", nullable = false, length = 64)
    private String salt;

    @Column(name = "role", nullable = false)
    private int role; // 0 = Student, 1 = Teacher

    @OneToOne(mappedBy = "userAccount", cascade = CascadeType.ALL)
    private UserInfo userInfo;

    // Constructors
    public UserAccount() {
    }

    public UserAccount(String username, String pwdHash, String salt, int role) {
        this.username = username;
        this.pwdHash = pwdHash;
        this.salt = salt;
        this.role = role;
    }

    // Getters and Setters
    public int getUcId() {
        return ucId;
    }

    public void setUcId(int ucId) {
        this.ucId = ucId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPwdHash() {
        return pwdHash;
    }

    public void setPwdHash(String pwdHash) {
        this.pwdHash = pwdHash;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }
}
