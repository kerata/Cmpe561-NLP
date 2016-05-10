# POS tagger

This project contains sources and executable .jar file and some resources for an HMM bigram POS tagger.

## What's in it?

Java source codes placed under `./src` folder and executable package `./TTL_POS_Tagger` folder.

* Source codes placed under `POSTagger` package which contains `POSTagger.java` main file. This package could directly be used to produce a executable jar file.
* `TTL_POS_Tagger` folder contained `POSTagger.jar` file and default training, sample test files and a bigger validation file. Name and relative paths of these files are set staticly within program so they should not be changed.

## How to use it?

Since lambda expressions have been used, it requires Java 8 to be run. User interface will be opened which supports multiple language models to be loaded and tested at once.

    java -jar POSTagger.jar

### Customization

`-tr` flag only used in user interface mode, which opens up model creation. Every model have flags to use `Form` or `Lemma` fields as keywords, `cPosTag` or `PosTag` to use as tags. Their training, output file locations could be setted, if not default files will be used from resources folder.

`-q` flag only used in user interface mode to terminate the program.

`-h` flag only used in user interface mode to see all available flags.

`-V` flag will open or close VERBOSE of the program, default value to this is false.

`-pt` flag will use next parameter, or request to use, as default training file for language models.

`-o` flag will use next parameter, or request to use, as output file path for tagging.

`-t` flag will use next parameter as file path to be tagged; using trained model, first tags the file and prints out results to output file to default location `results/output.txt` or given location. In user interface mode; it will process all models for given file or default test file and prints output to default location by adding enumaration to the end of the file name or to the predefined location. If no language exists, it will navigate user to create models, train once that has not been trained and then tag file using these models.

`-e` flag will use next parameter as file path as gold standard; using trained model to know which tags and keywords it should be looking for and evaluates output file or tries to evaluate default location. In user interface mode, program will list current models and evaluates the one that has been picked, if there are no models or user wants to evaluate two files without training; file paths and flags will be requested and evaluation is done using these data.

`-v` flag will use next parameter as file path as validation; this flag is the combination of `-t` and `-e` without creating an output file. It will parse given file with the created model, predicts tags and test its own results comparing with one in the validation file(gold standard file). In user interface mode, it will validate all trained models but prins results in a merged format. It will print all keywords with found and actual tags as well as scores and confusion matrixes for each model.

**Creating models:** Every model has its own file to be trained, output file path to report tag results and parameters to use keyword and tag types. Default model creation will print its output to default location `results/output<num>.txt` if not set, uses `From` and `PosTag` fields. While creating models, these values could be set with using `-cpostag` and `-lemma` flags.