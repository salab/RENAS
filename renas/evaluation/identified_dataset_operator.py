from renas.evaluation.dataset_operator import DatasetOperator


class IdentifiedDatasetOperator(DatasetOperator):
    def __init__(self):
        self.count = 0

    def print_count(self, num=1):
        print(f"The number of corenamings = {int(self.count/num)}")

    def set_corename_list(self, goldset):
        corename_sets = {}
        for ginfo in goldset:
            operations = ginfo["operation"]
            if not self.__is_meaningful(operations):
                continue
            for op in operations:
                key = str(op)
                if key not in corename_sets:
                    corename_sets[key] = []
                corename_sets[key].append(ginfo)
        self.corename = corename_sets

        for cos in corename_sets.values():
            ll = set(map(lambda x: x["id"], cos))
            if len(ll) >= 2:
                self.count += 1

    def __is_meaningful(self, operations):
        meaningful = ["insert", "delete", "replace"]
        for op in operations:
            if op[0] in meaningful:
                return True
            if op[0] == "format" and op[1][0] == "Plural":
                return True
        return False

    def get_correct_ids(self, ginfo):
        ids = set()
        if not self.__is_meaningful(ginfo["operation"]):
            return ids
        for op in ginfo["operation"]:
            for g in self.corename[str(op)]:
                ids.add(g["id"])
        ids.remove(ginfo["id"])
        return ids

    def get_corename(self):
        return self.corename
