import argparse
import os

import pandas as pd

from renas.refactoring.parameter_extractor import parameter_extractor


def set_argument():
    parser = argparse.ArgumentParser()
    parser.add_argument("source", help="set directory containing result.json")
    args = parser.parse_args()
    return args


def convert_rename_type(rt):
    if rt == "Class":
        return "ClassName"
    elif rt == "Method":
        return "MethodName"
    elif rt == "Attribute":
        return "FieldName"
    elif rt == "Parameter":
        return "ParameterName"
    elif rt == "Variable":
        return "VariableName"


def add_information(commitId, units):
    extractor = parameter_extractor()
    for refactoring in units:
        renaming = extractor.extract_rename(refactoring)
        location = extractor.extract_location(refactoring)
        rename_type = extractor.extract_type(refactoring)
        refactoring["commit"] = commitId
        refactoring["oldname"] = renaming["old"].split(" ")[-1]
        refactoring["newname"] = renaming["new"].split(" ")[-1]
        refactoring["typeOfIdentifier"] = convert_rename_type(rename_type)
        refactoring["location"] = location
        refactoring["line"] = refactoring["leftSideLocations"][0]["startLine"]
        refactoring["files"] = location["old"]
    return


def delete_infomation(units):
    for refactoring in units:
        try:
            del (
                refactoring["leftSideLocations"],
                refactoring["rightSideLocations"],
                refactoring["description"],
                refactoring["location"],
            )
        except KeyError:
            pass
    return


def flatten(data):
    new_data = []
    [new_data.extend(rename_list) for rename_list in data["refactorings"]]
    return pd.DataFrame(new_data)


def read_refactoring_file(root):
    inFilePath = os.path.join(root, "result.json")
    data = pd.read_json(inFilePath, encoding="UTF-8")
    return data


def extract_rename_data(data: pd.DataFrame):
    data = data.rename(columns={"sha1": "commitId"})
    data["refactorings"] = data["refactorings"].map(
        lambda x: [r for r in x if "Rename" in r["type"]]
    )
    data["count"] = data["refactorings"].map(len)
    data = data[data["count"] > 0].reset_index(drop=True)
    return data


def main(root, data: pd.DataFrame):
    rename_data = extract_rename_data(data)
    for commit_id, units in zip(rename_data["commitId"], rename_data["refactorings"]):
        add_information(commit_id, units)
        delete_infomation(units)
    rename_data = flatten(rename_data)
    return rename_data


if __name__ == "__main__":
    args = set_argument()
    root = args.source
    data = read_refactoring_file(root)
    rename_data = main(root, data)
    out_file_path = os.path.join(root, "rename.json.gz")
    rename_data.to_json(out_file_path, orient="records", indent=4, compression="gzip")
