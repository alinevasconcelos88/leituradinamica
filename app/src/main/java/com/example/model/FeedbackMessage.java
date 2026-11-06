package com.example.model;

import java.io.Serializable;

public class FeedbackMessage implements Serializable {
    private String id;
    private String userEmail;
    private String category; // "Cadastro", "Sugestão", "Dúvida", "Financeiro", "Outro"
    private String subject;
    private String message;
    private String timestamp;
    private String status; // "Pendente", "Respondida"
    private String reply;
    private String replyTimestamp;

    public FeedbackMessage() {}

    public FeedbackMessage(String id, String userEmail, String category, String subject, String message, String timestamp, String status, String reply, String replyTimestamp) {
        this.id = id;
        this.userEmail = userEmail;
        this.category = category;
        this.subject = subject;
        this.message = message;
        this.timestamp = timestamp;
        this.status = status;
        this.reply = reply;
        this.replyTimestamp = replyTimestamp;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getReply() { return reply; }
    public void setReply(String reply) { this.reply = reply; }

    public String getReplyTimestamp() { return replyTimestamp; }
    public void setReplyTimestamp(String replyTimestamp) { this.replyTimestamp = replyTimestamp; }
}
