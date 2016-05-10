package POSTagger.Utils;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class Parser {

    public interface ParseListener {
        void onTokenDetected(Token token);
        void onNewSentence();
        void onParseEnded();
    }

    public static String defaultTrainingFilePath = "src/main/resources/train/turkish_metu_sabanci_train.conll";

    public static final String testBlindFilePath = "src/main/resources/test/turkish_metu_sabanci_test_blind_sample.conll.txt";
    public static final String testGoldFilePath = "src/main/resources/test/turkish_metu_sabanci_test_gold_sample.conll.txt";

    public static final String validationFilePath = "src/main/resources/validation/turkish_metu_sabanci_val.conll";

    public static final String defaultOutputFilePath = "src/main/results/output.txt";

    public static void train(List<LModel> languageModels) {
        languageModels.stream()
                .forEach(lModel -> lModel.addListener(new ParseListener() {
            @Override
            public void onTokenDetected(Token token) {
                if ("DERIV".equals(token.getDeprel()))
                    lModel.getStats().addInflectionalToken(token);
                else lModel.getStats()
                        .addInflectionalToken(token)
                        .processInflectionalGroup()
                        .addToken(token);
            }

            @Override
            public void onNewSentence() {
                lModel.getStats().endSentence();
            }

            @Override
            public void onParseEnded() {

            }
        }));

        if (defaultTrainingFilePath == null)
            languageModels.stream()
                    .forEach(lModel -> readTTBTokens(lModel.getTrainingFilePath(), lModel.getListeners()));
        else readTTBTokens(defaultTrainingFilePath, languageModels.stream()
                .flatMap(lModel -> lModel.getListeners().stream())
                .collect(Collectors.toList()));

        languageModels.forEach(LModel::clearListeners);
    }

    public static void tag(String path, List<LModel> languageModels) {
        languageModels.stream()
                .filter(LModel::hasTrained)
                .forEach(lModel -> lModel.addListener(new ParseListener() {
            StringBuilder output = new StringBuilder();
            ArrayList<Token> sentence = new ArrayList<>();

            @Override
            public void onTokenDetected(Token token) {
                if (Commons.getKeyword(token, lModel) != null) {
                    Token lastToken = sentence.isEmpty() ? null : sentence.get(sentence.size() - 1);
                    lModel.findNextTagViterbi(lastToken, token);
                    String match = String.format("%s|%s", Commons.getKeyword(token, lModel),
                            Commons.getPosTag(token, lModel));

                    Commons.printlnBlue(match);
                    output.append(match).append("\n");
                    sentence.add(token);
                }
            }

            @Override
            public void onNewSentence() {
                Commons.printlnBlack("");
                output.append("\n");
                sentence.clear();
            }

            @Override
            public void onParseEnded() {
                if (lModel.getOutputFilePath() != null)
                    Commons.printToFile(lModel.getOutputFilePath(), output.toString());
                else
                    lModel.setOutputFilePath(Commons
                            .printToFile(null, "output" + languageModels.indexOf(lModel), output.toString()));
                sentence.clear();
            }
        }));

        path = path == null || path.isEmpty() ? testBlindFilePath : path;
        readTTBTokens(path, languageModels.stream()
                .filter(LModel::hasTrained)
                .flatMap(lModel -> lModel.getListeners().stream())
                .collect(Collectors.toList()));
        languageModels.forEach(LModel::clearListeners);
    }

    public static void evaluate(List<LModel> languageModels) {
        languageModels.forEach(lModel -> {
            final Iterator<Tuple<String, String>> resultIt = readOutputFormat(lModel.getOutputFilePath()).iterator();
            lModel.addListener(new ParseListener() {
                Map<String, Tuple<Integer, Integer>> scores = new HashMap<>();

                Tuple<String, String> result;
                Token currentToken;

                @Override
                public void onTokenDetected(Token token) {
                    if (Commons.getKeyword(token, lModel) == null) return;

                    if (!token.equals(currentToken)) {
                        result = resultIt.next();
                        currentToken = token;
                    }

                    String tag = Commons.getPosTag(currentToken, lModel);
                    if (tag.equals(result.getSecond())) {
                        Commons.printlnBlue(String.format("%s=%10s", result.toString(), tag));
                        if (scores.containsKey(tag)) {
                            Tuple<Integer, Integer> score = scores.get(tag);
                            score.setFirst(score.getFirst() +1);
                        }
                        else scores.put(tag, new Tuple<>(1, 0));
                    }
                    else {
                        Commons.printlnRed(String.format("%sX%10s", result.toString(), tag));
                        if (scores.containsKey(tag)) {
                            Tuple<Integer, Integer> score = scores.get(tag);
                            score.setSecond(score.getSecond() +1);
                        }
                        else scores.put(tag, new Tuple<>(0, 1));
                    }
                }

                @Override
                public void onNewSentence() {
                    resultIt.next();
                    Commons.printlnBlack("");
                }

                @Override
                public void onParseEnded() {
                    Commons.printPrecisions(scores);
                    Commons.printBlack("");
                }
            });

            readTTBTokens(lModel.getGoldFilePath(), lModel.getListeners());
            lModel.clearListeners();
        });
    }

    public static void validate(String validationFilePath, List<LModel> languageModels) {
        languageModels.stream()
                .filter(LModel::hasTrained)
                .forEach(lModel -> lModel.addListener(new ParseListener() {
            Map<String, Tuple<Integer, Integer>> scores = new HashMap<>();
            Map<String, Map<String, Integer>> confusion = new HashMap<>();
            int KT = 0, KF = 0;
            int UKT = 0, UKF = 0;

            ArrayList<Token> sentence = new ArrayList<>();

            @Override
            public void onTokenDetected(Token token) {
                if (Commons.getKeyword(token, lModel) != null) {
                    Token lastToken = sentence.isEmpty() ? null : sentence.get(sentence.size() - 1);

                    String goldStandard = Commons.getPosTag(token, lModel);
                    lModel.findNextTagViterbi(lastToken, token);
                    String tagFound = Commons.getPosTag(token, lModel);

                    confusion.compute(goldStandard, (goldStandardTag, distribution) -> {
                        distribution = distribution == null ? new HashMap<>() : distribution;
                        distribution.compute(tagFound, (s, freq) -> freq == null ? 1 : freq + 1);
                        return distribution;
                    });
                    if (goldStandard.equals(tagFound)) {
                        Commons.printlnBlue(String.format("%-20s: %-10s=%10s",
                                Commons.getKeyword(token, lModel), tagFound, goldStandard));
                        if (scores.containsKey(goldStandard)) {
                            Tuple<Integer, Integer> score = scores.get(goldStandard);
                            score.setFirst(score.getFirst() +1);
                        }
                        else scores.put(goldStandard, new Tuple<>(1, 0));

                        if (lModel.getStats().getWordTagCounts()
                                .get(tagFound).get(Commons.getKeyword(token, lModel)) == null)
                            UKT++;
                        else KT++;
                    }
                    else {
                        Commons.printlnRed(String.format("%-20s: %-10sX%10s",
                                Commons.getKeyword(token, lModel), tagFound, goldStandard));
                        if (scores.containsKey(goldStandard)) {
                            Tuple<Integer, Integer> score = scores.get(goldStandard);
                            score.setSecond(score.getSecond() +1);
                        }
                        else scores.put(goldStandard, new Tuple<>(0, 1));

                        if (lModel.getStats().getWordTagCounts()
                                .get(tagFound).get(Commons.getKeyword(token, lModel)) == null)
                            UKF++;
                        else KF++;
                    }

                    sentence.add(token);
                }
            }

            @Override
            public void onNewSentence() {
                sentence.clear();
                Commons.printlnBlack("");
            }

            @Override
            public void onParseEnded() {
                sentence.clear();
                Commons.printPrecisions(scores);
                Commons.printlnBlack("");
                Commons.printConfusionMatrix(confusion);
                Commons.printlnBlack("");
                Commons.printlnBlue(String.format("Known+True: %d; Known+False: %d; Precision: %f",
                        KT, KF, (1f * KT / (KT + KF))));
                Commons.printlnRed(String.format("Unknown+True: %d; Unknown+False: %d; Precision: %f",
                        UKT, UKF, (1f * UKT / (UKT + UKF))));
            }
        }));

        validationFilePath = validationFilePath == null || validationFilePath.isEmpty() ?
                Parser.validationFilePath : validationFilePath;
        readTTBTokens(validationFilePath, languageModels.stream()
                .filter(LModel::hasTrained)
                .flatMap(lModel -> lModel.getListeners().stream())
                .collect(Collectors.toList()));
        languageModels.forEach(LModel::clearListeners);
    }

    private static List<Tuple<String, String>> readOutputFormat(String path) {
        List<Tuple<String, String>> results = new LinkedList<>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(new File(path)));

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) {
                    results.add(null);
                    continue;
                }

                String[] fragments = line.split("\\|");

                Tuple<String, String> token = new Tuple<>(fragments[0], fragments[1]);
                results.add(token);
            }

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return results;
    }

    private static void readTTBTokens(String path, List<? extends ParseListener> listeners) {
        if (listeners.size() == 0) return;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(new File(path)));

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) {
                    listeners.forEach(ParseListener::onNewSentence);
                    continue;
                }

                String[] fragments = line.split("\\s");

                Token token = new Token();
                try {
                    token.setId(Integer.valueOf(fragments[0]));
                    token.setForm(fragments[1]);
                    token.setLemma(fragments[2]);
                    token.setcPosTag(fragments[3]);
                    token.setPosTag(fragments[4]);
                    token.setFeats(fragments[5]);
                    token.setHead(fragments[6]);
                    token.setDeprel(fragments[7]);
                    token.setpHead(fragments[8]);
                    token.setpDeprel(fragments[9]);
                } catch (ArrayIndexOutOfBoundsException e) {
                    if (token.getForm() == null && token.getLemma() == null)
                        throw new RuntimeException("Invalid file format: " + line);
                } finally {
                    listeners.forEach(parseListener -> parseListener.onTokenDetected(token));
                }
            }

            listeners.forEach(ParseListener::onParseEnded);
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean[] parseLanguageFlags(String input) {
        String[] langFlags = input.split("\\s");

        boolean[] flags = new boolean[] { true, true };
        for (String flag: langFlags) {
            if (flag.equals("-cpostag"))
                flags[0] = false;
            else if (flag.equals("-lemma"))
                flags[1] = false;
        }
        return flags;
    }
}