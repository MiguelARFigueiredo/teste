import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

public class PaymentReport {

    // Formato da data utilizado para formatação no arquivo CSV
    private static final DateTimeFormatter CSV_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // Formato da data utilizado para formatação na URL
    private static final DateTimeFormatter URL_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    // URL base para consulta dos relatórios de pagamento
    private static final String URL = "http://142.93.170.182:8000/easypay/payments/report?date=";

    // Número máximo de chamadas permitidas por dez minutos -> No máximo 1000
    private static final int MAX_CALLS = 999;

    public static void main(String[] args) {
        // Timer para agendar a tarefa de gerar um relatório
        Timer timer = new Timer();

        // Utilizando um array de tamanho 1 para armazenar a variável fileExists
        final boolean[] fileExists = new boolean[1];
        fileExists[0] = new File("payment_report.csv").exists();

        // Definindo a hora de início (12:30 PM)
        Calendar startTime = Calendar.getInstance();
        startTime.set(Calendar.HOUR_OF_DAY, 16);
        startTime.set(Calendar.MINUTE, 21);
        startTime.set(Calendar.SECOND, 0);
        startTime.set(Calendar.MILLISECOND, 0);

        // Se a hora de início for antes do horário atual, agende para o próximo dia
        if (startTime.getTime().before(Calendar.getInstance().getTime())) {
            startTime.add(Calendar.DAY_OF_MONTH, 1);
        }

        // Calcula o período em milissegundos para repetir a tarefa diariamente
        long period = 24 * 60 * 60 * 1000; // 24 horas

        // Agenda a tarefa para ser executada diariamente às 12:30 PM
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                executeReportGeneration(fileExists);
            }
        }, startTime.getTime(), period);

        // Loop infinito para manter o programa em execução até ser interrompido manualmente (Ctrl+C)
        while (true) {
            try {
                // Aguarda 1 segundo antes de verificar novamente
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // Captura e imprime qualquer exceção InterruptedException ocorrida durante o loop
                e.printStackTrace();
            }
        }
    }

    // Método principal que executa a geração do relatório
    private static void executeReportGeneration(boolean[] fileExists) {
        // Contador de chamadas nos últimos dez minutos
        int callsInLastTenMinutes = 0;

        // Tempo da última chamada
        long lastCallTime = System.currentTimeMillis();

        // Data de início dos relatórios
        //LocalDate startDate = LocalDate.of(2023, 6, 1);
        LocalDate startDate = LocalDate.now().minusWeeks(1);


        // Data de término será a data atual
        LocalDate endDate = LocalDate.now();

        // Data atual para começar a procura
        LocalDate currentDate = startDate;

        try {
            // Abre o ficheiro "payment_report.csv" para escrita (append true para adicionar ao ficheiro existente)
            BufferedWriter writer = new BufferedWriter(new FileWriter("payment_report.csv", true));

            // Se o ficheiro não existir, escreve os cabeçalhos
            if (!fileExists[0]) {
                writer.write("Date,Value");
                writer.newLine();
                fileExists[0] = true; // Modifica a variável fileExists através do array
            }

            // Loop para processar cada data até endDate
            while (!currentDate.isAfter(endDate)) {
                // Formata a data atual no formato específico para o arquivo CSV
                String formattedDateCSV = currentDate.format(CSV_DATE_FORMAT);

                // Formata a data atual no formato específico para a URL
                String formattedDateURL = currentDate.format(URL_DATE_FORMAT);

                // Constrói a URL completa para consulta
                String urlString = URL + formattedDateURL;

                try {
                    // Procura o valor do pagamento da URL
                    float value = fetchPaymentValue(urlString);

                    // Formata o valor para duas casas decimais e substitui vírgulas por pontos, se houver
                    String formattedValue = String.format("%.2f", value).replace(',', '.');

                    // Verifica se a data já existe no ficheiro
                    if (doesDateExistInFile("payment_report.csv", formattedDateCSV)) {
                        // Se existir, atualiza o valor no ficheiro
                        updateValueInFile("payment_report.csv", formattedDateCSV, formattedValue);
                    } else {
                        // Caso contrário, escreve uma nova entrada no ficheiro
                        writer.write(formattedDateCSV + "," + formattedValue);
                        writer.newLine();
                    }

                    // Exibe mensagem de sucesso para a data processada
                    System.out.println("Successfully wrote data for date: " + formattedDateCSV);

                    // Incrementa o contador de chamadas nos últimos dez minutos
                    callsInLastTenMinutes++;

                    // Obtém o tempo atual
                    long currentTime = System.currentTimeMillis();

                    // Verifica se o limite de chamadas nos últimos dez minutos foi excedido
                    if (callsInLastTenMinutes >= MAX_CALLS && (currentTime - lastCallTime) < 600000) {
                        // Calcula o tempo de espera necessário para não exceder o limite
                        long sleepTime = 600000 - (currentTime - lastCallTime);
                        System.out.println("Exceeded maximum calls in 10 minutes. Sleeping for " + sleepTime + " milliseconds.");

                        // Aguarda o tempo de espera calculado
                        Thread.sleep(sleepTime);

                        // Reseta o contador de chamadas nos últimos dez minutos
                        callsInLastTenMinutes = 0;
                    }

                    // Atualiza o tempo da última chamada
                    lastCallTime = System.currentTimeMillis();
                } catch (IOException e) {
                    // Captura e imprime qualquer exceção IOException ocorrida durante a execução
                    System.err.println("Error fetching data for date " + formattedDateCSV + ": " + e.getMessage());
                }

                // Avança para o próximo dia
                currentDate = currentDate.plusDays(1);

                // Aguarda um curto período de tempo entre as iterações
                Thread.sleep(600);
            }

            // Fecha o BufferedWriter após concluir a escrita no ficheiro
            writer.close();
        } catch (IOException | InterruptedException e) {
            // Captura e imprime qualquer exceção IOException ou InterruptedException ocorrida durante a execução
            e.printStackTrace();
        }

        System.out.println("Finished processing all dates.");
    }

    // Método para procurar o valor do pagamento da URL fornecida
    private static float fetchPaymentValue(String urlString) throws IOException {
        // Cria um objeto URL com a string urlString
        URL url = new URL(urlString);

        // Abre uma conexão HTTP com a URL
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        // Define o método da requisição HTTP como GET
        connection.setRequestMethod("GET");

        try {
            // Obtém o código de resposta HTTP
            int responseCode = connection.getResponseCode();

            // Verifica se a resposta foi bem-sucedida
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("HTTP error code: " + responseCode);
            }

            // Tenta ler a resposta da conexão
            try (Scanner scanner = new Scanner(connection.getInputStream())) {
                if (scanner.hasNext()) {
                    // Lê a resposta e remove espaços em branco
                    String response = scanner.nextLine().trim();

                    // Converte a resposta para float e retorna
                    return Float.parseFloat(response);
                } else {
                    throw new IOException("Invalid response: no valid float found");
                }
            }
        } catch (IOException e) {
            // Captura e imprime qualquer exceção IOException ocorrida durante a requisição HTTP
            System.err.println("Error fetching data from URL: " + urlString);
            System.err.println("Error message: " + e.getMessage());
            throw e;
        } finally {
            // Fecha a conexão após o uso
            connection.disconnect();
        }
    }

    // Método para verificar se a data já existe no ficheiro CSV
    private static boolean doesDateExistInFile(String filename, String date) throws IOException {
        // Abre o ficheiro especificado para leitura usando BufferedReader
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;

            // Lê cada linha do ficheiro
            while ((line = br.readLine()) != null) {
                // Verifica se a linha começa com a data fornecida
                if (line.startsWith(date + ",")) {
                    return true;
                }
            }
        }

        // Retorna false se a data não for encontrada no ficheiro
        return false;
    }

    // Método para atualizar o valor para uma data específica no ficheiro CSV
    private static void updateValueInFile(String filename, String date, String value) throws IOException {
        // Lê todas as linhas do arquivo original
        File inputFile = new File(filename);
        BufferedReader reader = new BufferedReader(new FileReader(inputFile));
        StringBuilder fileContent = new StringBuilder();

        String line;
        boolean dateUpdated = false;

        // Lê cada linha do arquivo original
        while ((line = reader.readLine()) != null) {
            // Se encontrar a linha correspondente à data fornecida, escreve a linha atualizada no StringBuilder
            if (line.startsWith(date + ",")) {
                fileContent.append(date).append(",").append(value);
                dateUpdated = true;
            } else {
                // Caso contrário, escreve a linha original no StringBuilder
                fileContent.append(line);
            }
            fileContent.append(System.lineSeparator());
        }
        reader.close();

        // Se a data não foi encontrada no arquivo original, adiciona ao final
        if (!dateUpdated) {
            fileContent.append(date).append(",").append(value).append(System.lineSeparator());
        }

        // Escreve o conteúdo atualizado de volta ao arquivo original
        PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(inputFile)));
        writer.write(fileContent.toString());
        writer.close();
    }
}
