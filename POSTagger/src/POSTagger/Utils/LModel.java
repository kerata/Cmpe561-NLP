package POSTagger.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class LModel {

    public static final String[] sentenceDelimiter = { "<BoS>", "<EoS>" };

    private String trainingFilePath;
    private String outputFilePath;
    private String goldFilePath;

    private boolean isPosTag;
    private boolean isUsingForm = true;
    private Stats stats;

    private ArrayList<Parser.ParseListener> listeners = new ArrayList<>();

    public LModel(boolean isPosTag, boolean isUsingForm) {
        this.isPosTag = isPosTag;
        this.isUsingForm = isUsingForm;
        stats = new Stats(this);
    }

    public boolean isPosTag() {
        return isPosTag;
    }

    public void setIsPosTag(boolean isPosTag) {
        this.isPosTag = isPosTag;
    }

    public boolean isUsingForm() {
        return isUsingForm;
    }

    public void setUsingForm(boolean usingForm) {
        isUsingForm = usingForm;
    }

    public boolean hasTrained() {
        return !getStats().wordTagCounts.isEmpty();
    }

    public String getTrainingFilePath() {
        return trainingFilePath;
    }

    public void setTrainingFilePath(String trainingFilePath) {
        if (trainingFilePath != null && !trainingFilePath.isEmpty())
            this.trainingFilePath = trainingFilePath;
    }

    public String getOutputFilePath() {
        return outputFilePath;
    }

    public void setOutputFilePath(String outputFilePath) {
        if (outputFilePath != null && !outputFilePath.isEmpty())
            this.outputFilePath = outputFilePath;
    }

    public String getGoldFilePath() {
        return goldFilePath;
    }

    public void setGoldFilePath(String goldFilePath) {
        this.goldFilePath = goldFilePath;
    }

    public Stats getStats() {
        return stats;
    }

    public ArrayList<Parser.ParseListener> getListeners() {
        return listeners;
    }

    public void addListener(Parser.ParseListener listener) {
        this.listeners.add(listener);
    }

    public void removeListener(Parser.ParseListener listener) {
        this.listeners.remove(listener);
    }

    public void clearListeners() {
        this.listeners.clear();
    }

    public float calculateWLP(String word, String tag) {
        int count = stats.wordTagCounts.get(tag).getOrDefault(word, -1);
        return (count == -1 ? 0.0000001f : count * 1f) / stats.getTagOccurrence(tag);
    }

    public float calculateTagTransition(String fromTag, String toTag) {
        int count = stats.tagTransitionCounts.get(fromTag).getOrDefault(toTag, -1);
        return (count == -1 ? 0.0000001f : count * 1f) / stats.getTagOccurrence(fromTag);
    }

    public float calculateTagProbability(String fromTag, String toTag, Token token) {
        return calculateWLP(Commons.getKeyword(token, this), toTag) * calculateTagTransition(fromTag, toTag);
    }

    public void findNextTagViterbi(Token prevToken, Token nextToken) {
        try (Stream<String> runner = new ArrayList<>(stats.tagTransitionCounts.keySet()).stream()) {
            final String[] foundTag = {null};
            final float[] maxProb = {0f};

            runner.onClose(() -> {
                        if (isPosTag)
                            nextToken.setPosTag(foundTag[0]);
                        else nextToken.setcPosTag(foundTag[0]);
                    })
                    .forEach(tag -> {
                        if (!sentenceDelimiter[0].equals(tag)) {
                            float prob = calculateTagProbability(
                                    prevToken == null ? sentenceDelimiter[0] :
                                            Commons.getPosTag(prevToken, this),
                                    tag, nextToken);

                            if (prob > maxProb[0]) {
                                maxProb[0] = prob;
                                foundTag[0] = tag;
                            }
                        }
                    });
        }
    }

    public void onTokenDetected(Token token) {
        listeners.forEach(parseListener -> parseListener.onTokenDetected(token));
    }

    public void onNewSentence() {
        listeners.forEach(Parser.ParseListener::onNewSentence);
    }

    public class Stats {
        private LModel lModel;

        // tag -> (keyword -> count)
        private Map<String, Map<String, Integer>> wordTagCounts;
        private Map<String, Integer> tagOccurrences;
        // tag -> (tag -> count)
        private Map<String, Map<String, Integer>> tagTransitionCounts;

        private ArrayList<Token> inflectionalTokenGroup;
        private Token previousToken;

        Stats(LModel lModel) {
            this.lModel = lModel;
            wordTagCounts = new HashMap<>();
            tagOccurrences = new HashMap<>();
            tagTransitionCounts = new HashMap<>();
            tagTransitionCounts.put(sentenceDelimiter[0], new HashMap<>());
            inflectionalTokenGroup = new ArrayList<>();
        }

        public Map<String, Map<String, Integer>> getWordTagCounts() {
            return wordTagCounts;
        }

        public Map<String, Integer> getTagOccurrences() {
            return tagOccurrences;
        }

        Stats addToken(Token token) {
            String tag = Commons.getPosTag(token, lModel);

            wordTagCounts.compute(tag, (s, stringIntegerMap) -> {
                if (stringIntegerMap == null) {
                    stringIntegerMap = new HashMap<>();
                    stringIntegerMap.put(Commons.getKeyword(token, lModel), 1);
                }
                else stringIntegerMap.compute(Commons.getKeyword(token, lModel),
                        (form, integer) -> integer == null ? 1 : integer + 1);
                return stringIntegerMap;
            });

            String prevTag = previousToken == null ? sentenceDelimiter[0] :
                    Commons.getPosTag(previousToken, lModel);

            tagTransitionCounts.compute(prevTag, (s, stringIntegerMap) -> {
                if (stringIntegerMap == null) {
                    stringIntegerMap = new HashMap<>();
                    tagTransitionCounts.forEach((s1, stringIntegerMap1) -> stringIntegerMap1.put(tag, 0));
                    stringIntegerMap.put(tag, 1);
                }
                else stringIntegerMap.compute(tag, (s1, integer) -> integer == null ? 1 : integer + 1);
                return stringIntegerMap;
            });

            previousToken = token;
            return this;
        }

        Stats addInflectionalToken(Token token) {
            inflectionalTokenGroup.add(token);
            return this;
        }

        Stats processInflectionalGroup() {
            // Do not handle any derived tokens.
            if (inflectionalTokenGroup.size() > 0)
                addToken(inflectionalTokenGroup.get(inflectionalTokenGroup.size() -1));
            inflectionalTokenGroup.clear();
            return this;
        }

        Stats endSentence() {
            String tag = Commons.getPosTag(previousToken, lModel);
            tagTransitionCounts.compute(tag, (s, stringIntegerMap) -> {
                if (stringIntegerMap == null) {
                    stringIntegerMap = new HashMap<>();
                    stringIntegerMap.put(sentenceDelimiter[1], 1);
                }
                else stringIntegerMap.compute(sentenceDelimiter[1], (s1, integer) -> integer == null ? 1 : integer + 1);
                return stringIntegerMap;
            });

            previousToken = null;
            inflectionalTokenGroup.clear();
            return this;
        }

        int getTagOccurrence(String tag) {
            tagOccurrences.computeIfAbsent(tag, s -> {
                final int[] totalCount = {0};
                wordTagCounts.getOrDefault(tag, new HashMap<>()).forEach((s1, integer) -> totalCount[0] += integer);
                return totalCount[0];
            });
            return tagOccurrences.get(tag);
        }

        public void printTagTransitions() {
            StringBuilder table = new StringBuilder(String.format("%-10s", " "));
            ArrayList<String> tags = new ArrayList<>(tagTransitionCounts.keySet().size());

            tagTransitionCounts.keySet().forEach(s -> {
                tags.add(s);
                table.append(String.format("%-10s", s));
            });
            table.append("\n");

            for (String from: tags) {
                table.append(String.format("%-10s", from));

                Map<String, Integer> row = tagTransitionCounts.get(from);
                for (String to: tags)
                    table.append(String.format("%-10s", row.getOrDefault(to, 0)));

                table.append("\n");
            }

            Commons.printlnYellow(table.toString());
        }
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append("isPosTag: ").append(isPosTag)
                .append("\tisUsingForm: ").append(isUsingForm)
                .append("\thasTrained: ").append(hasTrained()).append("\n")
                .append("Training file path: ").append(trainingFilePath).append("\n")
                .append("Output File Path: ").append(outputFilePath).append("\n")
                .toString();
    }
}
