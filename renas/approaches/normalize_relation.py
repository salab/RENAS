from copy import deepcopy

from renas.approaches.internal.approach import Approach
from renas.approaches.util.extable import ExTable
from renas.approaches.util.rename import Rename


class NormalizeRelation(Approach):

    def __init__(self):
        super().__init__()

    def recommend(self, table_data: ExTable, trigger: dict):
        triggerData = table_data.selectDataByRow(trigger)
        if triggerData is None:
            return []
        triggerDataDictCopy = deepcopy(triggerData.to_dict())
        triggerRename = Rename(triggerDataDictCopy, normalize=True)
        triggerRename.setNewName(trigger["newname"])
        return self.coRenameRelation(table_data, triggerData, triggerRename)

    def coRenameRelation(self, tableData, triggerData, triggerRename):
        triedIds = {triggerData["id"]}
        nextIds = self.getRelatedIds(triggerData[self.RELATION_LIST].dropna())
        result = []
        hops = 0
        while len(nextIds) > 0:
            triedIds.update(nextIds)
            hops += 1
            relatedData = tableData.selectDataByIds(nextIds)
            relatedDataLen = len(relatedData)
            for rIdx in range(relatedDataLen):
                candidate = relatedData[rIdx]
                candidateCopy = deepcopy(candidate)
                recommended = triggerRename.coRename(candidateCopy)
                if recommended is not None:
                    result.append(recommended)
                    nextIds.update(self.getRelatedIds(candidate))
            nextIds = nextIds - triedIds
        return result

    def getRelatedIds(self, relationSeries):
        relatedIds = set()
        for i, ids in relationSeries.items():
            if ids == "" or i not in self.RELATION_LIST:
                continue
            relatedIds.update(id.rsplit(":", 1)[0] for id in ids.split(" - "))
        return relatedIds

    def get_approach_name(self):
        return "normalizeRelation"
