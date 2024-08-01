import gzip
import json
import os
from collections import deque

from renas.evaluation.identified_dataset_operator import IdentifiedDatasetOperator
from renas.evaluation.manual_dataset_operator import ManualDatasetOperator


class RecommendDictOperator:

    def __init__(self):
        self.now_commit = ""
        self.now_goldset_num = 0
        self.alpha = 0.5
        self.threshold = 0.53
        self.correct_database = None

    def set_renas_alpha(self, alpha):
        self.alpha = alpha

    def set_renas_threshold(self, threshold):
        self.threshold = threshold

    def set_recommend_json(self, repo_path, manual=False):
        file_path = os.path.join(repo_path, "recommend.json.gz")
        if not os.path.isfile(file_path):
            print("error: file is not found")
            exit(1)

        with gzip.open(file_path, "r") as f:
            json_data = json.load(f)
        self.__recommend_json = json_data
        if manual:
            self.correct_database = ManualDatasetOperator()
            self.correct_database.set_manual_dataset(repo_path, json_data)
        else:
            self.correct_database = IdentifiedDatasetOperator()
        return self.initialize()

    def initialize(self):
        self.commit_list = deque(list(self.__recommend_json.keys()))
        if len(self.commit_list) == 0:
            return False

        self.now_commit = self.commit_list.popleft()
        self.now_goldset_num = 0
        self.correct_database.set_corename_list(
            self.__recommend_json[self.now_commit]["goldset"]
        )
        return True

    def get_next_recommend(self):
        commit = self.now_commit
        goldset_num = self.now_goldset_num
        recommend_dict = {}
        correct_sets = []
        if len(self.__recommend_json[commit]["goldset"]) == goldset_num:
            if not self.set_next_commit():
                if self.alpha == 1.0:
                    self.correct_database.print_count(21)
                return {}, []
            return self.get_next_recommend()
        if self.__recommend_json[commit]["goldset"][goldset_num]["id"] == "":
            self.now_goldset_num += 1
            return self.get_next_recommend()
        for approach in self.__recommend_json[commit].keys():
            if approach == "goldset":
                correct_sets = self.correct_database.get_correct_ids(
                    self.__recommend_json[commit][approach][goldset_num]
                )
            elif approach == "renas":
                rec_list = []
                for rec in self.__recommend_json[commit][approach][str(goldset_num)]:
                    rec["priority"] = (
                        self.alpha * rec["similarity"]
                        + (1 - self.alpha) / rec["relationship"]
                    )
                    if rec["priority"] >= self.threshold:
                        rec_list.append(rec)
                recommend_dict[approach] = rec_list
            else:
                recommend_dict[approach] = self.__recommend_json[commit][approach][
                    str(goldset_num)
                ]
        self.now_goldset_num += 1
        return recommend_dict, correct_sets

    def set_next_commit(self):
        if len(self.commit_list) == 0:
            return False
        self.now_commit = self.commit_list.popleft()
        self.now_goldset_num = 0
        self.correct_database.set_corename_list(
            self.__recommend_json[self.now_commit]["goldset"]
        )
        return True

    def get_corename(self):
        return self.correct_database.get_corename()
