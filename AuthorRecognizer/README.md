# Author Recognizer

This project contains sources for simple author recognizer using naive bayes.

## What's in it?

Python source codes and sample input files in put in required folder format.

* Folders of text of authors placed in `./raw_texts` folder named in camel case format
* Every author folder contains raw texts for these authors, they will be split randomly on every run to train and test the system.

## How to use it?

This application written for Python 3.5.1 and could be run through the console.

    python3 author_recognizer.py

### Customization

There are couple of flags to open different levels of debug or setting some critical values.

    python3 author_recognizer.py -v -nd -l

`-v` will open basic feedback when program runs, when `-nl` is open with verbose it will also print every tested article when it is correct or false, latest status for every author.

`-d` will open debug mode also to be able to see vocabulary, extra feature ranking for every false guess to see their contribution and how well extra features worked by giving their average ranking for correct values.

Default logging flags are as given, but could be changed by setting `-nv`, `-d` or `-nl` flags.

    python3 author_recognizer.py -a <float>

`-a` will set alpha value for normalization of naive bayes word probabilities which is essential to adjust for smaller data sets such as given one. Default value for alpha is 0.011.

    python3 author_recognizer.py -e <str> -p <str>

Default folder path for data set is "./raw_texts" but this could be changed by `-p` flag. Also encoding format could be specified by `-e` flag but default is "windows-1254"
