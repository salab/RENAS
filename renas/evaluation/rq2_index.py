import dataclasses


@dataclasses.dataclass
class RQ2Index:
    precision_by_threshold: list[float]
    recall_by_threshold: list[float]
    fscore_by_threshold: list[float]
    map_value: float
    mrr_value: float
    top_n_recall: list[float]
