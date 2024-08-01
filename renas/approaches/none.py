from copy import deepcopy

from renas.approaches.internal.approach import Approach
from renas.approaches.util.rename import Rename


class NoneApproach(Approach):

    def __init__(self):
        super().__init__()

    def recommend(self, tableData, trigger):
        triggerData = tableData.selectDataByRow(trigger)
        if triggerData is None:
            return []
        triggerDataDictCopy = deepcopy(triggerData[self.IDENTIFIER_LIST].to_dict())
        triggerRename = Rename(triggerDataDictCopy, normalize=False)
        triggerRename.setNewName(trigger["newname"])
        return self.coRenameNone(tableData, triggerRename)

    def coRenameNone(self, tableData, triggerRename):
        tableDict = tableData.selectDataByColumns(self.IDENTIFIER_LIST).to_dict(
            orient="records"
        )
        recommends = [triggerRename.coRename(deepcopy(d)) for d in tableDict]
        result = [r for r in recommends if r is not None]
        return result

    def get_approach_name(self):
        return "none"
