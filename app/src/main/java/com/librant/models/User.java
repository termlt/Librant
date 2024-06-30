package com.librant.models;

import com.google.firebase.firestore.PropertyName;

import java.util.List;

public class User {
    @PropertyName("id")
    private String id;

    @PropertyName("name")
    private String name;

    @PropertyName("surname")
    private String surname;

    @PropertyName("phoneNumber")
    private String phoneNumber;

    @PropertyName("address")
    private String address;

    @PropertyName("savedBooks")
    private List<String> savedBooks;

    @PropertyName("viewedBooks")
    private List<String> viewedBooks;

    @PropertyName("phoneNumberVisible")
    private boolean phoneNumberVisible;

    @PropertyName("addressVisible")
    private boolean addressVisible;

    @PropertyName("isAdmin")
    private boolean isAdmin;

    public User() {
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

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public List<String> getSavedBooks() {
        return savedBooks;
    }

    public void setSavedBooks(List<String> savedBooks) {
        this.savedBooks = savedBooks;
    }

    public List<String> getViewedBooks() {
        return viewedBooks;
    }

    public void setViewedBooks(List<String> viewedBooks) {
        this.viewedBooks = viewedBooks;
    }

    public boolean isPhoneNumberVisible() {
        return phoneNumberVisible;
    }

    public void setPhoneNumberVisible(boolean phoneNumberVisible) {
        this.phoneNumberVisible = phoneNumberVisible;
    }

    public boolean isAddressVisible() {
        return addressVisible;
    }

    public void setAddressVisible(boolean addressVisible) {
        this.addressVisible = addressVisible;
    }

    public boolean getIsAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(boolean isAdmin) {
        this.isAdmin = isAdmin;
    }
}
