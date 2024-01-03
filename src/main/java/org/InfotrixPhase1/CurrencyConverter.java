package org.InfotrixPhase1;

import java.net.HttpURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import com.google.gson.JsonObject;
import com.google.gson.Gson;

public class CurrencyConverter {
    private static final String API_KEY = "f07672c8d0ff8bc73ef74a211c9d71d9";
    private static final String API_URL = "http://api.exchangeratesapi.io/v1/latest?access_key=" + API_KEY;

    private List<CurrencyRate> exchangeRates;
    private List<CurrencyRate> favorites;

    public CurrencyConverter() {
        this.exchangeRates = new ArrayList<>();
        this.favorites = new ArrayList<>();
        initializeRates();
    }

    private void initializeRates() {
        try {
            URL apiUrl = new URL(API_URL);
            HttpURLConnection connection = (HttpURLConnection) apiUrl.openConnection();
            connection.setRequestMethod("GET");

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                parseRates(response.toString());
            }
        } catch (IOException e) {
            System.out.println("Error connecting to the API: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void parseRates(String jsonResponse) {
        try {
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(jsonResponse, JsonObject.class);

            if (jsonObject.has("rates") && jsonObject.get("rates").isJsonObject()) {
                JsonObject ratesObject = jsonObject.getAsJsonObject("rates");

                for (String currencyCode : ratesObject.keySet()) {
                    double exchangeRate = ratesObject.get(currencyCode).getAsDouble();
                    exchangeRates.add(new CurrencyRate(currencyCode, exchangeRate));
                }
            } else {
                System.out.println("Error: Missing or invalid 'rates' object in JSON response.");
            }
        } catch (Exception e) {
            System.out.println("Error parsing JSON response: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private double convert(double amount, String fromCurrency, String toCurrency) {
        fromCurrency = fromCurrency.toUpperCase();
        toCurrency = toCurrency.toUpperCase();

        CurrencyRate fromRate = findCurrencyRate(fromCurrency);
        CurrencyRate toRate = findCurrencyRate(toCurrency);

        if (fromRate != null && toRate != null) {
            return amount * (toRate.getRate() / fromRate.getRate());
        } else {
            System.out.println("Invalid currency code. Please check and try again.");
            return -1.0;
        }
    }

    private CurrencyRate findCurrencyRate(String currencyCode) {
        for (CurrencyRate rate : exchangeRates) {
            if (rate.getCurrencyCode().equalsIgnoreCase(currencyCode)) {
                return rate;
            }
        }
        return null;
    }

    private void displaySupportedCurrencies() {
        System.out.println("Supported currencies:");
        for (CurrencyRate rate : exchangeRates) {
            System.out.println(rate.getCurrencyCode());
        }
    }

    private void addFavorite(String currency) {
        CurrencyRate rate = findCurrencyRate(currency);
        if (rate != null) {
            favorites.add(rate);
            System.out.println(currency + " added to favorites.");
        } else {
            System.out.println("Invalid currency code. Please check and try again.");
        }
    }

    private void removeFavorite(String currency) {
        CurrencyRate rateToRemove = null;
        for (CurrencyRate rate : favorites) {
            if (rate.getCurrencyCode().equalsIgnoreCase(currency)) {
                rateToRemove = rate;
                break;
            }
        }

        if (rateToRemove != null) {
            favorites.remove(rateToRemove);
            System.out.println(currency + " removed from favorites.");
        } else {
            System.out.println("Currency not found in favorites.");
        }
    }

    private void viewFavorites() {
        System.out.println("Favorite currencies:");
        for (CurrencyRate rate : favorites) {
            System.out.println(rate.getCurrencyCode() + ": " + rate.getRate());
        }
    }

    public static void main(String[] args) {
        CurrencyConverter converter = new CurrencyConverter();
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.println("Options:");
                System.out.println("1. Convert");
                System.out.println("2. Add to Favorites");
                System.out.println("3. Remove from Favorites");
                System.out.println("4. View Favorites");
                System.out.println("5. Supported Currency Codes");
                System.out.println("6. Exit");

                int choice = scanner.nextInt();
                scanner.nextLine();

                switch (choice) {
                    case 1:
                        System.out.println("Enter amount:");
                        double amount = scanner.nextDouble();
                        scanner.nextLine();

                        System.out.println("Enter source currency code:");
                        String fromCurrency = scanner.nextLine().toUpperCase();

                        System.out.println("Enter target currency code:");
                        String toCurrency = scanner.nextLine().toUpperCase();

                        double result = converter.convert(amount, fromCurrency, toCurrency);
                        System.out.println("Converted amount: " + result);
                        break;

                    case 2:
                        System.out.println("Enter currency code to add to favorites:");
                        String favoriteCurrency = scanner.nextLine().toUpperCase();
                        converter.addFavorite(favoriteCurrency);
                        break;

                    case 3:
                        System.out.println("Enter currency code to remove from favorites:");
                        String removeCurrency = scanner.nextLine().toUpperCase();
                        converter.removeFavorite(removeCurrency);
                        break;

                    case 4:
                        converter.viewFavorites();
                        break;

                    case 5:
                        converter.displaySupportedCurrencies();
                        break;

                    case 6:
                        System.out.println("Exiting...");
                        System.exit(0);
                        break;

                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            }
        }
    }
}

class CurrencyRate {
    private String currencyCode;
    private double rate;

    public CurrencyRate(String currencyCode, double rate) {
        this.currencyCode = currencyCode;
        this.rate = rate;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public double getRate() {
        return rate;
    }
}
