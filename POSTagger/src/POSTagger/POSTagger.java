package POSTagger;

import POSTagger.Utils.Commons;
import POSTagger.Utils.LModel;
import POSTagger.Utils.Parser;

import java.util.*;

public class POSTagger {

    static Scanner in;

    public static void main(String[] args) throws InterruptedException {
        List<LModel> models = new ArrayList<>();

        // Run with arguments
        if (args != null && args.length > 0) {
            boolean shouldTag = false, shouldEvaluate = false, shouldValidate = false;
            boolean isPosTag = true, isUsingForm = true;

            String trainingFilePath = Parser.defaultTrainingFilePath;

            String outputFilePath = Parser.defaultOutputFilePath;

            String testFilePath = Parser.testBlindFilePath;
            String testGoldStandardFilePath = Parser.testGoldFilePath;

            String validationFilePath = Parser.validationFilePath;
            for (int i = 0;i < args.length;i++) {
                String arg = args[i];
                switch (arg) {
                    case "-t":
                        shouldTag = true;
                        if ((i + 1) < args.length && !args[i +1].startsWith("-"))
                            testFilePath = args[++i];
                        break;
                    case "-e":
                        shouldEvaluate = true;
                        if ((i + 1) < args.length && !args[i +1].startsWith("-"))
                            testGoldStandardFilePath = args[++i];
                        break;
                    case "-v":
                        shouldValidate = true;
                        if ((i + 1) < args.length && !args[i +1].startsWith("-"))
                            validationFilePath = args[++i];
                        break;
                    case "-pt":
                        trainingFilePath = args[++i];
                        break;
                    case "-o":
                        outputFilePath = args[++i];
                        break;
                    case "-cpostag":
                        isPosTag = false;
                        break;
                    case "-lemma":
                        isUsingForm = false;
                        break;
                    case "-V":
                        Commons.VERBOSE = true;
                        break;
                    default:
                        Commons.fPrintlnBlack("|#%+&!?");
                        Commons.fPrintlnBlack("Enter \"-h\" to see help.\n");
                        break;
                }
            }
            LModel lModel = new LModel(isPosTag, isUsingForm);
            lModel.setTrainingFilePath(trainingFilePath);
            lModel.setOutputFilePath(outputFilePath);
            lModel.setGoldFilePath(testGoldStandardFilePath);
            models = Collections.singletonList(lModel);

            Parser.train(models);
            if (shouldTag)
                Parser.tag(testFilePath, models);
            if (shouldEvaluate)
                Parser.evaluate(models);
            if (shouldValidate)
                Parser.validate(validationFilePath, models);

            Commons.fPrintBlack("");
        }
        // Run with user interface
        else {
            in = new Scanner(System.in);
            String input;

            Commons.fPrintlnBlack("Enter \"-h\" to see help.\n");
            userInterface: do {
                Commons.fPrintBlack("> ");
                input = in.nextLine();
                qProcessor: switch (input) {
                    case "-q":
                        break userInterface;
                    case "-h":
                        printHelp();
                        break;
                    case "-tr":
                        createLanguageModels(models);
                        Parser.train(models);
                        break;
                    case "-t":
                        while (models.isEmpty()) createLanguageModels(models);
                        Commons.fPrintBlack("Write path or press enter to use default file to be tagged: ");

                        Parser.tag(in.nextLine(), models);
                        break;
                    case "-e":
                        String outputFile = null;
                        String goldFile = null;

                        if (!models.isEmpty()) {
                            while (true) {
                                Commons.fPrintlnBlue("Choose model output to evaluate: ");
                                Commons.fPrintlnYellow("0: Choose own output");
                                Iterator<LModel> it = models.iterator();
                                for (int i = 1;it.hasNext();i++)
                                    Commons.fPrintlnBlack(String.format("%d: %s", i, it.next().toString()));
                                input = in.nextLine();
                                if (input.equals("0"))
                                    break;
                                else {
                                    Parser.evaluate(Collections.singletonList(models.get(Integer.parseInt(input) -1)));
                                    break qProcessor;
                                }
                            }
                        }
                        Commons.fPrintBlack("Write path of output file: ");
                        outputFile = in.nextLine();
                        Commons.fPrintBlack("Write path gold standard file: ");
                        goldFile = in.nextLine();

                        boolean[] flags = Parser.parseLanguageFlags(input);
                        LModel lModel = new LModel(flags[0], flags[1]);
                        lModel.setOutputFilePath(outputFile);
                        lModel.setGoldFilePath(goldFile);

                        Parser.evaluate(Collections.singletonList(lModel));
                        break;
                    case "-v":
                        while (models.isEmpty()) createLanguageModels(models);
                        Commons.fPrintBlack("Write path or press enter to use default validation file: ");

                        Parser.validate(in.nextLine(), models);
                        break;
                    case "-V":
                        Commons.VERBOSE = !Commons.VERBOSE;
                        break;
                    case "-pt":
                        Commons.fPrintBlack("Should use common training file(Y/n): ");
                        input = in.nextLine();
                        if (input.isEmpty() || input.equalsIgnoreCase("Y")) {
                            Commons.fPrintBlack("Write path of common training file or press enter to use default: ");
                            input = in.nextLine();
                            if (input != null && !input.isEmpty())
                                Parser.defaultTrainingFilePath = input;
                            else Parser.defaultTrainingFilePath = "src/main/resources/train/turkish_metu_sabanci_train.conll";
                        }
                        else Parser.defaultTrainingFilePath = null;
                        break;
                    default:
                        Commons.fPrintlnBlack("|#%+&!?");
                        Commons.fPrintlnBlack("Enter \"-h\" to see help.\n");
                        break;
                }
            } while (true);
            in.close();
            Commons.fPrintBlack("");
        }
    }

    /**
     * Prompt help.
     */
    private static void printHelp() {
        Commons.fPrintlnBlack("Enter \"-tr\" to train.");
        Commons.fPrintlnBlack("Enter \"-t\"  to tag.");
        Commons.fPrintlnBlack("Enter \"-e\"  to evaluate.");
        Commons.fPrintlnBlack("Enter \"-v\"  to validate.");
        Commons.fPrintlnBlack("Enter \"-V\"  to toggle verbose.");
        Commons.fPrintlnBlack("Enter \"-pt\" set common training file.");
        Commons.fPrintlnBlack("Enter \"-q\"  to exit.");
    }

    private static void createLanguageModels(List<LModel> models) {
        Commons.fPrintlnBlack("There must be models to work on!!!\n");

        Commons.fPrintlnBlack("Default model uses PosTag and Form fields");
        Commons.fPrintlnBlack("-cpostag : model uses cPosTag field instead of PosTag");
        Commons.fPrintlnBlack("-lemma : model uses Lemma field instead of form");
        do {
            Commons.fPrintBlack("Flags|exit: ");

            String input = in.nextLine();
            if (input.equals("exit")) break;

            boolean[] flags = Parser.parseLanguageFlags(input);
            LModel lModel = new LModel(flags[0], flags[1]);

            if (Parser.defaultTrainingFilePath == null) {
                Commons.fPrintBlack("Training File Path: ");
                lModel.setTrainingFilePath(in.nextLine());
            }
            else lModel.setTrainingFilePath(Parser.defaultTrainingFilePath);

            Commons.fPrintBlack("Output File Path: ");
            lModel.setOutputFilePath(in.nextLine());
            models.add(lModel);
        } while (true);
    }
}
