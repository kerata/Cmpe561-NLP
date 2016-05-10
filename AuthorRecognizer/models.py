import locale
import utils
from math import log, fabs
from re import sub, split
from functools import cmp_to_key


class Author:
    alpha = 0.011

    def __init__(self, author_name, article_file_names, encoding, global_vocabulary):
        self.author_name = author_name

        self.articles = []
        self.article_count = 0
        self.vocabulary = {}

        self.possibility = 0.0

        self.sentence_count = 0
        self.avg_word_count_in_sentence = 0

        self.word_count = 0
        self.avg_word_count_in_article = 0
        self.avg_word_length = 0

        self.quasi_count = 0
        self.avg_quasi_in_article = 0
        self.quote_count = 0
        self.avg_quote_in_article = 0
        self.abbreviation_count = 0
        self.avg_abbreviation_in_article = 0

        self.exclamation_count = 0
        self.avg_exclamation_in_article = 0
        self.question_mark_count = 0
        self.avg_question_mark_in_article = 0
        self.semicolon_count = 0
        self.avg_semicolon_in_article = 0
        self.comma_count = 0
        self.avg_comma_in_article = 0
        self.colon_count = 0
        self.avg_colon_in_article = 0

        for file_name in article_file_names:
            with open(file_name, encoding=encoding) as file:
                self.accumulate_stats(Article(" ".join(file.readlines()).strip(), [global_vocabulary, self.vocabulary]))
        self.calculate_stats()

    def accumulate_stats(self, article):
        self.article_count += 1
        self.articles.append(article)

        self.sentence_count += article.sentence_count
        self.word_count += article.word_count

        self.quote_count += article.quote_count
        self.quasi_count += article.quasi_count
        self.abbreviation_count += article.abbreviation_count

        self.exclamation_count += article.exclamation_count
        self.question_mark_count += article.question_mark_count
        self.semicolon_count += article.semicolon_count
        self.comma_count += article.comma_count
        self.colon_count += article.colon_count

    def calculate_stats(self):
        self.avg_word_count_in_article = self.word_count / self.article_count
        self.avg_word_count_in_sentence = self.word_count / self.sentence_count
        for article in self.articles:
            self.avg_word_length += (article.avg_word_length * article.word_count)
            self.avg_quote_in_article += article.quote_count
            self.avg_quasi_in_article += article.quasi_count
            self.avg_question_mark_in_article += article.question_mark_count
            self.avg_abbreviation_in_article += article.abbreviation_count
            self.avg_exclamation_in_article += article.exclamation_count
            self.avg_semicolon_in_article += article.semicolon_count
            self.avg_comma_in_article += article.comma_count
            self.avg_colon_in_article += article.colon_count

        self.avg_word_length /= self.word_count

        self.avg_quote_in_article /= self.article_count
        self.avg_quasi_in_article /= self.article_count
        self.avg_abbreviation_in_article /= self.article_count
        self.avg_exclamation_in_article /= self.article_count
        self.avg_question_mark_in_article /= self.article_count
        self.avg_semicolon_in_article /= self.article_count
        self.avg_comma_in_article /= self.article_count
        self.avg_colon_in_article /= self.article_count

    @staticmethod
    def calculate_naive_bayes_probability(author, **kwargs):
        article = kwargs["article"]
        vocabulary = kwargs["vocabulary"]
        p_naive_bayes = author.possibility
        for token, count in article.vocabulary.items():
            p_naive_bayes += log((author.vocabulary.get(token, 0) + Author.alpha) /
                                 (author.word_count + Author.alpha * len(vocabulary))) * count
        return p_naive_bayes

    @staticmethod
    def get_diff_word_length(author, **kwargs):
        article = kwargs["article"]
        return fabs(author.avg_word_length - article.avg_word_length)

    @staticmethod
    def get_diff_word_count_in_sentence(author, **kwargs):
        article = kwargs["article"]
        return fabs(author.avg_word_count_in_sentence - article.avg_word_count_in_sentence)

    @staticmethod
    def get_diff_word_count_in_article(author, **kwargs):
        article = kwargs["article"]
        return fabs(author.avg_word_count_in_article - article.word_count)

    @staticmethod
    def get_diff_exclamation_count(author, **kwargs):
        article = kwargs["article"]
        return fabs(author.avg_exclamation_in_article - article.exclamation_count)

    @staticmethod
    def get_diff_question_mark_count(author, **kwargs):
        article = kwargs["article"]
        return fabs(author.avg_question_mark_in_article - article.question_mark_count)

    @staticmethod
    def get_diff_quasi_count(author, **kwargs):
        article = kwargs["article"]
        return fabs(author.avg_quasi_in_article - article.quasi_count)

    @staticmethod
    def get_diff_quote_count(author, **kwargs):
        article = kwargs["article"]
        return fabs(author.avg_quote_in_article - article.quote_count)

    @staticmethod
    def get_diff_abbreviation_count(author, **kwargs):
        article = kwargs["article"]
        return fabs(author.avg_abbreviation_in_article - article.abbreviation_count)

    @staticmethod
    def get_diff_colon_count(author, **kwargs):
        article = kwargs["article"]
        return fabs(author.avg_colon_in_article - article.colon_count)

    @staticmethod
    def get_diff_comma_count(author, **kwargs):
        article = kwargs["article"]
        return fabs(author.avg_comma_in_article - article.comma_count)

    @staticmethod
    def get_diff_semicolon_count(author, **kwargs):
        article = kwargs["article"]
        return fabs(author.avg_semicolon_in_article - article.semicolon_count)

    def print_stats(self):
        print()
        utils.print_header(self.author_name)
        print()
        utils.print_blue("Article count:                        " + str(self.article_count))
        utils.print_blue("Sentence count:                       " + str(self.sentence_count))
        utils.print_blue("Word count:                           " + str(self.word_count))
        utils.print_blue("Quote count:                          " + str(self.quote_count))
        utils.print_blue("Abbreviation count:                   " + str(self.abbreviation_count))
        utils.print_blue("Exclamation count:                    " + str(self.exclamation_count))
        utils.print_blue("Question count:                       " + str(self.question_mark_count))
        utils.print_blue("Semicolon count:                      " + str(self.semicolon_count))
        utils.print_blue("Comma count:                          " + str(self.comma_count))
        utils.print_blue("Colon count:                          " + str(self.colon_count))
        print()
        utils.print_blue("Avg word count in sentence:           " + str(self.avg_word_count_in_sentence))
        utils.print_blue("Avg word length:                      " + str(self.avg_word_length))
        utils.print_blue("Avg quote in article:                 " + str(self.avg_quote_in_article))
        utils.print_blue("Avg abbreviation in article:          " + str(self.avg_abbreviation_in_article))
        utils.print_blue("Avg exclamation in article:           " + str(self.avg_exclamation_in_article))
        utils.print_blue("Avg question mark length in article:  " + str(self.avg_question_mark_in_article))
        utils.print_blue("Avg semicolon in article:             " + str(self.avg_semicolon_in_article))
        utils.print_blue("Avg comma in article:                 " + str(self.avg_comma_in_article))
        utils.print_blue("Avg colon in article:                 " + str(self.avg_colon_in_article))


class Article:
    def __init__(self, text, vocabularies):
        self.vocabulary = {}

        self.sentence_count = 0
        self.avg_word_count_in_sentence = 0

        self.word_count = 0
        self.avg_word_length = 0

        self.quasi_count = 0
        self.quote_count = 0
        self.abbreviation_count = 0

        self.exclamation_count = 0
        self.question_mark_count = 0
        self.semicolon_count = 0
        self.comma_count = 0
        self.colon_count = 0

        self.tokenize(text, vocabularies)

    def handle_abbreviations(self, match):
        self.abbreviation_count += 1
        return sub(r'\.', r'', match.group(0))

    def handle_quotes(self, match):
        self.quote_count += 1
        return sub(r'[\.\?!]', r' ', match.group(1))

    def handle_quasi(self, match):
        self.quasi_count += 1
        return sub(r'[\.\?!]', r'', match.group(1))

    def handle_exclamations(self, match):
        self.sentence_count += 1
        self.exclamation_count += 1
        return ''

    def handle_question_marks(self, match):
        self.sentence_count += 1
        self.question_mark_count += 1
        return ''

    def handle_commas(self, match):
        self.comma_count += 1
        return ''

    def handle_colons(self, match):
        self.colon_count += 1
        return ''

    def handle_semicolons(self, match):
        self.semicolon_count += 1
        return ''

    def tokenize(self, article, vocabularies):
        """

        :param article:
        :param vocabularies:
        :rtype: array of objects
        """
        locale.setlocale(locale.LC_ALL, 'tr_TR.UTF-8')
        article = article.lower()
        article = sub(r'\b(?:(?:[0-9]+,+)*(?:[0-9])+)*(?:\.[0-9]+)*', r'', article)

        article = sub(r'\"(.*?)\".*?\W', self.handle_quotes, article)
        article = sub(r'\'(.*?)\'.*?\W', self.handle_quasi, article)
        article = sub(r'\'.*?\W', r' ', article)
        article = sub(r'\.{2,}', r'.', article)
        article = sub(r'(?:\w*?\.){2,}', self.handle_abbreviations, article)

        article = sub(r'!', self.handle_exclamations, article)
        article = sub(r'\?', self.handle_question_marks, article)
        article = sub(r',', self.handle_commas, article)
        article = sub(r':', self.handle_colons, article)
        article = sub(r';', self.handle_semicolons, article)

        total_word_length = 0
        for sentence in article.split("."):
            self.sentence_count += 1

            sentence = sub(r'\W+', r' ', sentence)
            tokens = split(r'\s', sentence)
            tokens.sort(key=cmp_to_key(locale.strcoll))

            for token in tokens:
                if len(token) > 1 and not utils.stop_word(token):
                    self.word_count += 1
                    total_word_length += len(token)
                    self.vocabulary[token] = self.vocabulary.get(token, 0) + 1
                    for vocabulary in vocabularies:
                        if vocabulary is not None:
                            vocabulary[token] = vocabulary.get(token, 0) + 1

        self.avg_word_count_in_sentence = self.word_count / self.sentence_count
        self.avg_word_length = total_word_length / self.word_count

    def print_stats(self):
        utils.print_blue("Sentence count:               " + str(self.sentence_count))
        utils.print_blue("Avg word count in sentence:   " + str(self.avg_word_count_in_sentence))
        utils.print_blue("Word count:                   " + str(self.word_count))
        utils.print_blue("Avg word length:              " + str(self.avg_word_length))
        utils.print_blue("Quote count:                  " + str(self.quote_count))
        utils.print_blue("Abbreviation count:           " + str(self.abbreviation_count))
        utils.print_blue("Exclamation count:            " + str(self.exclamation_count))
        utils.print_blue("Question count:               " + str(self.question_mark_count))
        utils.print_blue("Semicolon count:              " + str(self.semicolon_count))
        utils.print_blue("Comma count:                  " + str(self.comma_count))
        utils.print_blue("Colon count:                  " + str(self.colon_count))
