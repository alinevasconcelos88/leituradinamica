package com.example.model;

import java.io.Serializable;

public class PaymentRecord implements Serializable {
    private String id;
    private String userEmail;
    private String parentName;
    private int childAge;
    private double amount;
    private String status; // "ATIVO", "PENDENTE", "CANCELADO", "TESTE"
    private String method; // "Pix", "Cartão de Crédito"
    private String date;

    public PaymentRecord() {}

    public PaymentRecord(String id, String userEmail, String parentName, int childAge, double amount, String status, String method, String date) {
        this.id = id;
        this.userEmail = userEmail;
        this.parentName = parentName;
        this.childAge = childAge;
        this.amount = amount;
        this.status = status;
        this.method = method;
        this.date = date;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getParentName() { return parentName; }
    public void setParentName(String parentName) { this.parentName = parentName; }

    public int getChildAge() { return childAge; }
    public void setChildAge(int childAge) { this.childAge = childAge; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
}
