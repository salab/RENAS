import heapq
from copy import deepcopy

from renas.approaches.internal.approach import Approach
from renas.approaches.util.extable import ExTable
from renas.approaches.util.rename import Rename


class Renas(Approach):

    def __init__(self):
        super().__init__()

    def get_approach_name(self):
        return "renas"

    def recommend(self, table_data: ExTable, trigger: dict):
        trigger_data = table_data.selectDataByRow(trigger)
        if trigger_data is None:
            return []
        trigger_data_dict_copy = deepcopy(trigger_data.to_dict())
        trigger_rename = Rename(trigger_data_dict_copy, normalize=True)
        self.rename = trigger_rename
        trigger_rename.setNewName(trigger["newname"])
        return self.co_rename(table_data, trigger_data["id"], trigger_rename)

    def co_rename(self, tableData, trigger_id, triggerRename):
        triedIds = set()
        triggerScore = 0
        startHop = 0
        nextIds = []
        heapq.heappush(nextIds, [triggerScore, startHop, trigger_id])
        result = []
        trueRecommend = 0

        while len(nextIds) > 0:
            score, hop, searchId = heapq.heappop(nextIds)
            if searchId in triedIds:
                continue
            triedIds.add(searchId)

            searchData = tableData.selectDataById(searchId)
            nextScore = score
            searchDataCopy = deepcopy(searchData)
            if searchDataCopy["id"] != trigger_id:
                recommended = triggerRename.coRename(searchDataCopy)
                if recommended is not None:
                    recommended["relationship"] = nextScore
                    recommended["hop"] = hop
                    result.append(recommended)
                    trueRecommend += 1

            candidateIds, idCost = self.get_related_ids_and_cost(searchData)
            candidateIds = candidateIds - triedIds
            candidateData = tableData.selectDataByIds(candidateIds)
            candidateLen = len(candidateData)

            for ci in range(candidateLen):
                candidate = candidateData[ci]
                distanceCost = idCost[candidate["id"]]
                heapq.heappush(
                    nextIds, [nextScore + distanceCost, hop + 1, candidate["id"]]
                )

        return result

    def get_related_ids_and_cost(self, relationSeries):
        relatedIds = set()
        idToCost = {}
        for i, ids in relationSeries.items():
            if ids == "" or i not in self.RELATION_LIST:
                continue
            idList = {id.rsplit(":", 1)[0] for id in ids.split(" - ")}
            relatedIds.update(idList)
            idToCost.update([(id, self.RELATION_COST[i]) for id in idList])
        return relatedIds, idToCost
