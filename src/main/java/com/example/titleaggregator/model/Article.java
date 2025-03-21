package com.example.titleaggregator.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Article {
    private String title;
    private String url;
    private LocalDateTime date;

    public Article(String title, String url, LocalDateTime date) {
        this.title = title;
        this.url = url;
        this.date = date;
    }

    // Constructor without date for backward compatibility
    public Article(String title, String url) {
        this.title = title;
        this.url = url;
        this.date = null;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }
    
    public LocalDateTime getDate() {
        return date;
    }
    
    public String getFormattedDate() {
        if (date == null) {
            return "";
        }
        return date.format(DateTimeFormatter.ofPattern("MMMM d, yyyy"));
    }
}