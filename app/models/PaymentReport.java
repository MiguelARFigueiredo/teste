package models;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class PaymentReport {

    // Formato da data utilizado para formatação no ficheiro CSV
    private static final DateTimeFormatter CSV_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // Formato da data utilizado para formatação na URL
    private static final DateTimeFormatter URL_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    // URL base para consulta dos relatórios de pagamento
    private static final String URL = "http://142.93.170.182:8000/easypay/payments/report?date=";

    // Tempo mínimo entre as requisições (em milissegundos)
    private static final long MINIMUM_REQUEST_INTERVAL = 600;

    public static void main(String[] args) {
        // Verifica se foram fornecidos os argumentos corretos (data de início e data de fim)
        if (args.length < 2) {
            System.err.println("Uso: java PaymentReport <dataInicio> <dataFim>");
            System.exit(1);
        }

        // Parse das datas de início e fim fornecidas como argumentos
        LocalDate startDate = LocalDate.parse(args[0], CSV_DATE_FORMAT);
        LocalDate endDate = LocalDate.parse(args[1], CSV_DATE_FORMAT);

        // Verifica se a data de início é antes da data de fim
        if (startDate.isAfter(endDate)) {
            System.err.println("Data de início não pode ser depois da data de fim.");
            System.exit(1);
        }

        // Chama a função para iniciar o processo de gerar relatórios com as datas fornecidas
        getCorrectRevenuefromEasypay(startDate, endDate);
    }

    public static void getCorrectRevenuefromEasypay(LocalDate startDate, LocalDate endDate) {
        try {
            // Verifica se o ficheiro de relatório existe
            boolean fileExists = new File("payment_report.csv").exists();

            // Abre o ficheiro "payment_report.csv" para escrita (append true para adicionar ao ficheiro existente)
            BufferedWriter writer = new BufferedWriter(new FileWriter("payment_report.csv", true));

            // Se o ficheiro não existir ou estiver vazio, escreve os cabeçalhos
            if (!fileExists || new File("payment_report.csv").length() == 0) {
                writer.write("Date,Value,RequestDateTime");
                writer.newLine();
                writer.flush(); // Certifica-se de que os cabeçalhos são escritos imediatamente
                fileExists = true; // Marca que o ficheiro existe após a primeira execução
            }

            // Contador para controlar o tempo mínimo entre as requisições
            long lastRequestTime = 0;

            // Loop para processar cada data até endDate
            for (LocalDate currentDate = startDate; !currentDate.isAfter(endDate); currentDate = currentDate.plusDays(1)) {
                // Formata a data atual no formato específico para o ficheiro CSV
                String formattedDateCSV = currentDate.format(CSV_DATE_FORMAT);

                // Formata a data atual no formato específico para a URL
                String formattedDateURL = currentDate.format(URL_DATE_FORMAT);

                // Constrói a URL completa para consulta
                String urlString = URL + formattedDateURL;

                try {
                    // Calcula o tempo decorrido desde a última requisição
                    long currentTime = System.currentTimeMillis();
                    long timeElapsed = currentTime - lastRequestTime;

                    // Se o tempo decorrido for menor que o mínimo, aguarda o tempo restante
                    if (timeElapsed < MINIMUM_REQUEST_INTERVAL) {
                        Thread.sleep(MINIMUM_REQUEST_INTERVAL - timeElapsed);
                    }

                    // Cria um objeto URL com a string urlString
                    URL url = new URL(urlString);

                    // Abre uma conexão HTTP com a URL
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                    // Define o método da requisição HTTP como GET
                    connection.setRequestMethod("GET");

                    // Obtém o código de resposta HTTP
                    int responseCode = connection.getResponseCode();

                    // Verifica se a resposta foi bem-sucedida
                    if (responseCode != HttpURLConnection.HTTP_OK) {
                        throw new IOException("HTTP error code: " + responseCode);
                    }

                    // Lê a resposta da conexão
                    float value;
                    try (Scanner responseScanner = new Scanner(connection.getInputStream())) {
                        if (responseScanner.hasNext()) {
                            // Lê a resposta e remove espaços em branco
                            String response = responseScanner.nextLine().trim();

                            // Converte a resposta para float
                            value = Float.parseFloat(response);
                        } else {
                            throw new IOException("Invalid response: no valid float found");
                        }
                    } finally {
                        // Fecha a conexão após o uso
                        connection.disconnect();
                    }

                    // Formata o valor para duas casas decimais e substitui vírgulas por pontos, se houver
                    String formattedValue = String.format("%.2f", value).replace(',', '.');

                    // Obtém a data e hora atual
                    LocalDateTime requestDateTime = LocalDateTime.now();
                    String formattedRequestDateTime = requestDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

                    // Verifica se a data já existe no ficheiro
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
                        // Atualiza o valor no ficheiro
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

                        System.out.println("Updated value for date: " + formattedDateCSV);
                    } else {
                        // Caso contrário, escreve uma nova entrada no ficheiro
                        writer.write(formattedDateCSV + "," + formattedValue + "," + formattedRequestDateTime);
                        writer.newLine();
                        writer.flush(); // Certifica-se de que os dados são escritos imediatamente
                        System.out.println("Successfully wrote data for date: " + formattedDateCSV + ", " + formattedValue + ", " + formattedRequestDateTime);

                    }

                    // Atualiza o tempo da última requisição
                    lastRequestTime = System.currentTimeMillis();

                    // Aguarda um curto período de tempo entre as iterações
                    Thread.sleep(MINIMUM_REQUEST_INTERVAL);
                } catch (IOException e) {
                    System.err.println("Error fetching data for date " + formattedDateCSV + ": " + e.getMessage());
                }

                // Aguarda um curto período de tempo entre as iterações
                Thread.sleep(MINIMUM_REQUEST_INTERVAL);
            }

            writer.close();
            System.out.println("Finished processing all dates.");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
