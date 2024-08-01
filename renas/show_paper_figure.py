import argparse
import csv
import os
import pathlib

import matplotlib.pyplot as plt
import numpy as np


def setArgument():
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "source", help="set directory containing repositories to be analyzed"
    )
    parser.add_argument(
        "-sim", help="research similarity", action="store_true", default=False
    )
    parser.add_argument(
        "-pre",
        help="preliminary survey (determine threshold and ratio)",
        action="store_true",
        default=False,
    )
    parser.add_argument(
        "-rq1", help="research question 1", action="store_true", default=False
    )
    parser.add_argument(
        "-rq2", help="research question 2", action="store_true", default=False
    )
    parser.add_argument(
        "-manual", help="use manual dataset", action="store_true", default=False
    )
    parser.add_argument(
        "-count",
        help="count success recommendation in direct relationship and indirect relationship",
        action="store_true",
        default=False,
    )

    args = parser.parse_args()
    return args


def show_rank_figure(dir_path, values, figure_name):
    alphas = [str(i / 20) for i in range(20 + 1)]
    fig, ax = plt.subplots()
    values = np.array(values)
    plt.plot(list(map(float, alphas)), values)
    plt.xlabel("α")
    plt.ylabel("MAP")
    maxIndex = np.argmax(values)
    maxValue = values[maxIndex]
    plt.plot(maxIndex / (len(alphas) - 1), maxValue, ".", markersize=7, color="r")
    fig.savefig(os.path.join(dir_path, f"{figure_name}.svg"))
    plt.close(fig)


def preliminary_graph_beta(dir_path, values_dir, figure_name):
    for alpha, values in values_dir.items():
        threshold_list = [i / 100 for i in range(100 + 1)]
        fig, ax = plt.subplots()
        plt.plot(threshold_list, values)
        plt.xlabel("threshold")
        plt.ylabel(f"{figure_name}-alpha={alpha}")
        maxIndex = np.argmax(values)
        maxValue = values[maxIndex]
        plt.plot(
            maxIndex / 100,
            maxValue,
            ".",
            markersize=7,
            color="r",
        )
        fig.savefig(os.path.join(dir_path, f"{figure_name}-alpha{alpha}.svg"))
        plt.close(fig)


def preliminary_graph_alpha(dir_path, values, figure_name):
    alphas = [str(i / 20) for i in range(20 + 1)]
    values = np.array(values)
    fig, ax = plt.subplots()
    plt.plot(alphas, values)
    plt.xlabel("α")
    plt.ylabel(f"{figure_name}")
    maxIndex = np.argmax(values)
    maxValue = values[maxIndex]
    plt.plot(
        maxIndex / 100,
        maxValue,
        ".",
        markersize=7,
        color="r",
    )
    fig.savefig(os.path.join(dir_path, f"{figure_name}_max.svg"))
    plt.close(fig)


args = setArgument()
dir_path = args.source
precision_dir = {}
recall_dir = {}
fscore_dir = {}
rank_eval_dir = {}
beta_path = os.path.join(dir_path, "preliminary", "values_by_beta.csv")
rank_eval_path = os.path.join(dir_path, "rq2", "ranking_evaluation.csv")
if os.path.isfile(beta_path):
    with open(beta_path, "r") as f:
        reader = csv.reader(f)
        rows = [row for row in reader]
    for row in rows[1:]:
        if row[0] not in precision_dir:
            precision_dir[row[0]] = []
            recall_dir[row[0]] = []
            fscore_dir[row[0]] = []
        precision_dir[row[0]].append(row[1])
        recall_dir[row[0]].append(row[2])
        fscore_dir[row[0]].append(row[3])
    max_precision = []
    max_recall = []
    max_fscore = []
    for alpha in precision_dir.keys():
        max_precision.append(max(precision_dir[alpha]))
        max_recall.append(max(recall_dir[alpha]))
        max_fscore.append(max(fscore_dir[alpha]))

    preliminary_graph_beta(
        os.path.join(dir_path, "preliminary"), precision_dir, "precision"
    )
    preliminary_graph_beta(os.path.join(dir_path, "preliminary"), recall_dir, "recall")
    preliminary_graph_beta(os.path.join(dir_path, "preliminary"), fscore_dir, "fscore")
    preliminary_graph_alpha(
        os.path.join(dir_path, "preliminary"), max_precision, "precision"
    )
    preliminary_graph_alpha(os.path.join(dir_path, "preliminary"), max_recall, "recall")
    preliminary_graph_alpha(os.path.join(dir_path, "preliminary"), max_fscore, "fscore")

if os.path.isfile(rank_eval_path):
    with open(rank_eval_path, "r") as f:
        reader = csv.reader(f)
        rows = [row for row in reader]
    map_v = []
    mrr_v = []
    top1 = []
    top5 = []
    top10 = []
    for row in rows[1:]:
        map_v.append(float(row[1]))
        mrr_v.append(float(row[2]))
        top1.append(float(row[3]))
        top5.append(float(row[4]))
        top10.append(float(row[5]))
    show_rank_figure(os.path.join(dir_path, "rq2"), map_v, "MAP")
    show_rank_figure(os.path.join(dir_path, "rq2"), mrr_v, "MRR")
    show_rank_figure(os.path.join(dir_path, "rq2"), top1, "top1")
    show_rank_figure(os.path.join(dir_path, "rq2"), top5, "top5")
    show_rank_figure(os.path.join(dir_path, "rq2"), top10, "top10")
