# Quotes Scraper & Login Automation Tool

A Java + Selenium WebDriver automation project that logs into a website, navigates through paginated content, and extracts structured data into a CSV file.

Built against [quotes.toscrape.com](https://quotes.toscrape.com) — a sandbox site designed for practicing web automation and scraping.

## Features

- **Login automation** — simulates user authentication via form fields
- **Pagination handling** — automatically detects and clicks through all pages until no "Next" button remains
- **Data extraction** — pulls quote text, author, and tags from each page
- **CSV export** — writes results to `quotes.csv` with proper escaping for commas/quotes in the data
- **Explicit waits** — uses `WebDriverWait` + `ExpectedConditions` instead of hardcoded sleeps, avoiding flaky failures on slow page loads
- **Auto driver management** — uses WebDriverManager to download the correct ChromeDriver version automatically, no manual setup required

## Tech Stack

- Java 11
- Selenium WebDriver 4.25
- Maven (build & dependency management)
- WebDriverManager (automatic browser driver handling)

## Project Structure

```
.
├── pom.xml
└── src
    └── main
        └── java
            └── com
                └── automation
                    └── QuotesAutomation.java
```

## Prerequisites

- Java 11 or higher
- Maven 3.6+
- Google Chrome installed

## Setup & Run

Clone the repo:

```bash
git clone <your-repo-url>
cd quotes-automation
```

Run directly with Maven:

```bash
mvn exec:java
```

Or build a standalone executable JAR:

```bash
mvn package
java -jar target/quotes-automation-1.0.0.jar
```

## Output

After running, a `quotes.csv` file is generated in the project root with the following columns:

| Column | Description                  |
|--------|-------------------------------|
| text   | The quote text                |
| author | The quote's author            |
| tags   | Comma-separated list of tags  |

## How It Works

1. Launches a headless Chrome browser via Selenium
2. Navigates to the login page and submits credentials
3. Confirms login success by checking for a "Logout" link
4. Loops through each page of quotes:
   - Extracts quote text, author, and tags for every quote block
   - Clicks "Next" if available, otherwise stops
5. Writes all collected data to `quotes.csv`

## Possible Extensions

- Parameterize the target URL and credentials via command-line args or a config file
- Add logging with SLF4J instead of `System.out`
- Add unit tests with JUnit and Mockito for the parsing logic
- Schedule periodic runs with a cron job / Windows Task Scheduler
- Extend to a non-headless mode with a CLI flag for debugging

## License

This project is for educational/portfolio purposes.
