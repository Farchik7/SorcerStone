import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Main {
    // Винесено «магічне число» у константу замість жорсткого зашивання в циклі
    private static final int TOP_LIMIT = 30;
    
    // Оптимізація пам'яті: попередньо скомпільований патерн для регулярних виразів
    private static final Pattern WORD_PATTERN = Pattern.compile("[^A-Za-z ]+");

    public static void main(String[] args) throws IOException {
        LocalDateTime start = LocalDateTime.now();

        // Поділ логіки на окремі виклики методів
        String path = "src/txt/harry.txt";
        
        // Оптимізація пам'яті: потоковий підрахунок без завантаження всього файлу в масив
        Map<String, Long> wordFrequencies = countWordsOptimized(path);
        
        printTopWords(wordFrequencies, TOP_LIMIT);

        LocalDateTime finish = LocalDateTime.now();
        System.out.println("------");
        System.out.println(ChronoUnit.MILLIS.between(start, finish));
    }

    // Окремий метод для безпечного зчитування файлу (збережено для сумісності)
    private static String readTextFile(String path) throws IOException {
        return new String(Files.readAllBytes(Paths.get(path)));
    }

    // Окремий метод для очищення тексту та розбиття на окремі слова 
    // (виправлено баг із підрядками: тепер split коректно формує список слів)
    private static List<String> extractWords(String content) {
        String cleaned = content.replaceAll("[^A-Za-z ]", " ").toLowerCase(Locale.ROOT);
        return Arrays.asList(cleaned.split("\\s+"));
    }

    // Окремий метод для ефективного підрахунку частоти без вкладених циклів O(N^2)
    private static Map<String, Long> countWords(List<String> words) {
        return words.stream()
                .filter(w -> !w.isEmpty())
                .collect(Collectors.groupingBy(w -> w, Collectors.counting()));
    }

    // НОВИЙ МЕТОД ОПТИМІЗАЦІЇ ПАМ'ЯТІ: потокове читання рядків (Files.lines) замість масивів
    private static Map<String, Long> countWordsOptimized(String path) throws IOException {
        try (var lines = Files.lines(Paths.get(path), StandardCharsets.ISO_8859_1)) {
            return lines
                    .parallel() // Прискорення за рахунок паралельної обробки
                    .map(line -> WORD_PATTERN.matcher(line).replaceAll(" ").toLowerCase(Locale.ROOT))
                    .flatMap(line -> Arrays.stream(line.split("\\s+")))
                    .filter(w -> !w.isEmpty())
                    .collect(Collectors.groupingByConcurrent(w -> w, Collectors.counting()));
        }
    }

    // Окремий метод для сортування за популярністю та виводу результату
    private static void printTopWords(Map<String, Long> wordFrequencies, int limit) {
        wordFrequencies.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(limit)
                .forEach(entry -> System.out.println(entry.getKey() + " " + entry.getValue()));
    }
}
