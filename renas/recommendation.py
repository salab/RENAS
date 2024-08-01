import argparse
import gzip
import os
import pathlib
import time
from datetime import timedelta
from logging import INFO, Formatter, StreamHandler, getLogger

import pandas as pd
import simplejson

from renas.approaches import none, normalize_relation, relation, renas
from renas.approaches.internal import approach
from renas.approaches.util.extable import ExTable
from renas.approaches.util.rename import setAbbrDic

_logger = getLogger(__name__)
RECOMMEND_APPROACHES: list[approach.Approach] = [
    none.NoneApproach(),
    relation.Relation(),
    normalize_relation.NormalizeRelation(),
    renas.Renas(),
]


def set_argument():
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "source", help="set directory containing repositories to be analyzed", nargs="+"
    )
    parser.add_argument(
        "-f",
        help="Even if recommend.json.gz exists, re-execute recommendation",
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


def read_rename_file(root):
    json_path = os.path.join(root, "goldset.json.gz")
    try:
        json_data = pd.read_json(json_path, orient="records", compression="gzip")
    except ValueError:
        _logger.error(f"{json_path} does not exists")
        return
    except KeyError:
        _logger.error(f"{json_path} is empty")
        return
    return json_data


def recommend(repo, force):
    startTime = time.time()
    root = pathlib.Path(repo)
    archive_root = root.joinpath("archives")
    output_path = root.joinpath("recommend.json.gz")

    if os.path.isfile(output_path) and not force:
        _logger.info("this project is already recommended")
        return

    json_data = read_rename_file(root)
    result_dict = {}

    for commit in json_data["commit"].unique():
        repo_root = archive_root.joinpath(commit)
        table_path = repo_root.joinpath("exTable.csv.gz")
        if not os.path.isfile(table_path):
            continue

        table_data = ExTable(table_path)
        setAbbrDic(repo_root)
        goldSet = json_data[json_data["commit"] == commit]
        result_dict[commit] = {}
        result_dict[commit]["goldset"] = goldSet.to_dict(orient="records")
        for rec_approach in RECOMMEND_APPROACHES:
            result_dict[commit][rec_approach.get_approach_name()] = {}

        size = goldSet.shape[0]
        for gIdx in range(size):
            trigger = goldSet.iloc[gIdx]

            for rec_approach in RECOMMEND_APPROACHES:
                commit_start_time = time.time()
                _logger.info(
                    f"start recommend:{rec_approach.get_approach_name()} {commit} | {gIdx}"
                )

                result_dict[commit][rec_approach.get_approach_name()][gIdx] = (
                    rec_approach.recommend(table_data, trigger)
                )
                if rec_approach.get_approach_name() == "renas":
                    result_dict[commit]["goldset"][gIdx][
                        "operation"
                    ] = rec_approach.get_operation()
                    result_dict[commit]["goldset"][gIdx][
                        "normalized"
                    ] = rec_approach.get_old_normalized()
                    result_dict[commit]["goldset"][gIdx]["id"] = rec_approach.get_id()
                    try:
                        del result_dict[commit]["goldset"][gIdx]["type"]
                    except KeyError:
                        pass
                commit_end_time = time.time()
                _logger.info(f"end recommend: {commit} | {gIdx}")
                _logger.info(
                    f"commit elapsed time: {timedelta(seconds=(commit_end_time - commit_start_time))}"
                )
    _logger.info("export result")

    with gzip.open(output_path, "wt") as OAN:
        simplejson.dump(result_dict, OAN, indent=4, ignore_nan=True)

    endTime = time.time()
    _logger.info(f"elapsed time: {timedelta(seconds=(endTime - startTime))}")


if __name__ == "__main__":
    mainArgs = set_argument()
    _logger = set_logger(INFO)
    for repo in mainArgs.source:
        recommend(repo, mainArgs.f)
