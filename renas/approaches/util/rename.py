import difflib
from copy import deepcopy
from logging import DEBUG, getLogger

from renas.approaches.util.common import getPaddingList, printDict, splitIdentifier
from renas.approaches.util.name import ExpandManager, LemmaManager

_NECESSARY_ITEM = [
    "id",
    "files",
    "line",
    "name",
    "typeOfIdentifier",
]
_lemmatizer = LemmaManager()
_expandManager = None
_logger = getLogger("util.Rename")
_logger.setLevel(DEBUG)


def setAbbrDic(abbrDicPath):
    global _expandManager
    _expandManager = ExpandManager(abbrDicPath)
    _logger.info("set abbrDic record")


class Rename:
    def __init__(self, old, normalize):
        self.__normalize = normalize
        if isinstance(old, str):
            self.__old = self.__getNameDetail(old)
        elif isinstance(old, dict):
            self.__old = old
        if not normalize:
            self.__overWriteDetail(self.__old)
        self.__new = None
        self.__diff = None
        self.__wordColumn = "normalized"
        pass

    def get_operation(self):
        return self.get_diff()

    def get_old_normalize(self):
        return self.__old["normalized"]

    def get_id(self):
        return self.__old["id"]

    def setNewName(self, newName):
        if self.__new is None:
            self.__new = self.__getNameDetail(newName)
            self.newSetDiff()
        else:
            _logger.error(f"new name is already set: {self.__new}")
        return

    def get_diff(self):
        if self.__diff is None:
            _logger.error("you first need to call setNewName() to get diff")
        return self.__diff

    def polish_recommended_dict(self, idDict):
        new_dict = {i: idDict[i] for i in _NECESSARY_ITEM}
        if self.__normalize:
            new_dict["similarity"] = idDict["similarity"]
        return new_dict

    def coRename(self, idDict):
        if not self.__normalize:
            self.__overWriteDetail(idDict)
        if idDict == self.__old:
            _logger.debug("candidate is the same as trigger")
            return None
        _logger.debug(
            f'BEFORE {printDict(idDict, "case", "pattern", "delimiter", "heuristic", "postag", self.__wordColumn)}'
        )
        beforeWordList = deepcopy(idDict[self.__wordColumn])
        beforeCaseList = deepcopy(idDict["pattern"])
        # apply diff
        for diff in self.__diff:
            self.__applyDiff(diff, idDict)
        if (
            idDict[self.__wordColumn] == beforeWordList
            and beforeCaseList == idDict["pattern"]
        ):
            _logger.debug("not a candidate")
            return None
        if self.__normalize:
            idDict["similarity"] = self.word_similarity(beforeWordList)
        return self.polish_recommended_dict(idDict)

    def __overWriteDetail(self, old):
        detail = splitIdentifier(old["name"])
        old["split"] = detail["split"]
        old["case"] = detail["case"]
        old["pattern"] = detail["pattern"]
        old["delimiter"] = detail["delimiter"]
        old["normalized"] = deepcopy(old["split"])
        return

    def __getNameDetail(self, name):
        result = splitIdentifier(name)
        words = result["split"]

        if self.__normalize:
            result["expanded"], result["heuristic"], result["case"] = (
                _expandManager.expand(result, self.__old)
            )
            postags = _lemmatizer.getPosTags(result["expanded"])
            result["postag"] = postags
            result["normalized"] = _lemmatizer.normalize(result["expanded"], postags)
        else:
            postags = _lemmatizer.getPosTags(words)
            result["postag"] = postags
            result["normalized"] = deepcopy(words)
        return result

    def newSetDiff(self):
        if self.__diff is None:
            old_split = self.__old["normalized"]
            self.__diff = []
            if self.__normalize:
                self.__diff += self.extractOrder()
                self.__diff += self.extractFormat()
                old_split = self.__old["ordered"]
            new_split = self.__new[self.__wordColumn]
            diff_list = difflib.SequenceMatcher(
                None, old_split, new_split
            ).get_opcodes()

            for diff in diff_list:
                if diff[0] == "insert":
                    self.__diff.append((diff[0], tuple(new_split[diff[3] : diff[4]])))
                elif diff[0] == "replace":
                    self.__diff.append(
                        (
                            diff[0],
                            tuple(old_split[diff[1] : diff[2]]),
                            tuple(new_split[diff[3] : diff[4]]),
                        )
                    )
                elif diff[0] == "delete":
                    self.__diff.append((diff[0], tuple(old_split[diff[1] : diff[2]])))

    def word_similarity(self, candidate_name):
        old_name = self.__old["normalized"]
        similarity = (
            len(set(old_name) & set(candidate_name))
            * 2
            / (len(old_name) + len(candidate_name))
        )

        return similarity

    def extractFormat(self):
        oldNormalize = self.__old[self.__wordColumn]
        newNormalize = self.__new[self.__wordColumn]
        oldExpanded = self.__old["expanded"]
        newExpanded = self.__new["expanded"]
        oldSplit = self.__old["split"]
        newSplit = self.__new["split"]

        equalSplitWords = set(oldSplit) & set(newSplit)
        equalExpandedWords = set(oldExpanded) & set(newExpanded)
        equalNormalizeWords = set(oldNormalize) & set(newNormalize)
        equalExpandedWordsIndex = [
            oldExpanded.index(word) for word in equalExpandedWords
        ]

        abbrCandidate = equalExpandedWords - equalSplitWords
        abbrCandidate = [i for i in oldExpanded if i in abbrCandidate]
        formatAbbreviation = []
        heuH1 = ["Abbreviation", "H1", []]
        for word in abbrCandidate:
            oldHeu = self.__old["heuristic"][oldExpanded.index(word)]
            newHeu = self.__new["heuristic"][newExpanded.index(word)]
            if oldHeu == newHeu:
                continue
            elif oldHeu == "H1":
                heuH1[2].append((word[0], word))
            elif newHeu == "H1":
                heuH1[2].append((word, word[0]))
            elif oldHeu == "H2" or newHeu == "H2":
                formatAbbreviation.append(
                    [
                        "format",
                        (
                            "Abbreviation",
                            "H2",
                            oldSplit[oldExpanded.index(word)],
                            newSplit[newExpanded.index(word)],
                        ),
                    ]
                )
            else:
                formatAbbreviation.append(
                    [
                        "format",
                        (
                            "Abbreviation",
                            "H3",
                            oldSplit[oldExpanded.index(word)],
                            newSplit[newExpanded.index(word)],
                        ),
                    ]
                )

        formatConjugate = []
        formatPlural = []
        for word in equalNormalizeWords - equalExpandedWords:
            if oldNormalize.index(word) not in equalExpandedWordsIndex:
                oldIndex = oldNormalize.index(word)
                newIndex = newNormalize.index(word)
                if "V" in self.__old["postag"][oldIndex]:
                    formatConjugate.append(
                        [
                            "format",
                            ("Conjugate", oldExpanded[oldIndex], newExpanded[newIndex]),
                        ]
                    )
                else:
                    formatPlural.append(
                        [
                            "format",
                            ("Plural", oldExpanded[oldIndex], newExpanded[newIndex]),
                        ]
                    )

        heuH1 = [["format", heuH1]] if heuH1[2] != [] else []
        _logger.debug(formatAbbreviation + formatConjugate + formatPlural)
        return formatAbbreviation + formatConjugate + formatPlural + heuH1

    # 変更操作order抽出
    def extractOrder(self):
        oldNormalize = self.__old[self.__wordColumn]
        newNormalize = self.__new[self.__wordColumn]
        equalNormalizeWords = set(oldNormalize) & set(newNormalize)
        self.__old["ordered"] = oldNormalize
        if len(equalNormalizeWords) > 1:
            oldWordsOrder = [
                word for word in oldNormalize if word in equalNormalizeWords
            ]
            newWordsOrder = [
                word for word in newNormalize if word in equalNormalizeWords
            ]
            if not self.__checkUnique(oldWordsOrder):
                return []
            if not self.__checkUnique(newWordsOrder):
                return []
            if len(oldWordsOrder) > 1 and oldWordsOrder != newWordsOrder:
                order = ["order", (oldWordsOrder, newWordsOrder)]
                self.__old["ordered"] = [
                    (
                        word
                        if word not in oldWordsOrder
                        else newWordsOrder[oldWordsOrder.index(word)]
                    )
                    for word in oldNormalize
                ]
                _logger.debug(order)
                return [order]
            return []

        return []

    def __checkUnique(self, wordList):
        if len(wordList) != len(set(wordList)):
            return False
        return True

    def __applyDiff(self, diff, oldDict):
        dType, *dWords = diff
        if dType == "format":
            self.__applyFormat(oldDict, diff[1])
        elif dType == "order":
            self.__applyOrder(oldDict, diff[1])
        elif dType == "delete":
            self.__applyDelete(oldDict, dWords[0])
        elif dType == "replace":
            self.__applyReplace(oldDict, dWords[0], dWords[1])
        elif dType == "insert":
            self.__applyInsert(oldDict, dWords[0])
        return

    def __applyFormat(self, oldDict, format):
        operation = format[0]

        if operation == "Abbreviation":
            heu = format[1]
            if heu == "H1":
                ops = format[2]
                toAbbr = []
                toExpan = []
                for op in ops:
                    old = op[0]
                    new = op[1]
                    if len(old) == 1:
                        toExpan.append([old, new])
                    else:
                        toAbbr.append([old, new])
                if len(toExpan) >= 2:
                    n = len(toExpan)
                    newExpan = []
                    for i in range(2**n):
                        exWord = ["", ""]
                        for k in range(n):
                            if (i >> k) & 1:
                                exWord = [
                                    exWord[0] + toExpan[k][0],
                                    exWord[1] + toExpan[k][1],
                                ]
                        if exWord != ["", ""]:
                            newExpan.append(exWord)
                    toExpan = newExpan

                # Expan
                for i in toExpan:
                    if i[0] in oldDict["normalized"]:
                        id = oldDict["normalized"].index(i[0])
                        oldDict["normalized"][id] = i[1]
                        oldDict["heuristic"][id] = "ST"
                    elif i[1] in oldDict["normalized"]:
                        id = oldDict["normalized"].index(i[1])
                        oldDict["heuristic"][id] = "ST"
                for i in toAbbr:
                    if i[0] in oldDict["normalized"]:
                        id = oldDict["normalized"].index(i[0])
                        oldDict["normalized"][id] = i[1]
                        oldDict["heuristic"][id] = "ST"
                        oldDict["postag"][id] = "NN"
                        oldDict["pattern"] = []

            elif heu == "H2":
                oldWord = format[2]
                newWord = format[3]
                if len(oldWord) < len(newWord):
                    if oldWord in oldDict["split"]:
                        id = oldDict["split"].index(oldWord)
                        oldDict["normalized"][id] = newWord
                        oldDict["heuristic"][id] = "ST"
                else:
                    if oldWord in oldDict["split"]:
                        id = oldDict["split"].index(oldWord)
                        oldDict["normalized"][id] = newWord
                        oldDict["heuristic"][id] = "ST"
                        oldDict["postag"][id] = "NN"
                    return
            elif heu == "H3":
                oldWord = format[2]
                newWord = format[3]
                if len(oldWord) < len(newWord):
                    if oldWord in oldDict["split"]:
                        id = oldDict["split"].index(oldWord)
                        oldDict["normalized"][id] = newWord
                        oldDict["heuristic"][id] = "ST"

                else:
                    if oldWord in oldDict["split"]:
                        id = oldDict["split"].index(oldWord)
                        oldDict["normalized"][id] = newWord
                        oldDict["heuristic"][id] = "ST"
                        oldDict["postag"][id] = "NN"
                    return
        elif operation == "Conjugate" or operation == "Plural":
            oldWord = format[1]
            newWord = format[2]
            if oldWord in oldDict["expanded"]:
                id = oldDict["expanded"].index(oldWord)
                oldDict["postag"][id] = "NN"
                oldDict["normalized"][id] = newWord

        else:
            _logger.error("undefined format operation")

    def __applyOrder(self, oldDict, order):
        oldWords = deepcopy(oldDict["normalized"])
        oldOrder = order[0]
        newOrder = order[1]
        useOrderWords = []
        for w in oldWords:
            if w in oldOrder:
                useOrderWords.append(w)
        if len(useOrderWords) <= 1:
            return

        # return if the same word is included in the identifier
        if len(useOrderWords) != len(set(useOrderWords)):
            return

        orderedWords = []
        newHeu = []
        newPostag = []
        for w in newOrder:
            if w in useOrderWords:
                orderedWords.append(w)
                oldId = oldWords.index(w)
                newHeu.append(oldDict["heuristic"][oldId])
                newPostag.append(oldDict["postag"][oldId])
        if useOrderWords == orderedWords:
            return
        for i in range(len(useOrderWords)):
            oldId = oldWords.index(useOrderWords[i])
            oldDict["normalized"][oldId] = orderedWords[i]
            oldDict["heuristic"][oldId] = newHeu[i]
            oldDict["postag"][oldId] = newPostag[i]

        return

    def __applyDelete(self, oldDict, deletedWords):
        _logger.debug(f"delete {deletedWords}")
        idx = self.__findIndex(deletedWords, oldDict[self.__wordColumn])
        if idx == -1:
            return
        deletedWordLen = len(deletedWords)
        if deletedWordLen == len(oldDict[self.__wordColumn]):
            _logger.debug(f"cannot delete {deletedWords} because words will be empty")
            return
        newSep = max(oldDict["delimiter"][idx : idx + deletedWordLen + 1], key=len)
        # word
        self.__replaceSlice(oldDict[self.__wordColumn], idx, idx + deletedWordLen, [])
        # case
        self.__replaceSlice(oldDict["case"], idx, idx + deletedWordLen, [])
        # delimiter
        self.__replaceSlice(
            oldDict["delimiter"], idx, idx + deletedWordLen + 1, [newSep]
        )
        if self.__normalize:
            # postag
            self.__replaceSlice(oldDict["postag"], idx, idx + deletedWordLen, [])
            # heuristic
            self.__replaceSlice(oldDict["heuristic"], idx, idx + deletedWordLen, [])

    def __applyReplace(self, oldDict, deletedWords, insertedWords):
        _logger.debug(f"replace {deletedWords} to {insertedWords}")
        idx = self.__findIndex(deletedWords, oldDict[self.__wordColumn])
        if idx == -1:
            return
        deletedWordLen = len(deletedWords)
        insertedWordLen = len(insertedWords)
        # word
        self.__replaceSlice(
            oldDict[self.__wordColumn], idx, idx + deletedWordLen, insertedWords
        )
        # case
        toCases = getPaddingList(
            oldDict["case"][idx : idx + deletedWordLen], insertedWordLen
        )
        self.__replaceSlice(oldDict["case"], idx, idx + deletedWordLen, toCases)
        # delimiter
        fromDelims = oldDict["delimiter"][idx : idx + deletedWordLen + 1]
        toDelims = [fromDelims[0]] + [""] * (insertedWordLen - 1) + [fromDelims[-1]]
        self.__replaceSlice(
            oldDict["delimiter"], idx, idx + deletedWordLen + 1, toDelims
        )
        if self.__normalize:
            # postag
            toPostags = getPaddingList(
                oldDict["postag"][idx : idx + deletedWordLen], insertedWordLen
            )
            self.__replaceSlice(oldDict["postag"], idx, idx + deletedWordLen, toPostags)
            # heuristic
            toHeuristics = getPaddingList(
                oldDict["heuristic"][idx : idx + deletedWordLen], insertedWordLen
            )
            self.__replaceSlice(
                oldDict["heuristic"], idx, idx + deletedWordLen, toHeuristics
            )

    def __applyInsert(self, oldDict, insertedWords):
        _logger.debug(f"insert {insertedWords}")
        insertedWordLen = len(insertedWords)
        newNorm = self.__new[self.__wordColumn]
        oldNorm = oldDict[self.__wordColumn]
        idx = self.__findIndex(insertedWords, newNorm)
        before = idx - 1
        beforeWord = [newNorm[before] if before >= 0 else ""]
        after = idx + insertedWordLen
        afterWord = [newNorm[after] if after < len(newNorm) else ""]
        beforeIdx = self.__findIndex(beforeWord, oldNorm)
        afterIdx = self.__findIndex(afterWord, oldNorm)
        if afterIdx != -1:
            contextIdx = afterIdx
            replIdx = afterIdx
        elif beforeIdx != -1:
            contextIdx = beforeIdx
            replIdx = beforeIdx + 1
        else:
            return
        # word
        self.__replaceSlice(oldDict[self.__wordColumn], replIdx, replIdx, insertedWords)
        # case
        newCase = [oldDict["case"][contextIdx]] * insertedWordLen
        self.__replaceSlice(oldDict["case"], replIdx, replIdx, newCase)
        # delim
        newDelim = [""] * insertedWordLen
        self.__replaceSlice(oldDict["delimiter"], replIdx, replIdx, newDelim)
        if self.__normalize:
            # postag
            # newPostag = [oldDict['postag'][contextIdx]] * insertedWordLen
            newPostag = ["NN"] * insertedWordLen
            self.__replaceSlice(oldDict["postag"], replIdx, replIdx, newPostag)
            # heuristic
            newHeuristic = [oldDict["heuristic"][contextIdx]] * insertedWordLen
            self.__replaceSlice(oldDict["heuristic"], replIdx, replIdx, newHeuristic)

    def __findIndex(self, words, target):
        iterRange = len(target) - len(words) + 1
        _logger.debug(f"find {words} from {target}")
        for i in range(iterRange):
            if target[i : i + len(words)] == list(words):
                _logger.debug(f"found at {i}")
                return i
        return -1

    def __replaceSlice(self, target, start, end, value):
        target[start:end] = list(value)
