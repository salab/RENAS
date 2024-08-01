import argparse
import csv
import os
import pathlib
from logging import INFO, Formatter, StreamHandler, getLogger

from renas.evaluation import approach_performance, renas_parameter
from renas.evaluation.research_similarity import similarity
from renas.evaluation.util.recommended_dict_operator import RecommendDictOperator

_logger = getLogger(__name__)


def setArgument():
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "source", help="set directory containing repositories to be analyzed", nargs="+"
    )
    parser.add_argument(
        "-sim", help="research similarity", action="store_true", default=False
    )
    parser.add_argument(
        "-pre",
        help="preliminary survey (determine threshold and ratio)",
        action="store_true",
        default=False,
    )
    parser.add_argument(
        "-rq1", help="research question 1", action="store_true", default=False
    )
    parser.add_argument(
        "-rq2", help="research question 2", action="store_true", default=False
    )
    parser.add_argument(
        "-manual", help="use manual dataset", action="store_true", default=False
    )
    parser.add_argument(
        "-count",
        help="count success recommendation in direct relationship and indirect relationship",
        action="store_true",
        default=False,
    )

    args = parser.parse_args()
    return args


def set_logger(level):
    root_logger = getLogger()
    root_logger.setLevel(level)
    handler = StreamHandler()
    handler.setLevel(level)
    formatter = Formatter("[%(asctime)s] %(name)s -- %(levelname)s : %(message)s")
    handler.setFormatter(formatter)
    root_logger.addHandler(handler)
    return root_logger


def show_rq1(rq1_dict, dir_path):
    output_path = os.path.join(dir_path, "rq1.csv")
    repository_average = {}
    with open(output_path, "w") as op:
        writer = csv.writer(op)
        writer.writerow(
            [
                "project name",
                "approach",
                "precision average",
                "recall average",
                "fscore average",
            ]
        )
        for repo, result in rq1_dict.items():

            for approach, value in result.items():
                writer.writerow([repo, approach] + list(map(str, value)))
                if approach not in repository_average:
                    repository_average[approach] = [0, 0, 0]
                repository_average[approach] = list(
                    map(lambda x, y: x + y, repository_average[approach], value)
                )

        for approach in repository_average.keys():
            repository_average[approach] = list(
                map(lambda x: x / len(rq1_dict), repository_average[approach])
            )
        for approach, value in repository_average.items():
            writer.writerow(["average", approach] + value)


def merge(repos_path, output_dir):
    alpha_beta_dir = {}
    rank_eval_dir = {}
    for dir_path in repos_path:
        beta_path = os.path.join(dir_path, "result", "values_by_beta.csv")
        rank_eval_path = os.path.join(dir_path, "result", "ranking_evaluation.csv")
        if os.path.isfile(beta_path):
            with open(beta_path, "r") as f:
                reader = csv.reader(f)
                rows = [row for row in reader]
            for row in rows[1:]:
                if row[0] not in alpha_beta_dir:
                    alpha_beta_dir[row[0]] = {}
                if row[1] not in alpha_beta_dir[row[0]]:
                    alpha_beta_dir[row[0]][row[1]] = [0, 0, 0]
                alpha_beta_dir[row[0]][row[1]] = [
                    alpha_beta_dir[row[0]][row[1]][0] + float(row[2]),
                    alpha_beta_dir[row[0]][row[1]][1] + float(row[3]),
                    alpha_beta_dir[row[0]][row[1]][2] + float(row[4]),
                ]
        if os.path.isfile(rank_eval_path):
            with open(rank_eval_path, "r") as f:
                reader = csv.reader(f)
                rows = [row for row in reader]
            for row in rows[1:]:
                if row[0] not in rank_eval_dir:
                    rank_eval_dir[row[0]] = [0, 0, 0, 0, 0]
                for i in range(5):
                    rank_eval_dir[row[0]][i] += float(row[i + 1])

    with open(os.path.join(output_dir, "values_by_alpha_beta.csv"), "w") as w:
        writer = csv.writer(w)
        writer.writerow(
            ["alpha", "beta", "precision average", "recall average", "fscore average"]
        )
        for alpha, beta_dir in alpha_beta_dir.items():
            for beta, values in beta_dir.items():
                writer.writerow(
                    [alpha, beta] + list(map(lambda x: x / len(repos_path), values))
                )

    with open(os.path.join(output_dir, "ranking_evaluation.csv"), "w") as w:
        writer = csv.writer(w)
        writer.writerow(
            ["alpha", "MAP", "MRR", "top1 Recall", "top5 Recall", "top10 Recall"]
        )
        for alpha, values in rank_eval_dir.items():
            writer.writerow([alpha] + list(map(lambda x: x / len(repos_path), values)))


def main(repos_path, args):
    root_path = pathlib.Path("/work/result")
    os.makedirs(root_path, exist_ok=True)
    _recommend_dict_operator = RecommendDictOperator()
    if args.sim:
        _similarity = similarity()
        for repo in repos_path:
            _logger.info(f"similarity: {repo} is evaluating")
            _recommend_dict_operator.set_recommend_json(repo)
            _similarity.main(_recommend_dict_operator)
        output_path = os.path.join(root_path, "similarity")
        os.makedirs(output_path, exist_ok=True)
        _similarity.show_similarity(output_path)

    if args.pre:
        _recommend_dict_operator.set_renas_threshold(0.53)
        _recommend_dict_operator.set_renas_alpha(0.5)
        for repo in repos_path:
            _logger.info(f"preliminary: {repo} is evaluating")
            _renas_parameter = renas_parameter.RenasParameter()
            _recommend_dict_operator.set_recommend_json(repo)
            _renas_parameter.main(_recommend_dict_operator)
            output_path = os.path.join(repo, "result")
            os.makedirs(output_path, exist_ok=True)
            _renas_parameter.show_figure(output_path)
        result_dir = os.path.join(root_path, "preliminary")
        os.makedirs(result_dir, exist_ok=True)
        merge(repos_path, result_dir)

        # _renas_parameter.show_figure(output_path)

    if args.rq1:
        rq1_dict = {}
        _recommend_dict_operator.set_renas_threshold(0.53)
        _recommend_dict_operator.set_renas_alpha(0.5)
        for repo in repos_path:
            _logger.info(f"rq1: {repo} is evaluating")
            if args.manual:
                _recommend_dict_operator.set_recommend_json(repo, True)
            else:
                _recommend_dict_operator.set_recommend_json(repo)
            result_rq1 = approach_performance.main(repo, _recommend_dict_operator)
            rq1_dict[repo] = result_rq1
        output_path = (
            os.path.join(root_path, "rq1")
            if not args.manual
            else os.path.join(root_path, "rq1_manual")
        )
        os.makedirs(output_path, exist_ok=True)
        show_rq1(rq1_dict, output_path)

    if args.rq2:
        for repo in repos_path:
            _renas_parameter = renas_parameter.RenasParameter()
            _logger.info(f"rq2: {repo} is evaluating")
            if args.manual:
                _recommend_dict_operator.set_recommend_json(repo, True)
            else:
                _recommend_dict_operator.set_recommend_json(repo)
            _renas_parameter.main(_recommend_dict_operator)
            output_path = os.path.join(repo, "result")
            os.makedirs(output_path, exist_ok=True)
            _renas_parameter.show_figure(output_path)
        for repo in repos_path:
            input_path = os.path.join(repo, "result", "ranking_evaluation.csv")
            if not os.path.isfile(input_path):
                continue
        result_dir = (
            os.path.join(root_path, "rq2")
            if not args.manual
            else os.path.join(root_path, "rq2_manual")
        )
        os.makedirs(result_dir, exist_ok=True)
        merge(repos_path, result_dir)

    if args.count:
        rq1_dict = {}
        for repo in repos_path:
            _logger.info(f"count: {repo} is evaluating")
            _recommend_dict_operator.set_renas_threshold(0)
            if args.manual:
                _recommend_dict_operator.set_recommend_json(repo, True)
            else:
                _recommend_dict_operator.set_recommend_json(repo)
            result_rq1 = approach_performance.evaluate(_recommend_dict_operator)
            rq1_dict[repo] = result_rq1
        direct_count = count_direct_relationship(rq1_dict)
        indirect_count = count_indirect_relationship(rq1_dict) - direct_count
        output_path = os.path.join(root_path, "count")
        os.makedirs(output_path, exist_ok=True)
        with open(os.path.join(output_path, "count.txt"), "w") as fw:
            fw.write(f"All success recommendation = {direct_count + indirect_count}\n")
            fw.write(f"direct relationship = {direct_count}\n")
            fw.write(f"indirect relationship = {indirect_count}\n")


def count_direct_relationship(rq1_dict):
    s = 0
    for vals in rq1_dict.values():
        for v in vals["normalizeRelation"]:
            s += v.true_count
    return s


def count_indirect_relationship(rq1_dict):
    s = 0
    for vals in rq1_dict.values():
        for v in vals["renas"]:
            s += v.true_count
    return s


if __name__ == "__main__":
    args = setArgument()
    _logger = set_logger(INFO)
    main(args.source, args)
