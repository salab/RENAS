import argparse
import os
from logging import INFO, Formatter, StreamHandler, getLogger

from renas.evaluation.rq1_index import RQ1Index
from renas.evaluation.util.recommended_dict_operator import RecommendDictOperator

_logger = getLogger(__name__)


def setArgument():
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "source", help="set directory containing repositories to be analyzed", nargs="+"
    )

    args = parser.parse_args()
    return args


def setLogger(level):
    _logger.setLevel(level)
    root_logger = getLogger()
    handler = StreamHandler()
    handler.setLevel(level)
    formatter = Formatter("[%(asctime)s] %(name)s -- %(levelname)s : %(message)s")
    handler.setFormatter(formatter)
    root_logger.addHandler(handler)
    root_logger.setLevel(INFO)
    return root_logger


def calculate_fscore(precision, recall):
    if precision == 0 or recall == 0:
        return 0
    else:
        return 2 * precision * recall / (precision + recall)


def calculate_values(recommends, correct_sets):
    recommend_length = len(recommends)
    correct_length = len(correct_sets)
    true_recommend_length = 0
    for rec in recommends:
        if rec["id"] in correct_sets:
            true_recommend_length += 1
    precision = true_recommend_length / recommend_length if recommend_length != 0 else 0
    recall = true_recommend_length / correct_length
    fscore = calculate_fscore(precision, recall)
    return RQ1Index(precision, recall, fscore, true_recommend_length)


def evaluate(_recommend_dict_operator: RecommendDictOperator):
    results_dict = {}
    count = 0
    while True:
        recommends, correct_sets = _recommend_dict_operator.get_next_recommend()
        if recommends == {}:
            break
        if len(correct_sets) <= 0:
            continue
        count += 1
        for approach in recommends.keys():
            r_id: RQ1Index = calculate_values(recommends[approach], correct_sets)
            if approach not in results_dict:
                results_dict[approach] = []
            results_dict[approach].append(r_id)
    print(f"The number of renaming = {count}")
    return results_dict


def calc_average_result(result_dicts, repo_path):
    result_path = os.path.join(repo_path, "result")
    os.makedirs(result_path, exist_ok=True)
    average_dict = {i: 0 for i in result_dicts.keys()}
    for approach, results in result_dicts.items():
        recommend_number = len(results)
        precision_average = (
            sum(list(map(lambda x: x.precision, results))) / recommend_number
        )
        recall_average = sum(list(map(lambda x: x.recall, results))) / recommend_number
        fscore_average = sum(list(map(lambda x: x.fscore, results))) / recommend_number
        _logger.info(
            f"{approach}: precision = {precision_average}, \
            recall = {recall_average}, fscore = {fscore_average}"
        )
        average_dict[approach] = [precision_average, recall_average, fscore_average]
    return average_dict


def main(repo_path, _recommend_dict_operator: RecommendDictOperator):
    result_dicts = evaluate(_recommend_dict_operator)
    average_dict = calc_average_result(result_dicts, repo_path)
    return average_dict


if __name__ == "__main__":
    args = setArgument()
    setLogger(INFO)
    performance_path = os.path.join("result", "approach_performance.csv")
    _recommend_dict_operator = RecommendDictOperator()
    for repo_p in args.source:
        _recommend_dict_operator.set_recommend_json(repo_p)
        main(repo_p, _recommend_dict_operator)
