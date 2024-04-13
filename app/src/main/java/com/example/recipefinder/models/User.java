package com.example.recipefinder.models;

import java.util.List;

public class User {
    private String email;
    private String password;
    private List<Recipe> recipeList;
    private String phone;
    private String userName;

    public User(String email, String password, List<Recipe> recipeList, String phone, String userName) {
        this.email = email;
        this.password = password;
        this.recipeList = recipeList;
        this.phone = phone;
        this.userName = userName;
    }

    public User(){}

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<Recipe> getRecipeList() {
        return recipeList;
    }

    public void setRecipeList(List<Recipe> recipeList) {
        this.recipeList = recipeList;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
