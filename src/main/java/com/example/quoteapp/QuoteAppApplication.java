package com.example.quoteapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class QuoteAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuoteAppApplication.class, args);
    }

    @RestController
    class QuoteController {
        @GetMapping("/")
        public String getQuote() {
            return """
                    <html>
                        <head>
                            <title>Positive Quote</title>
                            <style>
                                body {
                                    display: flex;
                                    justify-content: center;
                                    align-items: center;
                                    height: 100vh;
                                    background-color: #caf0f8;
                                    font-family: Arial, sans-serif;
                                    color: #333;
                                }
                                .quote-container {
                                    background-color: #fff;
                                    padding: 20px;
                                    border-radius: 10px;
                                    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
                                    text-align: center;
                                }
                                h1 {
                                    font-size: 24px;
                                    margin-bottom: 20px;
                                }
                            </style>
                        </head>
                        <body>
                            <div class="quote-container">
                                <h1>Keep pushing forward, no matter what!</h1>
                            </div>
                        </body>
                    </html>
                    """;
        }
    }
}
