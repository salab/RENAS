import csv
import math
import os

import matplotlib.pyplot as plt
import numpy as np

from renas.evaluation.rq2_index import RQ2Index
from renas.evaluation.util.recommended_dict_operator import RecommendDictOperator


class RenasParameter:
    def __init__(self):
        self.alpha_count = 20
        self.threshold_count = 100
        self.approach = "renas"
        self.result_dict = {
            str(i / self.alpha_count): {} for i in range(self.alpha_count + 1)
        }

    def calculate_fscore(self, precision, recall):
        if precision == 0 or recall == 0:
            return 0
        else:
            return 2 * precision * recall / (precision + recall)

    def calculate_map(self, true_top_n, correct_length):
        correct_count = 0
        map_value = 0
        for sl in range(len(true_top_n)):
            if true_top_n[sl] == 1:
                correct_count += 1
                map_value += correct_count / (sl + 1)
            elif true_top_n[sl] > 1:
                print("something wrong")
                exit(1)
        map_value = map_value / correct_length
        return map_value

    def calculate_mrr(self, true_top_n):
        mrr_value = 0
        for sl in range(len(true_top_n)):
            if true_top_n[sl] == 1:
                mrr_value = 1 / (sl + 1)
                break
        return mrr_value

    def calculate_values(self, recommends, correct_sets):
        recommend_length = len(recommends)
        correct_length = len(correct_sets)
        true_count_by_threshold = [0 for i in range(self.threshold_count + 1)]
        recommend_by_threshold = [0 for i in range(self.threshold_count + 1)]
        true_top_n = [0 for i in range(recommend_length)]
        for ranking in range(len(recommends)):
            rec = recommends[ranking]
            priority = math.floor(rec["priority"] * 100)
            recommend_by_threshold[priority] += 1
            if rec["id"] in correct_sets:
                true_count_by_threshold[priority] += 1
                true_top_n[ranking] += 1

        precision_by_threshold = [0 for i in range(self.threshold_count + 1)]
        recall_by_threshold = [0 for i in range(self.threshold_count + 1)]
        fscore_by_threshold = [0 for i in range(self.threshold_count + 1)]
        true_sum = 0
        recommend_sum = 0
        for thre in range(self.threshold_count, -1, -1):
            true_sum += true_count_by_threshold[thre]
            recommend_sum += recommend_by_threshold[thre]
            precision = true_sum / recommend_sum if recommend_sum != 0 else 0
            recall = true_sum / correct_length
            precision_by_threshold[thre] = precision
            recall_by_threshold[thre] = recall
            fscore_by_threshold[thre] = self.calculate_fscore(precision, recall)

        top_n_recall = [0 for i in range(10)]
        top_sum = 0
        for tn in range(min(len(true_top_n), 10)):
            top_sum += true_top_n[tn]
            top_n_recall[tn] = top_sum / correct_length

        map_value = self.calculate_map(true_top_n, correct_length)
        mrr_value = self.calculate_mrr(true_top_n)

        return RQ2Index(
            precision_by_threshold,
            recall_by_threshold,
            fscore_by_threshold,
            map_value,
            mrr_value,
            top_n_recall,
        )

    def evaluate(self, _recommend_dict_operator: RecommendDictOperator):
        result_count = 0
        values = {}
        values["map"] = 0
        values["mrr"] = 0
        values["top1"] = 0
        values["top5"] = 0
        values["top10"] = 0
        values["precision_list"] = np.array(
            [0.0 for i in range(self.threshold_count + 1)]
        )
        values["recall_list"] = np.array([0.0 for i in range(self.threshold_count + 1)])
        values["fscore_list"] = np.array([0.0 for i in range(self.threshold_count + 1)])
        while True:
            recommends, correct_sets = _recommend_dict_operator.get_next_recommend()
            if recommends == {}:
                break
            if len(correct_sets) <= 0:
                continue
            recommend = sorted(
                recommends[self.approach],
                key=lambda x: (x["priority"], x["id"]),
                reverse=True,
            )
            r_id: RQ2Index = self.calculate_values(recommend, correct_sets)
            result_count += 1

            values["map"] += r_id.map_value
            values["mrr"] += r_id.mrr_value
            values["top1"] += r_id.top_n_recall[0]
            values["top5"] += r_id.top_n_recall[4]
            values["top10"] += r_id.top_n_recall[9]
            values["precision_list"] += np.array(r_id.precision_by_threshold)
            values["recall_list"] += np.array(r_id.recall_by_threshold)
            values["fscore_list"] += np.array(r_id.fscore_by_threshold)

        values["map"] = values["map"] / result_count
        values["mrr"] = values["mrr"] / result_count
        values["top1"] = values["top1"] / result_count
        values["top5"] = values["top5"] / result_count
        values["top10"] = values["top10"] / result_count
        values["precision_list"] = values["precision_list"] / result_count
        values["recall_list"] = values["recall_list"] / result_count
        values["fscore_list"] = values["fscore_list"] / result_count
        if _recommend_dict_operator.alpha == 0:
            print(f"The number of renamings = {result_count}")
        return values

    def show_map(self, dir_path):
        map_values = []
        alpha_list = list(self.result_dict.keys())
        for alpha in alpha_list:
            value = self.result_dict[alpha]["map"]
            map_values.append(value)
        fig, ax = plt.subplots()
        plt.plot(list(map(float, alpha_list)), map_values)
        plt.xlabel("α")
        plt.ylabel("MAP")
        maxIndex = np.argmax(map_values)
        maxValue = map_values[maxIndex]
        plt.plot(
            maxIndex / (len(alpha_list) - 1), maxValue, ".", markersize=7, color="r"
        )
        fig.savefig(os.path.join(dir_path, "MAP.svg"))
        plt.close(fig)

    def show_mrr(self, dir_path):
        mrr_values = []
        alpha_list = list(self.result_dict.keys())
        for alpha in alpha_list:
            value = self.result_dict[alpha]["mrr"]
            mrr_values.append(value)
        fig, ax = plt.subplots()
        plt.plot(list(map(float, alpha_list)), mrr_values)
        plt.xlabel("α")
        plt.ylabel("MRR")
        maxIndex = np.argmax(mrr_values)
        maxValue = mrr_values[maxIndex]
        plt.plot(
            maxIndex / (len(alpha_list) - 1), maxValue, ".", markersize=7, color="r"
        )
        fig.savefig(os.path.join(dir_path, "MRR.svg"))
        plt.close(fig)

    def show_topn_recall(self, dir_path):
        top_values = {1: [], 5: [], 10: []}
        alpha_list = list(self.result_dict.keys())
        for alpha in alpha_list:
            top_values[1].append(self.result_dict[alpha]["top1"])
            top_values[5].append(self.result_dict[alpha]["top5"])
            top_values[10].append(self.result_dict[alpha]["top10"])
        for topn, value in top_values.items():
            fig, ax = plt.subplots()
            plt.plot(list(map(float, alpha_list)), value)
            plt.xlabel("α")
            plt.ylabel(f"top{topn}-recall")
            maxIndex = np.argmax(value)
            maxValue = value[maxIndex]
            plt.plot(
                maxIndex / (len(alpha_list) - 1), maxValue, ".", markersize=7, color="r"
            )
            fig.savefig(os.path.join(dir_path, f"top{topn}-recall.svg"))
            plt.close(fig)

    def show_value_by_threshold(self, dir_path):
        alpha_list = list(self.result_dict.keys())
        with open(os.path.join(dir_path, "values_by_beta.csv"), "w") as aw:
            writer = csv.writer(aw)
            writer.writerow(
                [
                    "alpha",
                    "beta",
                    "precision average",
                    "recall average",
                    "fscore average",
                ]
            )
        for alpha in alpha_list:
            threshold_list = [
                i / self.threshold_count for i in range(self.threshold_count + 1)
            ]
            precision_list = self.result_dict[alpha]["precision_list"]
            recall_list = self.result_dict[alpha]["recall_list"]
            fscore_list = self.result_dict[alpha]["fscore_list"]

            for name, value in zip(
                ["precision", "recall", "fscore"],
                [precision_list, recall_list, fscore_list],
            ):
                fig, ax = plt.subplots()
                plt.plot(threshold_list, value)
                plt.xlabel("threshold")
                plt.ylabel(f"{name}-alpha{alpha}")
                maxIndex = np.argmax(value)
                maxValue = value[maxIndex]
                plt.plot(
                    maxIndex / self.threshold_count,
                    maxValue,
                    ".",
                    markersize=7,
                    color="r",
                )
                fig.savefig(os.path.join(dir_path, f"{name}-alpha{alpha}.svg"))
                plt.close(fig)
            with open(os.path.join(dir_path, "values_by_beta.csv"), "a") as aw:
                writer = csv.writer(aw)
                for thre, prec, rec, fscr in zip(
                    threshold_list,
                    precision_list.tolist(),
                    recall_list.tolist(),
                    fscore_list.tolist(),
                ):
                    writer.writerow([alpha, thre, prec, rec, fscr])

    def show_value_by_alpha(self, dir_path):
        alpha_list = list(self.result_dict.keys())
        max_fscore_list = []
        max_beta_list = []
        for alpha in alpha_list:
            fscore_list = self.result_dict[alpha]["fscore_list"]
            max_fscore_list.append(max(fscore_list))
            max_beta_list.append(np.argmax(fscore_list) / self.threshold_count)
        fig, ax = plt.subplots()
        plt.plot(list(map(float, alpha_list)), max_fscore_list)
        plt.xlabel("α")
        plt.ylabel("max-fscore")
        maxIndex = np.argmax(max_fscore_list)
        maxValue = max_fscore_list[maxIndex]
        plt.plot(
            maxIndex / (len(alpha_list) - 1),
            maxValue,
            ".",
            markersize=7,
            color="r",
        )
        fig.savefig(os.path.join(dir_path, "fscore-each-alpha.svg"))
        plt.close(fig)

        with open(os.path.join(dir_path, "value_by_alpha.csv"), "w") as aw:
            writer = csv.writer(aw)
            writer.writerow(["alpha", "beta", "fscore"])
            for alp, beta, fscr in zip(alpha_list, max_beta_list, max_fscore_list):
                writer.writerow([alp, beta, fscr])

    def show_csv_ranking_data(self, dir_path):
        csv_data = []
        alpha_list = list(self.result_dict.keys())
        for alpha in alpha_list:
            map_v = self.result_dict[alpha]["map"]
            mrr_v = self.result_dict[alpha]["mrr"]
            top1_v = self.result_dict[alpha]["top1"]
            top5_v = self.result_dict[alpha]["top5"]
            top10_v = self.result_dict[alpha]["top10"]

            csv_data.append([alpha, map_v, mrr_v, top1_v, top5_v, top10_v])

        with open(os.path.join(dir_path, "ranking_evaluation.csv"), "w") as rew:
            writer = csv.writer(rew)
            writer.writerow(
                ["alpha", "MAP", "MRR", "top1 Recall", "top5 Recall", "top10 Recall"]
            )
            for data in csv_data:
                writer.writerow(data)

    def show_figure(self, dir_path):
        self.show_map(dir_path)
        self.show_mrr(dir_path)
        # self.show_topn_recall(dir_path)
        self.show_value_by_threshold(dir_path)
        self.show_value_by_alpha(dir_path)
        self.show_csv_ranking_data(dir_path)

    def main(self, _recommend_dict_operator: RecommendDictOperator):
        _recommend_dict_operator.set_renas_threshold(0)
        for num in range(self.alpha_count + 1):
            alpha = num / self.alpha_count
            _recommend_dict_operator.set_renas_alpha(alpha)
            _recommend_dict_operator.initialize()
            self.result_dict[str(alpha)] = self.evaluate(_recommend_dict_operator)
