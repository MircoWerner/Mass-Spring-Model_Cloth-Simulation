package renderengine.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Mirco Werner
 */
public final class IOUtils {
    private IOUtils() throws IllegalAccessException {
        throw new IllegalAccessException("Utility class constructor.");
    }

    public static String readAllLines(String resourceName) throws IOException {
        return readAllLines(IOUtils.class.getClassLoader(), resourceName);
    }

    public static String readAllLines(ClassLoader classLoader, String fileName) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(classLoader.getResourceAsStream(fileName))));
        String lines = bufferedReader.lines().collect(Collectors.joining(System.lineSeparator()));
        bufferedReader.close();
        return lines;
    }

    public static List<String> readAllLinesAsList(String resourceName) throws IOException {
        return readAllLinesAsList(IOUtils.class.getClassLoader(), resourceName);
    }

    public static List<String> readAllLinesAsList(ClassLoader classLoader, String fileName) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(classLoader.getResourceAsStream(fileName))));
        List<String> lines = bufferedReader.lines().collect(Collectors.toList());
        bufferedReader.close();
        return lines;
    }
}
