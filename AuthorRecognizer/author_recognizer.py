#!/usr/local/Cellar/python3/3.5.1/bin/python3.5

import os
import argparse
import utils
from random import shuffle
from math import log
from models import Author

VERBOSE = True
DEBUG = False
LESS = True
vocabulary = {}
training_set = {}
test_set = {}


def train(article_count):
    global vocabulary
    for author in training_set.values():
        author.possibility = log(author.article_count / article_count)

    if DEBUG:
        print("Vocabulary created: " + str(vocabulary))
    utils.print_bold("Training finished")


def map_all_articles_to_authors(path, encoding, ratio):
    if VERBOSE:
        utils.print_header("Traversing files")

    folders = os.listdir(path)
    article_count = 0
    for author_name in folders:
        if VERBOSE:
            utils.print_header("Traversing texts of: " + author_name)
        if os.path.isdir(os.path.join(path, author_name)):
            article_file_names = []
            for filename in os.listdir(os.path.join(path, author_name)):
                article_file_names.insert(len(article_file_names), os.path.join(path, author_name, filename))

            global vocabulary

            shuffle(article_file_names)
            slicing_point = int(len(article_file_names) * ratio)
            article_count += slicing_point
            training_set[author_name] = Author(author_name, article_file_names[:slicing_point], encoding, vocabulary)
            test_set[author_name] = Author(author_name, article_file_names[slicing_point:], encoding, None)

        if VERBOSE:
            utils.print_bold(author_name + " learned!")

    train(article_count)
    find_authors_for_articles()


def probabilities_for_function(func, test_author, reverse=True):
    return utils.normalize_probabilities(
        [[[trained_author.author_name, func(trained_author, vocabulary=vocabulary, article=article)]
          for trained_author in training_set.values()] for article in test_author.articles], reverse=reverse)


def find_authors_for_articles():
    if VERBOSE or DEBUG:
        utils.print_header("Testing started")
    t0 = 0
    t1 = 0
    t2 = 0
    t3 = 0
    t4 = 0
    t5 = 0
    t6 = 0
    t7 = 0
    t8 = 0
    t9 = 0
    t10 = 0

    total_correct = 0
    total_fail = 0
    macro_avg = 0
    for author_name, test_author in test_set.items():
        if VERBOSE:
            utils.print_header("Testing for: " + author_name)

        naive_probabilities = probabilities_for_function(
            Author.calculate_naive_bayes_probability, test_author)
        total_probabilities = naive_probabilities

        wc_in_sentence_probabilities = probabilities_for_function(
            Author.get_diff_word_count_in_sentence, test_author, reverse=False)
        wc_in_article_probabilities = probabilities_for_function(
            Author.get_diff_word_count_in_article, test_author, reverse=False)
        comma_probabilities = probabilities_for_function(
            Author.get_diff_comma_count, test_author, reverse=False)

        word_length_probabilities = probabilities_for_function(
            Author.get_diff_word_length, test_author, reverse=False)

        abbreviation_probabilities = probabilities_for_function(
            Author.get_diff_abbreviation_count, test_author, reverse=False)
        quasi_probabilities = probabilities_for_function(
            Author.get_diff_quasi_count, test_author, reverse=False)
        quote_probabilities = probabilities_for_function(
            Author.get_diff_quote_count, test_author, reverse=False)

        exclamation_probabilities = probabilities_for_function(
            Author.get_diff_exclamation_count, test_author, reverse=False)
        question_probabilities = probabilities_for_function(
            Author.get_diff_question_mark_count, test_author, reverse=False)
        colon_probabilities = probabilities_for_function(
            Author.get_diff_colon_count, test_author, reverse=False)
        semicolon_probabilities = probabilities_for_function(
            Author.get_diff_semicolon_count, test_author, reverse=False)

        for i in range(len(naive_probabilities)):
            for j in range(len(naive_probabilities[i])):
                total_probabilities[i][j][1] = naive_probabilities[i][j][1] * 40
                val = [x[1] for k, x in enumerate(wc_in_sentence_probabilities[i])
                       if x[0] == naive_probabilities[i][j][0]][0]
                total_probabilities[i][j][1] += val * 1
                val = [x[1] for k, x in enumerate(wc_in_article_probabilities[i])
                       if x[0] == naive_probabilities[i][j][0]][0]
                total_probabilities[i][j][1] += val * 1
                val = [x[1] for k, x in enumerate(comma_probabilities[i])
                       if x[0] == naive_probabilities[i][j][0]][0]
                total_probabilities[i][j][1] += val * 1
                val = [x[1] for k, x in enumerate(word_length_probabilities[i])
                       if x[0] == naive_probabilities[i][j][0]][0]
                total_probabilities[i][j][1] += val * 1
                # val = [x[1] for k, x in enumerate(abbreviation_probabilities[i])
                #        if x[0] == naive_probabilities[i][j][0]][0]
                # total_probabilities[i][j][1] += val * 1
                # val = [x[1] for k, x in enumerate(quasi_probabilities[i])
                #        if x[0] == naive_probabilities[i][j][0]][0]
                # total_probabilities[i][j][1] += val * 1
                # val = [x[1] for k, x in enumerate(quote_probabilities[i])
                #        if x[0] == naive_probabilities[i][j][0]][0]
                # total_probabilities[i][j][1] += val * 1
                # val = [x[1] for k, x in enumerate(exclamation_probabilities[i])
                #        if x[0] == naive_probabilities[i][j][0]][0]
                # total_probabilities[i][j][1] += val * 1
                # val = [x[1] for k, x in enumerate(question_probabilities[i])
                #        if x[0] == naive_probabilities[i][j][0]][0]
                # total_probabilities[i][j][1] += val * 1
                val = [x[1] for k, x in enumerate(colon_probabilities[i])
                       if x[0] == naive_probabilities[i][j][0]][0]
                total_probabilities[i][j][1] += val * 1
                val = [x[1] for k, x in enumerate(semicolon_probabilities[i])
                       if x[0] == naive_probabilities[i][j][0]][0]
                total_probabilities[i][j][1] += val * 1
        total_probabilities = [sorted(author, key=lambda a: a[1], reverse=True) for author in total_probabilities]

        correct = 0
        fail = 0
        for i in range(len(total_probabilities)):
            guessed_author_names = [author[0] for author in total_probabilities[i]]
            if author_name == guessed_author_names[0]:
                correct += 1
                if DEBUG or VERBOSE and not LESS:
                    utils.print_green(author_name + " : " + str(guessed_author_names[0]))
            else:
                fail += 1
                if DEBUG or VERBOSE and not LESS:
                    utils.print_fail(author_name + " : " + guessed_author_names[0] +
                                     " rank : " + str(guessed_author_names.index(author_name)))
                if DEBUG:
                    utils.print_blue("wc_sentence   : " + str([k for k, x in enumerate(wc_in_sentence_probabilities[i])
                                                               if x[0] == author_name][0]))
                    utils.print_blue("wc_in_article : " + str([k for k, x in enumerate(wc_in_article_probabilities[i])
                                                               if x[0] == author_name][0]))
                    utils.print_blue("word_length   : " + str([k for k, x in enumerate(word_length_probabilities[i])
                                                               if x[0] == author_name][0]))
                    utils.print_blue("abbreviation  : " + str([k for k, x in enumerate(abbreviation_probabilities[i])
                                                               if x[0] == author_name][0]))
                    utils.print_blue("quasi         : " + str([k for k, x in enumerate(quasi_probabilities[i])
                                                               if x[0] == author_name][0]))
                    utils.print_blue("quote         : " + str([k for k, x in enumerate(quote_probabilities[i])
                                                               if x[0] == author_name][0]))
                    utils.print_blue("exclamation   : " + str([k for k, x in enumerate(exclamation_probabilities[i])
                                                               if x[0] == author_name][0]))
                    utils.print_blue("question      : " + str([k for k, x in enumerate(question_probabilities[i])
                                                               if x[0] == author_name][0]))
                    utils.print_blue("comma         : " + str([k for k, x in enumerate(comma_probabilities[i])
                                                               if x[0] == author_name][0]))
                    utils.print_blue("colon         : " + str([k for k, x in enumerate(colon_probabilities[i])
                                                               if x[0] == author_name][0]))
                    utils.print_blue("semicolon     : " + str([k for k, x in enumerate(semicolon_probabilities[i])
                                                               if x[0] == author_name][0]))
                    t0 += [k for k, x in enumerate(wc_in_sentence_probabilities[i]) if x[0] == author_name][0]
                    t1 += [k for k, x in enumerate(wc_in_article_probabilities[i]) if x[0] == author_name][0]
                    t2 += [k for k, x in enumerate(word_length_probabilities[i]) if x[0] == author_name][0]
                    t3 += [k for k, x in enumerate(abbreviation_probabilities[i]) if x[0] == author_name][0]
                    t4 += [k for k, x in enumerate(quasi_probabilities[i]) if x[0] == author_name][0]
                    t5 += [k for k, x in enumerate(quote_probabilities[i]) if x[0] == author_name][0]
                    t6 += [k for k, x in enumerate(exclamation_probabilities[i]) if x[0] == author_name][0]
                    t7 += [k for k, x in enumerate(question_probabilities[i]) if x[0] == author_name][0]
                    t8 += [k for k, x in enumerate(comma_probabilities[i]) if x[0] == author_name][0]
                    t9 += [k for k, x in enumerate(colon_probabilities[i]) if x[0] == author_name][0]
                    t10 += [k for k, x in enumerate(semicolon_probabilities[i]) if x[0] == author_name][0]

        macro_avg += correct / (correct + fail)
        if DEBUG or VERBOSE and not LESS:
            utils.print_blue(author_name +
                             " correct : " + str(correct) +
                             " fail : " + str(fail) +
                             " res : " + str(macro_avg))
        total_correct += correct
        total_fail += fail

    if DEBUG:
        utils.print_header("Extra feature average ranks: ")
        utils.print_bold("word count in sentence:" + str(t0 / total_fail))
        utils.print_bold("word count in article: " + str(t1 / total_fail))
        utils.print_bold("word length:           " + str(t2 / total_fail))
        utils.print_bold("abbreviation count:    " + str(t3 / total_fail))
        utils.print_bold("quasi count:           " + str(t4 / total_fail))
        utils.print_bold("quote count:           " + str(t5 / total_fail))
        utils.print_bold("exclamation count:     " + str(t6 / total_fail))
        utils.print_bold("question mark count:   " + str(t7 / total_fail))
        utils.print_bold("comma count:           " + str(t8 / total_fail))
        utils.print_bold("colon count:           " + str(t9 / total_fail))
        utils.print_bold("semicolon count:       " + str(t10 / total_fail))

    utils.print_header("Correct : " + str(total_correct) +
                       " Fail : " + str(total_fail))
    utils.print_header("Micro Averaged : " + str(total_correct / (total_correct + total_fail)))
    utils.print_header("Macro Averaged : " + str(macro_avg / len(test_set)))


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument('-v', '--verbose', dest='verbose', action='store_true')
    parser.add_argument('-nv', '--no-verbose', dest='verbose', action='store_false')
    parser.add_argument('-d', '--debug', dest='debug', action='store_true')
    parser.add_argument('-nd', '--no-debug', dest='debug', action='store_false')
    parser.add_argument('-nl', '--no-less', dest='less', action='store_false')
    parser.add_argument('-l', '--less', dest='less', action='store_true')
    parser.add_argument('-p', '--path', default="./raw_texts", type=str, help='Path to container folder')
    parser.add_argument('-e', '--encoding', default="windows-1254", type=str, help='File encoding')
    parser.add_argument('-a', '--alpha', default="0.011", type=float, help='Alpha value for naive bayes normalizer')
    parser.add_argument('-r', '--ratio', default=0.6, type=float, help='Rate to split for test and training sets')
    opts = parser.parse_args()
    Author.alpha = opts.alpha
    VERBOSE = opts.verbose
    LESS = opts.less
    DEBUG = opts.debug
    if VERBOSE:
        print("VERBOSE: true DEBUG: " + str(DEBUG) + " LESS: " + str(LESS) + " folder_path: " + opts.path +
              " encoding: " + opts.encoding + " alpha for normalization: " + str(opts.alpha) +
              " Training/Data: " + str(opts.ratio))
    map_all_articles_to_authors(path=opts.path, encoding=opts.encoding, ratio=opts.ratio)
