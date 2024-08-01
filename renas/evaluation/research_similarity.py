import argparse
import csv
import os

import matplotlib.pyplot as plt
import numpy as np

from renas.evaluation.util.recommended_dict_operator import RecommendDictOperator


def setArgument():
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "source", help="set directory containing repositories to be analyzed", nargs="+"
    )

    args = parser.parse_args()
    return args


class similarity:
    def __init__(self):
        self.similarity_data = []

    def show_similarity(self, path):
        width = 0.1
        sData = np.array(self.similarity_data)
        bins = np.arange(0, 1.2, width)
        counts, bin = np.histogram(sData[:, 7], bins=bins, density=True)

        hist_values = counts * width
        plt.hist(bin[:-1], bin, weights=hist_values)
        plt.xlabel("similarity")
        plt.ylabel("relative frequency")
        plt.savefig(os.path.join(path, "similarity.svg"))

        with open(os.path.join(path, "similarity.csv"), "w") as sw:
            writer = csv.writer(sw)
            writer.writerow(
                [
                    "commit",
                    "name1 file",
                    "name1 line",
                    "name1",
                    "name2 file",
                    "name2 line",
                    "name2",
                    "similarity",
                ]
            )
            for data in self.similarity_data:
                writer.writerow(data)

    def __set_similarity(self, corename):
        for num1 in range(len(corename)):
            for num2 in range(num1 + 1, len(corename)):
                name1 = corename[num1]["normalized"]
                name2 = corename[num2]["normalized"]
                similarity = self.__calc_similarity(name1, name2)
                self.similarity_data.append(
                    [
                        corename[num1]["commit"],
                        corename[num1]["files"],
                        str(corename[num1]["line"]),
                        " ".join(name1),
                        corename[num2]["files"],
                        str(corename[num2]["line"]),
                        " ".join(name2),
                        similarity,
                    ]
                )

    def __calc_similarity(self, aNormalize, bNormalize):
        similarity = (
            len(set(aNormalize) & set(bNormalize))
            * 2
            / (len(aNormalize) + len(bNormalize))
        )
        return similarity

    def __research(self, corename):
        for cv in corename.values():
            self.__set_similarity(cv)
        return

    def main(self, _recommend_dict_operator: RecommendDictOperator):
        while True:
            corename = _recommend_dict_operator.get_corename()
            self.__research(corename)
            if not _recommend_dict_operator.set_next_commit():
                break

        return self.similarity_data


if __name__ == "__main__":
    args = setArgument()
    _similarity = similarity()
    _recommend_dict_operator = RecommendDictOperator()
    for root in args.source:
        _recommend_dict_operator.set_recommend_json(root)
        _similarity.main(_recommend_dict_operator)

    _similarity.show_similarity(root)
