import dataclasses


@dataclasses.dataclass
class RQ1Index:
    precision: float
    recall: float
    fscore: float
    true_count: int
