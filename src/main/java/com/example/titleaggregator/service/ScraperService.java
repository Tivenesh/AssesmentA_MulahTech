package com.example.titleaggregator.service;
import com.example.titleaggregator.model.Article;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class ScraperService {

    private static final String BASE_URL = "https://www.theverge.com";
    private static final LocalDate CUT_OFF_DATE = LocalDate.of(2022, 1, 1);

    public List<Article> fetchArticles() {
        List<Article> articles = new ArrayList<>();
        int page = 1;
        boolean hasMoreArticles = true;
        
        try {
            // Fetch multiple pages to get more articles
            while (hasMoreArticles && page <= 5) { // Limit to 5 pages to avoid too many requests
                String url = page == 1 ? BASE_URL : BASE_URL + "/archives/" + page;
                Document doc = Jsoup.connect(url).get();
                
                // For the main page
                if (page == 1) {
                    // Get featured articles
                    Elements featuredElements = doc.select("h2.c-entry-box--compact__title a");
                    processArticleElements(featuredElements, articles);
                    
                    // Get other articles on the main page
                    Elements mainElements = doc.select("h2 a");
                    processArticleElements(mainElements, articles);
                } else {
                    // For archive pages
                    Elements archiveElements = doc.select("h2 a");
                    if (archiveElements.isEmpty()) {
                        hasMoreArticles = false;
                    } else {
                        processArticleElements(archiveElements, articles);
                    }
                }
                
                page++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        // Sort articles by date (newest first)
        articles.sort(Comparator.comparing(Article::getDate, Comparator.nullsLast(Comparator.reverseOrder())));
        
        return articles;
    }
    
    private void processArticleElements(Elements elements, List<Article> articles) {
        for (Element element : elements) {
            String title = element.text().trim();
            String url = element.absUrl("href");
            
            if (!title.isEmpty() && !url.isEmpty() && url.contains("theverge.com")) {
                try {
                    Document articleDoc = Jsoup.connect(url).get();
                    
                    // Try to find the publication date
                    String dateStr = extractPublicationDate(articleDoc);
                    LocalDateTime pubDate = parseDate(dateStr);
                    
                    // Only add articles published from January 1, 2022 onwards
                    if (pubDate != null && !pubDate.toLocalDate().isBefore(CUT_OFF_DATE)) {
                        articles.add(new Article(title, url, pubDate));
                    }
                } catch (Exception e) {
                    // Skip articles that can't be processed
                    System.err.println("Error processing article: " + url);
                }
            }
        }
    }
    
    private String extractPublicationDate(Document doc) {
        // Try different selectors for the publication date
        String dateStr = null;
        
        // Try meta tags first
        Element metaDate = doc.select("meta[property=article:published_time]").first();
        if (metaDate != null) {
            dateStr = metaDate.attr("content");
        }
        
        // If meta tag not found, try time elements
        if (dateStr == null || dateStr.isEmpty()) {
            Element timeElement = doc.select("time").first();
            if (timeElement != null) {
                dateStr = timeElement.attr("datetime");
                if (dateStr.isEmpty()) {
                    dateStr = timeElement.text();
                }
            }
        }
        
        return dateStr;
    }
    
    private LocalDateTime parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        
        try {
            // Try ISO format (2022-01-01T12:00:00Z)
            return LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_DATE_TIME);
        } catch (DateTimeParseException e1) {
            try {
                // Try other common formats
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH);
                return LocalDate.parse(dateStr, formatter).atStartOfDay();
            } catch (DateTimeParseException e2) {
                try {
                    // Try another format
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH);
                    return LocalDate.parse(dateStr, formatter).atStartOfDay();
                } catch (DateTimeParseException e3) {
                    System.err.println("Could not parse date: " + dateStr);
                    return null;
                }
            }
        }
    }
}