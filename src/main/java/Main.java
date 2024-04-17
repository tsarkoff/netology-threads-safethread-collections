import java.util.*;
import java.util.concurrent.*;
import java.util.stream.IntStream;

public class Main {
    public static final String CHARS = "abc";          // characters are used in text
    private static final int TEXTS_NUMBER = 10_000;    // number of texts are generated
    private static final int TEXT_LENGTH = 100_000;    // number of letters in one text
    private static final int QUEUE_SIZE = 100;         // size of blocking queue of texts
    private static Thread textGeneratorThread;         // Thread to generate texts
    private static final List<ArrayBlockingQueue<String>> queue = new ArrayList<>(CHARS.length());
    private static final Map<Character, Long> maxCounts = new HashMap<>();

    public static void main(String[] args) throws InterruptedException {
        List<Thread> calcThreads = new ArrayList<>(CHARS.length());
        for (int i = 0; i < CHARS.length(); i++)
            queue.add(new ArrayBlockingQueue<>(QUEUE_SIZE));

        textGeneratorThread = new Thread(() -> {
            for (int i = 0; i < TEXTS_NUMBER; i++) {
                try {
                    for (int j = 0; j < CHARS.length(); j++)
                        queue.get(j).put(generateText());
                } catch (InterruptedException e) {
                    System.out.println("textGeneratorThread : " + e.getMessage());
                }
                progressBar((double) i / TEXTS_NUMBER);
            }
            for (Thread thread : calcThreads) thread.interrupt();
        });
        textGeneratorThread.start();

        for (char ch = 0; ch < CHARS.length(); ch++) {
            calcThreads.add(getNewCalcThread(ch));
            calcThreads.getLast().start();
        }

        for (Thread thread : calcThreads) thread.join();
        System.out.println();
        IntStream.range(0, CHARS.length())
                .forEach(ch -> System.out.printf("Максимальное кол-во букв '%s' в %s текстах длинной %s символов = %s%n",
                        CHARS.charAt(ch),
                        TEXTS_NUMBER,
                        TEXT_LENGTH,
                        maxCounts.get(CHARS.charAt(ch))));
    }

    // CALC THREADS IMPLEMENTATION
    private static Thread getNewCalcThread(char ch) {
        return new Thread(() -> {
            long count = 0;
            while (textGeneratorThread.isAlive()) {
                try {
                    count = Math.max(count, queue.get(ch).take().chars().filter(c -> c == CHARS.charAt(ch)).count());
                } catch (InterruptedException e) {
                    //System.out.println("getNewCalcThread : " + e.getMessage());
                    break;
                }
            }
            maxCounts.put(CHARS.charAt(ch), count);
        });
    }

    // GENERATE 1 (one) TEXT
    private static String generateText() {
        Random random = new Random();
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < TEXT_LENGTH; i++)
            text.append(Main.CHARS.charAt(random.nextInt(Main.CHARS.length())));
        return text.toString();
    }

    // DISPLAY ROUTINE's PROGRESS
    private static void progressBar(double progressPercentage) {
        final int width = 50;
        System.out.print("\rProgress [");
        int i = 0;
        for (; i <= (int) (progressPercentage * width); i++) System.out.print(">");
        System.out.printf("%s%%", Math.round(progressPercentage * 100));
        for (; i < width; i++) System.out.print(".");
        System.out.print("]");
    }
}
