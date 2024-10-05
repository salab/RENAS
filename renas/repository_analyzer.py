import argparse
import os
import pathlib

import pandas as pd

from renas.refactoring import refactoringminer, rename_extractor
from renas.relationship import analyzer


def set_argument():
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "source", help="set directory containing repositories to be analyzed"
    )
    parser.add_argument(
        "-threshold",
        help="use commit which has more than specifying renames",
        action="store",
        default=0,
    )
    parser.add_argument(
        "-f",
        help="Even if rename.json exists, re-execute RefactoringMiner",
        action="store_true",
        default=False,
    )
    args = parser.parse_args()
    return args


def main(root, args):
    rename_path = os.path.join(root, "rename.json")
    if not os.path.isfile(rename_path) or args.f:
        refactoring_dict = refactoringminer.main(root)
        refactoring_data = pd.DataFrame.from_records(refactoring_dict["commits"])
        rename_data = rename_extractor.main(root, refactoring_data)
        dump(root, rename_data)
    else:
        rename_data = pd.read_json(rename_path, orient="records")
    analyzer.main(root, rename_data, int(args.threshold))


def dump(root, data: pd.DataFrame):
    out_file_path = os.path.join(root, "rename.json")
    data.to_json(out_file_path, orient="records", indent=4)


if __name__ == "__main__":
    args = set_argument()
    root = pathlib.Path(args.source)
    main(root, args)
