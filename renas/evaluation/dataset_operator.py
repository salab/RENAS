from abc import ABCMeta, abstractmethod


class DatasetOperator(metaclass=ABCMeta):
    def __init__(self):
        self.count = 0

    @abstractmethod
    def set_corename_list(self, goldset):
        pass

    @abstractmethod
    def get_corename(self):
        pass

    def get_correct_ids(self, ginfo):
        pass
