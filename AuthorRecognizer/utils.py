
class TerminalColors:
    HEADER = '\033[95m'
    OKBLUE = '\033[94m'
    OKGREEN = '\033[92m'
    WARNING = '\033[93m'
    FAIL = '\033[91m'
    ENDC = '\033[0m'
    BOLD = '\033[1m'
    UNDERLINE = '\033[4m'


def combine_dicts(a, b):
    return {x: a.get(x, 0) + b.get(x, 0) for x in set(a).union(b)}


def normalize_probabilities(probabilities, reverse=True):
    probabilities = [sorted(author, key=lambda a: a[1], reverse=reverse) for author in probabilities]
    for i in range(len(probabilities)):
        normalizer = probabilities[i][0][1]
        for j in range(len(probabilities[i])):
            if not probabilities[i][j][1] == 0:
                probabilities[i][j][1] = normalizer / probabilities[i][j][1]
    return probabilities


def stop_word(token):
    return {
        '': True,
        'de': True,
        'da': True
    }.get(token, False)


def print_blue(text):
    print(TerminalColors.OKBLUE + text + TerminalColors.ENDC)


def print_green(text):
    print(TerminalColors.OKGREEN + text + TerminalColors.ENDC)


def print_fail(text):
    print(TerminalColors.FAIL + text + TerminalColors.ENDC)


def print_warning(text):
    print(TerminalColors.WARNING + text + TerminalColors.ENDC)


def print_bold(text):
    print(TerminalColors.BOLD + text + TerminalColors.ENDC)


def print_underline(text):
    print(TerminalColors.UNDERLINE + text + TerminalColors.ENDC)


def print_header(text):
    print(TerminalColors.HEADER + text + TerminalColors.ENDC)
