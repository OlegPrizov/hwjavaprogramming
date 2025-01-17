import java.io.*;
import java.nio.charset.StandardCharsets;

public class LinkGenerator {

    // Метод для загрузки символов из файла в папке resources
    public static String loadSymbolsFromFile(String fileName) {
        StringBuilder symbols = new StringBuilder();

        try (InputStream inputStream = LinkGenerator.class.getClassLoader().getResourceAsStream(fileName);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                symbols.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return symbols.toString();
    }

    // Метод, который генерирует случайную строку заданной длины
    public static String generateRandomString(int length) {
        // Загружаем символы из файла
        String allSymbols = loadSymbolsFromFile("symbols.txt");

        StringBuilder newSymbols = new StringBuilder(length);

        // Генерация случайных символов
        for (int i = 0; i < length; i++) {
            int randomIndex = (int) (Math.random() * allSymbols.length());
            newSymbols.append(allSymbols.charAt(randomIndex));
        }

        return newSymbols.toString();
    }

    public static void main(String[] args) {
        // Пример использования метода с длиной строки 7
        System.out.println(generateRandomString(7));
    }
}
