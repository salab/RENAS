from renas.approaches.util.name import KgExpanderSplitter

_splitter = KgExpanderSplitter()


def splitIdentifier(name):
    return _splitter.split(name)


def getPaddingList(elements, length):
    if len(elements) < length:
        last = elements[-1]
        paddingLen = length - len(elements)
        return elements + [last] * paddingLen
    else:
        return elements[:length]


def printDict(dict, *keys):
    result = ""
    for k in keys:
        if k not in dict.keys():
            continue
        result += f"{k}: {dict[k]}, "
    return result
