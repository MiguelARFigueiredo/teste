package models;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class PaymentReport {

    private static final DateTimeFormatter CSV_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter URL_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final String URL = "http://142.93.170.182:8000/easypay/payments/report?date=";
    private static final long MINIMUM_REQUEST_INTERVAL = 600;
    private static final int MAX_REQUESTS_PER_INTERVAL = 1000;
    private static final long RATE_LIMIT_INTERVAL = 10 * 60 * 1000; // 10 minutes in milliseconds

    private static long lastResetTime = System.currentTimeMillis();
    private static int requestCount = 0;

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Uso: java PaymentReport <dataInicio> <dataFim>");
            System.exit(1);
        }

        LocalDate startDate = LocalDate.parse(args[0], CSV_DATE_FORMAT);
        LocalDate endDate = LocalDate.parse(args[1], CSV_DATE_FORMAT);

        if (startDate.isAfter(endDate)) {
            System.err.println("Data de início não pode ser depois da data de fim.");
            System.exit(1);
        }

        getCorrectRevenuefromEasypay(startDate, endDate);
    }

    public static void getCorrectRevenuefromEasypay(LocalDate startDate, LocalDate endDate) {
        try {
            boolean fileExists = new File("payment_report.csv").exists();
            BufferedWriter writer = new BufferedWriter(new FileWriter("payment_report.csv", true));

            if (!fileExists || new File("payment_report.csv").length() == 0) {
                writer.write("Date,Value,RequestDateTime");
                writer.newLine();
                writer.flush();
                fileExists = true;
            }

            long lastRequestTime = 0;

            for (LocalDate currentDate = startDate; !currentDate.isAfter(endDate); currentDate = currentDate.plusDays(1)) {
                String formattedDateCSV = currentDate.format(CSV_DATE_FORMAT);
                String formattedDateURL = currentDate.format(URL_DATE_FORMAT);
                String urlString = URL + formattedDateURL;

                // Rate limiting logic
                long currentTime = System.currentTimeMillis();
                long timeElapsed = currentTime - lastResetTime;

                if (timeElapsed > RATE_LIMIT_INTERVAL) {
                    lastResetTime = currentTime;
                    requestCount = 0;
                }

                if (requestCount > MAX_REQUESTS_PER_INTERVAL) {
                    long remainingTime = RATE_LIMIT_INTERVAL - (currentTime - lastResetTime);
                    if (remainingTime > 0) {
                        Thread.sleep(remainingTime);
                    }
                    lastResetTime = System.currentTimeMillis();
                    requestCount = 0;
                }

                lastRequestTime = System.currentTimeMillis();

                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                int responseCode = connection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    throw new IOException("HTTP error code: " + responseCode);
                }

                float value;
                try (Scanner responseScanner = new Scanner(connection.getInputStream())) {
                    if (responseScanner.hasNext()) {
                        String response = responseScanner.nextLine().trim();
                        value = Float.parseFloat(response);
                    } else {
                        throw new IOException("Invalid response: no valid float found");
                    }
                } finally {
                    connection.disconnect();
                }

                String formattedValue = String.format("%.2f", value).replace(',', '.');

                LocalDateTime requestDateTime = LocalDateTime.now();
                String formattedRequestDateTime = requestDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

                boolean dateExists = false;
                try (BufferedReader br = new BufferedReader(new FileReader("payment_report.csv"))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        if (line.startsWith(formattedDateCSV + ",")) {
                            dateExists = true;
                            break;
                        }
                    }
                }

                if (dateExists) {
                    File inputFile = new File("payment_report.csv");
                    BufferedReader reader = new BufferedReader(new FileReader(inputFile));
                    StringBuilder fileContent = new StringBuilder();

                    String line;
                    boolean dateUpdated = false;
                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith(formattedDateCSV + ",")) {
                            fileContent.append(formattedDateCSV).append(",").append(formattedValue).append(",").append(formattedRequestDateTime);
                            dateUpdated = true;
                        } else {
                            fileContent.append(line);
                        }
                        fileContent.append(System.lineSeparator());
                    }
                    reader.close();

                    if (!dateUpdated) {
                        fileContent.append(formattedDateCSV).append(",").append(formattedValue).append(",").append(formattedRequestDateTime).append(System.lineSeparator());
                    }

                    PrintWriter fileWriter = new PrintWriter(new BufferedWriter(new FileWriter(inputFile)));
                    fileWriter.write(fileContent.toString());
                    fileWriter.close();

                    System.out.println("Valor atualizado para a data: " + formattedDateCSV);
                } else {
                    writer.write(formattedDateCSV + "," + formattedValue + "," + formattedRequestDateTime);
                    writer.newLine();
                    writer.flush();
                    System.out.println("Dados escritos com sucesso para a data: " + formattedDateCSV + ", " + formattedValue + ", " + formattedRequestDateTime);
                }

                requestCount++;

                Thread.sleep(MINIMUM_REQUEST_INTERVAL);
            }

            writer.close();
            System.out.println("Processamento de todas as datas concluído.");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
