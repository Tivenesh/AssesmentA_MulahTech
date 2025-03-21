package com.example.titleaggregator.controller;

import com.example.titleaggregator.service.ScraperService;
import com.example.titleaggregator.model.Article;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

@Controller
public class HomeController {

    private final ScraperService scraperService;

    public HomeController(ScraperService scraperService) {
        this.scraperService = scraperService;
    }

    @GetMapping("/")
    public String home(Model model) {
        try {
            List<Article> articles = scraperService.fetchArticles();
            model.addAttribute("articles", articles);
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Failed to fetch articles: " + e.getMessage());
        }
        return "index";
    }
}