import argparse
import json
import os
import pathlib
import re
import subprocess
from logging import INFO, Formatter, StreamHandler, getLogger

import simplejson

GIT_REGULAR = re.compile(
    r"(?:^commit)\s+(.+)\nAuthor:\s+(.+)\nDate:\s+(.+)\n", re.MULTILINE
)
MINERPASS = "RefactoringMiner/RefactoringMiner-2.0.2/bin/RefactoringMiner"
_logger = getLogger(__name__)


def set_argument():
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "source", help="set directory containing repositories to be analyzed"
    )
    args = parser.parse_args()
    return args


def set_logger(level):
    _logger.setLevel(level)
    root_logger = getLogger()
    handler = StreamHandler()
    handler.setLevel(level)
    formatter = Formatter("[%(asctime)s] %(name)s -- %(levelname)s : %(message)s")
    handler.setFormatter(formatter)
    root_logger.addHandler(handler)
    root_logger.setLevel(INFO)
    return root_logger


def set_gitlog(path):
    repo_path = os.path.join(path, "repo")
    commits = set()
    cp = subprocess.run(f"cd {repo_path}; git log", shell=True, stdout=subprocess.PIPE)
    git_log = cp.stdout.decode("utf-8", "ignore")
    git_info = GIT_REGULAR.findall(git_log)
    for info in git_info:
        commit = info[0]
        commits.add(commit)
    return commits


def do_refactoringminer(commit, repo_path):
    cp = subprocess.run(
        f"{MINERPASS} -c {repo_path} {commit}", shell=True, stdout=subprocess.PIPE
    )
    if cp.returncode != 0:
        return
    refactoring = cp.stdout.decode("utf-8", "ignore")
    refactoring_json = json.loads(refactoring)
    if "commits" not in refactoring_json:
        print(f"Warn: {commit} \n refactoringMiner is something wrong")
        print(refactoring_json)
        return
    return refactoring_json["commits"]


def set_repository_path(root):
    repo_path = root.joinpath("repo")
    if not os.path.exists(repo_path):
        _logger.error("error: repo is not existed")
        exit(1)
    return repo_path


def main(root):
    set_logger(INFO)
    commits = set_gitlog(root)
    commit_length = len(commits)
    repo_path = set_repository_path(root)
    refactoring_dict = {"commits": []}
    count = 0
    for c in commits:
        count += 1
        _logger.info(f"{count} / {int(commit_length)}")
        _logger.info(f"start RefactoringMiner: commit={c}")
        refactoring = do_refactoringminer(c, repo_path)
        refactoring_dict["commits"] += refactoring
        _logger.info(f"end RefactoringMiner: commit={c}")
    _logger.info("finish merging refactoring")
    return refactoring_dict


if __name__ == "__main__":
    args = set_argument()
    root = pathlib.Path(args.source)
    refactoring_dict = main(root)
    with open(root.joinpath("result.json"), "w") as rp:
        simplejson.dump(refactoring_dict, rp, indent=4, ignore_nan=True)
