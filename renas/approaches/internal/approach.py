from abc import ABCMeta, abstractmethod

from renas.approaches.util.rename import Rename

_RELATION_LIST = [
    "subclass",
    "descendant",
    "parent",
    "ancestor",
    "method",
    "field",
    "sibling-members",
    "comment",
    "type",
    "enclosingClass",
    "assignmentEquation",
    "pass",
    "argumentToParameter",
    "parameter",
    "enclosingMethod",
    "parameterToArgument",
    "parameterOverload",
]

_RELATION_COST = {
    "subclass": 3.0,
    "descendant": 3.0,
    "parent": 3.0,
    "ancestor": 3.0,
    "method": 4.0,
    "field": 4.0,
    "sibling-members": 1.0,
    "comment": 2.0,
    "type": 3.0,
    "enclosingClass": 4.0,
    "assignmentEquation": 1.0,
    "pass": 3.0,
    "argumentToParameter": 2.0,
    "parameter": 3.0,
    "enclosingMethod": 3.0,
    "parameterToArgument": 2.0,
    "parameterOverload": 1.0,
}

_IDENTIFIER_LIST = [
    "id",
    "name",
    "line",
    "files",
    "typeOfIdentifier",
    "split",
    "case",
    "pattern",
    "delimiter",
]


class Approach(metaclass=ABCMeta):

    def __init__(self):
        self.rename: Rename = None
        self.RELATION_LIST = _RELATION_LIST
        self.RELATION_COST = _RELATION_COST
        self.IDENTIFIER_LIST = _IDENTIFIER_LIST

    @abstractmethod
    def recommend(self):
        pass

    @abstractmethod
    def get_approach_name(self):
        return ""

    def get_operation(self):
        if self.rename is None:
            return []
        return self.rename.get_operation()

    def get_old_normalized(self):
        if self.rename is None:
            return []
        return self.rename.get_old_normalize()

    def get_id(self):
        if self.rename is None:
            return ""
        return self.rename.get_id()
