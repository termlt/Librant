package com.librant.models;

import java.util.List;

public class User {
    private String id, name, surname, phoneNumber, address;
    private List<String> savedBooks;
    private List<String> viewedBooks;
    private boolean phoneNumberVisible;
    private boolean addressVisible;

    public User() {
    }

    public User(String id, String name, String surname, String phoneNumber,
                String address, List<String> savedBooks, List<String> viewedBooks,
                boolean phoneNumberVisible, boolean addressVisible) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.savedBooks = savedBooks;
        this.viewedBooks = viewedBooks;
        this.phoneNumberVisible = phoneNumberVisible;
        this.addressVisible = addressVisible;
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
}
