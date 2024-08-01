from ast import literal_eval
from logging import DEBUG, getLogger

import pandas as pd

_logger = getLogger("util.Name")
_logger.setLevel(DEBUG)


class ExTable:
    def __init__(self, tablePath):
        self.__table = self.__load(tablePath)
        return

    def __load(self, tablePath):
        _logger.info(f"read {tablePath}")
        tableData = pd.read_csv(
            tablePath,
            converters={
                "heuristic": literal_eval,
                "expanded": literal_eval,
                "postag": literal_eval,
                "normalized": literal_eval,
            },
        ).dropna(subset=["name", "split"])
        tableData = tableData[tableData["line"] != -1]
        tableData["split"] = tableData["split"].map(lambda x: str(x).split(";"))
        tableData["delimiter"] = tableData["delimiter"].map(lambda x: str(x).split(";"))
        tableData["pattern"] = tableData["pattern"].map(
            lambda x: x if isinstance(x, list) else [x] if not pd.isna(x) else []
        )
        tableData["case"] = tableData["case"].map(lambda x: str(x).split(";"))
        self.__tableDict = (
            tableData.set_index("id", drop=False).fillna("").to_dict(orient="index")
        )
        self.__tableIds = set(tableData["id"].values.tolist())
        _logger.debug("successfully read")
        return tableData

    def selectDataByRow(self, renameRow):
        _logger.debug(f'identify {renameRow["oldname"]} from exTable')

        result = self.__table[self.__table["name"] == renameRow["oldname"]]
        _logger.debug(f"name filter: {len(result)}")

        result = result[result["line"] == renameRow["line"]]
        _logger.debug(f"line filter: {len(result)}")

        result = result[result["files"] == renameRow["files"]]
        _logger.debug(f"file filter: {len(result)}")

        if len(result) > 1:
            result = result[result["typeOfIdentifier"] == renameRow["typeOfIdentifier"]]
            _logger.debug(f"type filter: {len(result)}")

        if len(result) == 0:
            if not isinstance(renameRow, dict):
                _logger.warning(f"Not Found\n {renameRow.to_dict()}!")
            return None
        if len(result) != 1:
            _logger.warning(f'Cannot Identify DevRename {renameRow["oldname"]}\n')
            _logger.warning(f"\n{result}")
        return result.iloc[0]

    def selectDataByIds(self, ids):
        data = []
        for id in ids:
            if id in self.__tableDict:
                data.append(self.__tableDict[id])
        return data

    def selectDataByColumns(self, columns):
        return self.__table[columns]

    def selectAllData(self):
        return self.__table

    def selectDataById(self, id):
        data = self.__tableDict[id]
        return data
