/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spotify.Bean;

import java.io.Serializable;

/**
 *
 * @author ADMIN
 */
public class User implements Serializable {

    private static final long serialVersionUID = 1L;
    private int userId;
    private String username;
    private String password;
    private String accountName;
    private String dateOfBirth;
    private String city;
    private int phone;
    private String email;

    public User() {

    }

    public User(String username, String password, String accountName, String dateOfBirth, String city, int phone, String email) {
        this.username = username;
        this.password = password;
        this.accountName = accountName;
        this.dateOfBirth = dateOfBirth;
        this.city = city;
        this.phone = phone;
        this.email = email;
    }

    public User(int userId, String accountName, String dateOfBirth, String city, int phone, String email) {
        this.userId = userId;
        this.accountName = accountName;
        this.dateOfBirth = dateOfBirth;
        this.city = city;
        this.phone = phone;
        this.email = email;
    }

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

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public int getPhone() {
        return phone;
    }

    public void setPhone(int phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}
