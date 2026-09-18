package com.automation;

/*
 * Automation Project: Login + Scrape Quotes
 * Website: https://quotes.toscrape.com (a sandbox site made for practicing automation)
 *
 * What this program does:
 * 1. Opens Chrome using Selenium
 * 2. Logs in to the site (any username/password works -- it's a demo login)
 * 3. Navigates through all pages of quotes
 * 4. Extracts: quote text, author, tags
 * 5. Saves everything to quotes.csv
 *
 * Run:
 *   mvn exec:java
 * or build a jar and run it:
 *   mvn package
 *   java -jar target/quotes-automation-1.0.0.jar
 */

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class QuotesAutomation {

    private static final String BASE_URL = "https://quotes.toscrape.com";
    private static final String LOGIN_URL = BASE_URL + "/login";
    private static final String OUTPUT_FILE = "quotes.csv";

    // Demo credentials -- the site accepts any non-empty username/password
    private static final String USERNAME = "demo_user";
    private static final String PASSWORD = "demo_pass";

    public static void main(String[] args) {
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1280,900");

        ChromeDriver driver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        try {
            login(driver, wait);
            List<Quote> quotes = scrapeAllQuotes(driver, wait);
            saveToCsv(quotes, OUTPUT_FILE);
        } finally {
            driver.quit();
            System.out.println("[+] Browser closed. Done.");
        }
    }

    private static void login(ChromeDriver driver, WebDriverWait wait) {
        driver.get(LOGIN_URL);

        WebElement usernameField = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.id("username")));
        WebElement passwordField = driver.findElement(By.id("password"));

        usernameField.sendKeys(USERNAME);
        passwordField.sendKeys(PASSWORD);

        driver.findElement(By.cssSelector("input[type='submit']")).click();

        // Confirm login succeeded (site shows a "Logout" link once logged in)
        wait.until(ExpectedConditions.presenceOfElementLocated(By.linkText("Logout")));
        System.out.println("[+] Logged in successfully.");
    }

    private static List<Quote> scrapeAllQuotes(ChromeDriver driver, WebDriverWait wait) {
        List<Quote> allQuotes = new ArrayList<>();
        int pageNum = 1;

        while (true) {
            wait.until(ExpectedConditions.presenceOfElementLocated(By.className("quote")));
            List<WebElement> quoteBlocks = driver.findElements(By.className("quote"));

            for (WebElement block : quoteBlocks) {
                String text = block.findElement(By.className("text")).getText();
                String author = block.findElement(By.className("author")).getText();
                List<WebElement> tagElements = block.findElements(By.className("tag"));
                String tags = tagElements.stream()
                        .map(WebElement::getText)
                        .collect(Collectors.joining(", "));

                allQuotes.add(new Quote(text, author, tags));
            }

            System.out.println("[+] Scraped page " + pageNum + " (" + quoteBlocks.size() + " quotes)");

            List<WebElement> nextButtons = driver.findElements(By.cssSelector(".next > a"));
            if (nextButtons.isEmpty()) {
                break;
            }

            nextButtons.get(0).click();
            pageNum++;

            try {
                Thread.sleep(500); // small pause to let the page load
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        return allQuotes;
    }

    private static void saveToCsv(List<Quote> quotes, String filename) {
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write("text,author,tags\n");
            for (Quote q : quotes) {
                writer.write(q.toCsvRow() + "\n");
            }
            System.out.println("[+] Saved " + quotes.size() + " quotes to " + filename);
        } catch (IOException e) {
            System.err.println("[-] Failed to write CSV: " + e.getMessage());
        }
    }

    /** Simple data holder for a scraped quote. */
    private static class Quote {
        final String text;
        final String author;
        final String tags;

        Quote(String text, String author, String tags) {
            this.text = text;
            this.author = author;
            this.tags = tags;
        }

        String toCsvRow() {
            return escape(text) + "," + escape(author) + "," + escape(tags);
        }

        // Wraps a field in quotes and escapes embedded quotes, so commas/quotes in
        // quote text don't break the CSV format.
        private String escape(String field) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
    }
}
