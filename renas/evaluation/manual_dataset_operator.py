import argparse
import os

import pandas as pd

from renas.evaluation.dataset_operator import DatasetOperator


class ManualDatasetOperator(DatasetOperator):

    def __init__(self):
        self.key_to_id = {}
        self.count = 0

    def print_count(self, num=1):
        print(f"The number of corenamings = {int(self.count)}")

    def setArgument(self):
        parser = argparse.ArgumentParser()
        parser.add_argument(
            "source",
            help="set directory containing repositories to be analyzed",
            nargs="+",
        )

        args = parser.parse_args()
        return args

    def changeTypeName(self, type_of_identifier):
        if type_of_identifier == "Parameter":
            return "ParameterName"
        elif type_of_identifier == "Variable":
            return "VariableName"
        elif type_of_identifier == "Method":
            return "MethodName"
        elif type_of_identifier == "Attribute":
            return "FieldName"
        elif type_of_identifier == "Class":
            return "ClassName"
        else:
            return None

    def get_key(self, ginfo):
        return (
            ginfo["commit"]
            + ginfo["files"]
            + ginfo["typeOfIdentifier"]
            + ginfo["oldname"]
            + str(ginfo["line"])
        )

    def get_correct_ids(self, ginfo):
        correct_keys = []
        if ginfo["commit"] not in self.corename_group:
            return []
        for keys in self.corename_group[ginfo["commit"]].values():
            for key in keys:
                if key == self.get_key(ginfo):
                    correct_keys += keys

        correct_ids = set()
        for key in correct_keys:
            if key not in self.key_to_id:
                continue
            correct_ids.add(self.key_to_id[key])
        if len(correct_ids) >= 1:
            correct_ids.remove(ginfo["id"])
        return correct_ids

    def set_corename_list(self, goldset):
        pass

    def set_manual_dataset(self, root, recommend_json):
        md_path = os.path.join(root, "manualValidation.csv")
        manual_dataset = pd.read_csv(md_path).to_dict(orient="records")

        self.corename_group = {}

        for row in manual_dataset:
            concept_rename = row["conceptRename?"]
            corename = row["coRename"]
            commit = row["commit"]
            row["typeOfIdentifier"] = self.changeTypeName(row["type"])
            row["files"] = row["file"]
            row["oldname"] = row["oldName"]

            if corename == -1 or concept_rename != "TRUE":
                continue

            if commit not in self.corename_group:
                self.corename_group[commit] = {}

            if corename not in self.corename_group[commit]:
                self.corename_group[commit][corename] = []

            self.corename_group[commit][corename].append(self.get_key(row))

        for commit, corename_dic in self.corename_group.items():
            if commit not in recommend_json:
                continue
            goldset = recommend_json[commit]["goldset"]
            for data_keys in corename_dic.values():
                self.count += 1
                for d_key in data_keys:
                    for ginfo in goldset:
                        g_key = self.get_key(ginfo)
                        if d_key == g_key:
                            self.key_to_id[g_key] = ginfo["id"]
                            break

    def get_corename(self):
        return self.corename_group
