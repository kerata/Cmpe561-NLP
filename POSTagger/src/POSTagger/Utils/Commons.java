package POSTagger.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Set;

public class Commons {

    public static boolean VERBOSE = false;

    private static final String ANSI_BLACK = "\u001B[30m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BLUE = "\u001B[34m";

    public static void printlnBlack(String text) {
        if (VERBOSE)
            System.out.println(ANSI_BLACK + text);
    }

    public static void printlnRed(String text) {
        if (VERBOSE)
            System.out.println(ANSI_RED + text);
    }

    public static void printlnYellow(String text) {
        if (VERBOSE)
            System.out.println(ANSI_YELLOW + text);
    }

    public static void printlnBlue(String text) {
        if (VERBOSE)
            System.out.println(ANSI_BLUE + text);
    }

    public static void printBlack(String text) {
        if (VERBOSE)
            System.out.print(ANSI_BLACK + text);
    }

    public static void printRed(String text) {
        if (VERBOSE)
            System.out.print(ANSI_RED + text);
    }

    public static void printYellow(String text) {
        if (VERBOSE)
            System.out.print(ANSI_YELLOW + text);
    }

    public static void printBlue(String text) {
        if (VERBOSE)
            System.out.print(ANSI_BLUE + text);
    }

    public static void fPrintlnBlack(String text) {
        System.out.println(ANSI_BLACK + text);
    }

    public static void fPrintlnRed(String text) {
        System.out.println(ANSI_RED + text);
    }

    public static void fPrintlnYellow(String text) {
        System.out.println(ANSI_YELLOW + text);
    }

    public static void fPrintlnBlue(String text) {
        System.out.println(ANSI_BLUE + text);
    }

    public static void fPrintBlack(String text) {
        System.out.print(ANSI_BLACK + text);
    }

    public static void fPrintRed(String text) {
        System.out.print(ANSI_RED + text);
    }

    public static void fPrintYellow(String text) {
        System.out.print(ANSI_YELLOW + text);
    }

    public static void fPrintBlue(String text) {
        System.out.print(ANSI_BLUE + text);
    }

    public static String getKeyword(Token token, LModel model) {
        return model.isUsingForm() ? token.getForm() : token.getLemma();
    }

    public static String getPosTag(Token token, LModel model) {
        return model.isPosTag() ? token.getPosTag() : token.getcPosTag();
    }

    public static void printPrecisions(Map<String, Tuple<Integer, Integer>> scores) {
        final Tuple<Integer, Integer> totalScores = new Tuple<>(0, 0);
        scores.forEach((tag, score) -> {
            totalScores.setFirst(totalScores.getFirst() + score.getFirst());
            totalScores.setSecond(totalScores.getSecond() + score.getSecond());
            float precision = score.getFirst() * 1f / (score.getFirst() + score.getSecond());
            Commons.printYellow(String.format("%-10s precision: %f", tag, precision));
            Commons.printlnYellow(String.format("\tTP: %d FP: %d", score.getFirst(), score.getSecond()));
        });
        float precision = totalScores.getFirst() * 1f / (totalScores.getFirst() + totalScores.getSecond());
        Commons.printRed(String.format("%-10s precision: %f", "total", precision));
        Commons.printlnRed(String.format("\tTP: %d FP: %d", totalScores.getFirst(), totalScores.getSecond()));
    }

    public static void printConfusionMatrix(Map<String, Map<String, Integer>> confusion) {
        Commons.printBlack("\t");
        Set<String> tagSet = confusion.keySet();
        tagSet.forEach(tag -> Commons.printYellow(String.format("%s\t", tag)));
        Commons.printlnBlack("");
        confusion.forEach((goldTag, distribution) -> {
            Commons.printBlue(String.format("%s\t", goldTag));
            tagSet.forEach(tag -> Commons.printBlack(String.format("%d\t", distribution.getOrDefault(tag, 0))));
            Commons.printlnBlack("");
        });
    }

    public static void printToFile(String fullPath, String text) {
        int i = fullPath.lastIndexOf("/");
        printToFile(fullPath.substring(0, i), fullPath.substring(i, fullPath.lastIndexOf(".")), text);
    }

    public static String printToFile(String path, String fileName, String text) {
        try {
            // TODO Remove this path absolute path later...
            path = path == null ? "./results/" : path;
            File file = new File(path);
            if(!file.exists())
                if (!file.mkdirs())
                    throw new RuntimeException("Could not created folders...");
            String fullPath = file.getAbsoluteFile() + "/" + fileName + ".txt";
            PrintWriter writer = new PrintWriter(fullPath, "UTF-8");
            writer.println(text);
            writer.close();

            return fullPath;
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();

            return "";
        }
    }
}
