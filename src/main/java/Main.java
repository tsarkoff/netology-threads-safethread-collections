import java.util.*;
import java.util.concurrent.*;

public class Main {
    public static final String CHARS = "abc";          // characters are used in text
    private static final int TEXTS_NUMBER = 10_000;    // number of texts are generated
    private static final int TEXT_LENGTH = 100_000;    // number of letters in one text
    private static final int QUEUE_SIZE = 100;         // size of blocking queue of texts
    private static Thread textGeneratorThread;         // Thread to generate texts
    private static final BlockingQueue<String> queueA = new ArrayBlockingQueue<>(QUEUE_SIZE);
    private static final BlockingQueue<String> queueB = new ArrayBlockingQueue<>(QUEUE_SIZE);
    private static final BlockingQueue<String> queueC = new ArrayBlockingQueue<>(QUEUE_SIZE);

    public static void main(String[] args) throws InterruptedException {
        textGeneratorThread = new Thread(() -> {
            for (int i = 0; i < TEXTS_NUMBER; i++) {
                String text = generateText();
                try {
                    queueA.put(text);
                    queueB.put(text);
                    queueC.put(text);
                } catch (InterruptedException e) {
                    System.out.println("textGeneratorThread : " + e.getMessage());
                }
                progressBar((double) i / TEXTS_NUMBER);
            }
            System.out.println();
        });
        textGeneratorThread.start();

        Thread threadA = getNewCalcThread(queueA, CHARS.charAt(0));
        Thread threadB = getNewCalcThread(queueB, CHARS.charAt(1));
        Thread threadC = getNewCalcThread(queueC, CHARS.charAt(2));

        threadA.start();
        threadB.start();
        threadC.start();

        threadA.join();
        threadB.join();
        threadC.join();

        textGeneratorThread.join();
        queueA.put("¶");
        queueB.put("¶");
        queueC.put("¶");
    }

    // CALC THREADS IMPLEMENTATION
    private static Thread getNewCalcThread(BlockingQueue<String> queue, char ch) {
        return new Thread(() -> {
            long count = 0;
            while (textGeneratorThread.isAlive()) {
                try {
                    String text = queue.take();
                    if (text.equals("¶")) break;
                    count = Math.max(count, text.chars().filter(c -> c == ch).count());
                } catch (InterruptedException e) {
                    System.out.println("getNewCalcThread : " + e.getMessage());
                }
            }
            System.out.printf("MAX кол-во '%s' = %s%n", ch, count);
        });
    }

    // GENERATE 1 (one) TEXT
    private static String generateText() {
        Random random = new Random();
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < TEXT_LENGTH; i++)
            text.append(Main.CHARS.charAt(random.nextInt(CHARS.length())));
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
