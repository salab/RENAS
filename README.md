# RENAS toolkit
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![DOI](https://zenodo.org/badge/DOI/10.5281/zenodo.13348547.svg)](https://doi.org/10.5281/zenodo.13348547)
[![arXiv](https://img.shields.io/badge/arXiv-2408.09716-b31b1b.svg)](https://arxiv.org/abs/2408.09716)

## Installation

### Requirements
- [Docker](https://www.docker.com/)
  - Confirmed working at Docker 24.0.2 on macOS 12.6.4 and Docker 20.10.22 on Linux (Ubuntu 22.04 LTS) 
- [Docker Compose](https://docs.docker.com/compose/) plugin v2
  - Confirmed working at v2.19.1
  - If you reproduce our result, docker can use at least **14GB** of memory. If you just run the tool, at least 4GB of memory is needed (depending on the project to apply).

- If you reproduce our result, you will need **60GB** of free disk space or more. If you just run the tool, probably just 5GB of free disk is needed (depending on the project to apply).

### Setup
1. Clone the project repository. We refer to this project directory as $RENAS.
```
$ git clone https://github.com/salab/RENAS
$ cd RENAS
```

2. Get the source code of [extended KgExpander](https://github.com/salab/AbbrExpansion) and place it under the $RENAS directory.
Note that its build will be conducted in a later process.
```
$ git clone https://github.com/salab/AbbrExpansion
```
This will result in a directory structure like this:
```
  RENAS
  ├ AbbrExpansion
  │  ├ code
  │     ├ ...
```

3. Prepare target project directories (see the sections below).

4. Start docker
```
$ docker compose up -d
```

6. Use RENAS
```
$ docker compose exec renas bash
```
You will have a shell to be ready to run RENAS.

-  If you'd like to use our tools, See [Basic Usage](#basic-usage) section below.
-  If you'd like to reproduce (part of) our result, please see [Reproduction (lightweight)](#reproduction-lightweight) or [Reproduction](#reproduction) sections below.

7. Stop the tool
```
$ docker compose down
```

## Basic Usage
This is a general explanation of how to use the RENAS tool. If you want to replicate our result, see [Reproduction](#reproduction) section below.

### Performing Recommendation

1. Create a directory with the name of the project to be analyzed in the **projects** directory.
For example, if the directories to be analyzed are named "proj1" or "proj2", the directory structure will be as follows:
```
  RENAS
  ├ projects
  │  ├ proj1
  │  ├ proj2
```

2. Create a directory called **repo** in the created directory and place the Git repository you want to analyze in it.
```
  RENAS
  ├ projects
  │  ├ proj1
  │  │  ├ repo 
  │  │    ├ foo.java
  │  │    ├ bar.java
  │  │    ├ baz
  │  │      ├ ... 
  │  │
  │  ├ proj2
  │  │  ├ repo 
  │  │    ├ qux.java
  │  │    ├ xyzzy
```

3. Enter the names of projects you'd like to recommend in "$RENAS/projects.txt", separated by lines, as shown below.
```
proj1
proj2
```

4. Create "$RENAS/projects/\*projects name\*/rename.json" and store the information on renamings.
Recommendations are made based on the renamings specified here.
If you'd like to make recommendations based on the renamings obtained from RefactoringMiner, there is no need to create it.
The way to write rename.json is as follows.
```
[
    {
        "commit":"34ee18a88a29d45ada4138e5e8f8b2e11143203c",
        "oldname":"render",
        "newname":"call",
        "typeOfIdentifier":"MethodName",
        "line":96,
        "files":"ratpack-core\/src\/main\/groovy\/org\/ratpackframework\/templating\/GroovyTemplateRenderer.java"
    },
    {
        "commit":"6872a6f3cdf4100dee5d5e902a7cdf7a199218c0",
        "oldname":"byteBuf",
        "newname":"buffer",
        "typeOfIdentifier":"ParameterName",
        "line":322,
        "files":"ratpack-core\/src\/main\/java\/ratpack\/http\/internal\/DefaultResponse.java"
    },
    {
        ...
    }
]
```
- "commit": Hash of commit.
- "oldname": identifier before renaming.
- "newname": identifier after renaming.
- "typeOfIdentifier": Type of Identifier. You can specify either of "ClassName", "FieldName", "MethodName", "ParameterName", and "VariableName".
- "line": Line where the identifier is defined.
- "files": The path from "repo" to the file where the identifier is defined.

5. Run `sh renas/execRenas`. You can obtain "projects/\*project name\*/recommend.json.gz".

## Reproduction (lightweight)

The reproduction process explained later requires a huge time (more than a week).
In case that you want to just check the process of reproduction briefly, we provide a lightweight version of the reproduction (less than 1 hour), limiting the target project only to `baasbox`.
If you want to try it, first prepare the `baasbox` repository outside the Docker environment:
```
$ mkdir -p projects/baasbox
$ git clone https://github.com/baasbox/baasbox.git projects/baasbox/repo
$ (cd projects/baasbox/repo && git reset --hard 42a265288906070f031ce9e0e24aeeac26c3a952)
```
and just run `bash evaluation-lightweight.sh` inside the Docker environment.
Then you will see a file of `projects/baasbox/recommend.json.gz` as the recommendation result.

You may see the following result (snapshot of a console) when you run `evaluation-lightwehght.sh`.
Note that the warnings in log4j may be produced when internally running RefactoringMiner, which could be ignored.
![screenshot of CUI](https://github.com/salab/RENAS/blob/main/png/refactoringMiner.png)

## Reproduction

This is the reproduction process of the results presented in our paper.
Download [the dataset](https://doi.org/10.5281/zenodo.13164183) and extract its contents (refer to as $Dataset).
Some of them will be used for the inputs in the reproduction process.

The projects we used are as follows:
<details><summary>17 projects</summary>
(...) indicates the latest commit.

**dataset which uses preliminary research (Section 3-E in paper)**
1. [baasbox](https://github.com/baasbox/baasbox.git) (42a265288906070f031ce9e0e24aeeac26c3a952)
2. [cordova-plugin-local-notifications](https://github.com/katzer/cordova-plugin-local-notifications) (eb0ac58a8a8a9b4602f9c795c285abe089d5d10f)
3. [morphia](https://github.com/mongodb/morphia.git) (cd0426c32b7c8426fbbcd4cbbfad3596246265f0)
4. [spring-integration](https://github.com/spring-projects/spring-integration.git) (6207bca3bd74cee3f37e2e9df18156a89aa90ab9)

**Automatically identified dataset**
1. [testng](https://github.com/cbeust/testng.git) (d01a4f1079e61b3f6990ba55a1ef1138266baedd)
2. [jackson-databind](https://github.com/FasterXML/jackson-databind.git) (bd9bf1b89195051a127d0a946aaf95259058c0e8)
3. [rest.li](https://github.com/linkedin/rest.li.git) (1d43edee1a9277324f75b4e90362dd6dc367ecdf)
4. [Activiti](https://github.com/Activiti/Activiti.git) (d9277212b01279079cfe71465e16398310d1c216)
5. [k-9](https://github.com/k9mail/k-9.git) (cba9ca31aa6bdb8911a2787afc145c27cf366bec)
6. [genie](https://github.com/Netflix/genie) (e0c62669f1016522ea1faaf8b1a18833c65cda0e)
7. [eucalyptus](https://github.com/eucalyptus/eucalyptus) (95e0cef57eba3da26ed798317900da4eeac44263)
8. [graylog2-server](https://github.com/Graylog2/graylog2-server.git) (80a9e8e69f0635e489b076c7dac62a7ef45c409f)
9. [core](https://github.com/wicketstuff/core.git) (49cada01fc2b71646ec36b1215d805c1c3a3b198)
10. [gnucash-android](https://github.com/codinguser/gnucash-android) (2ad44adf6dd846aabf8883d41be3719b723bf4f1)
11. [giraph](https://github.com/apache/giraph.git) (14a74297378dc1584efbb698054f0e8bff4f90bc)

**Manually validated dataset**
1. [ratpack](https://github.com/ratpack/ratpack) (29434f7ac6fd4b36a4495429b70f4c8163100332)
2. [argouml](https://github.com/argouml-tigris-org/argouml) (be952fcfa77451e594a41779db83e1a0d7221002)
</details>

1. Create directories for the above 17 projects and place each repository in the repo.
An easy way to do it is to run `bash ./clone_repository.sh`.

3. Place "manualValidation.csv" in the **ratpack** and **argouml** directories. This CSV file is located in $Dataset/projects/{ratpack, argouml}
```
$ cp $Dataset/projects/ratpack/manualValidation.csv projects/ratpack/
$ cp $Dataset/projects/argouml/manualValidation.csv projects/argouml/
```

3. Run the following commands in the order of top to bottom: preliminary study, evaluation with the automatically identified dataset, evaluation with the manually validated dataset.
```
# bash renas/preliminaryResearch.sh         # (may take ca. 2 days)
# bash renas/researchQuestion.sh            # (may take ca. 1 week)
# bash renas/researchQuestionManually.sh    # (may take ca. 2 days)
```

4. The results will be placed in the result directory.
    - "Output File" contains a description of each file.


## Inputs and Outputs of Commands

### renas/repository_analyzer.py  
By running the following command, the renames are extracted from RefactoringMiner. The relationships of source code are analyzed, and the identifiers are normalized.  
`python3 -m renas.repository_analyzer projects/**project name**`  
Input:
- repository which you'd like to analyze

Output directory:
- projects/\*project name\*/archives
- projects/\*project name\*/archives/\*commit id\*

Output file:
- projects/\*project name\*/archives/\*commit id\*/exTable.csv.gz
- projects/\*project name\*/archives/\*commit id\*/classRecord.json.gz
- projects/\*project name\*/archives/\*commit id\*/record.json.gz
- projects/\*project name\*/goldset.json.gz

The above programs mainly involve the files below.
- renas/refactoringminer.py  
    Run RefacotoringMiner
- renas/refactoring/rename_extractor.py  
    Extract rename refactorings from RefactoringMiner
- renas/relationship/analyzer.py  
Analyze the relationships of source code where rename refactoring was done and normalize identifiers.
    - AbbrExpansion/out/ParseCode-all.jar
        - parse relationships using AST (The source code is in AbbrExpansion/code/ParseCode)
    - AbbrExpansion/code/SemanticExpand/out/libs/SemanticExpand-all.jar
        - Abbreviation expansion using KgExpander (The source code is in AbbrExpansion/code/SemanticExpand)
    - renas/relationship/normalize.py
        - Remove identifier inflections

### renas/recommendation.py
By running the following command, four approaches (None, Relation, Relation + Normalize, RENAS) are recommended based on the renaming.  
`python3 -m renas.recommendation projects/**project name**`

Input:
- projects/\*project name\*/archives/\*commit id\*/exTable.csv.gz
- projects/\*project name\*/archives/\*commit id\*/classRecord.json.gz
- projects/\*project name\*/archives/\*commit id\*/record.json.gz
- projects/\*project name\*/goldset.json.gz

Output file:
- projects/\*project name\*/recommend.json.gz

The above programs primarily involve the "renas/approaches/" directory.

### renas/evaluator.py
By running the following command, recommended results obtained by four approaches (None, Relation, Relation + Normalize, RENAS) are evaluated based on co-renamings.  
`python3 -m renas.evaluator **option** projects/**project name**`   

The options are:
| option | description | Output |
| ---- | ---- | ---- |
| -pre | Preliminary study | result/preliminary |
| -rq1 | Research question 1 | result/rq1 |
| -rq2 | Research question 2 | result/rq2 |
| -manual | Evaluation with the manually validated dataset. Use with -rq1 and/or -rq2 | result/rq1_manual and/or result/rq2_manual  |
| -sim | Similarity study | result/similarity |

This script is executed by the following shell scripts:
- renas/preliminaryResearch.sh (Executing with `-sim -pre`)
- renas/researchQuestion.sh (Executing with `-rq1 -rq2`)
- renas/researchQuestionManually.sh (Executing with `-manual -rq1 -rq2`)

Input: 
- projects/\*project name\*/recommend.json.gz
- projects/\*project name\*/manualValidation.csv (if you choose `-manual`)

Output directory (option):
- result
- result/preliminary (`-pre`)
- result/rq1 (`-rq1`)
- result/rq2 (`-rq2`)
- result/rq1_manual (`-rq1 -manual`)
- result/rq2_manual (`-rq2 -manual`)
- result/similarity (`-sim`)

Output file (option):
- result/preliminary/values_by_alpha_beta.csv (`-pre`)
- result/rq1/rq1.csv (`-rq1`)
- result/rq2/ranking_evaluation.csv (`-rq2`)
- result/rq1_manual/rq1.csv (`-rq1 -manual`)
- result/rq2_manual/ranking_evaluation.csv (`-rq2 -manual`)
- result/similarity/similarity.csv (`-sim`)

The above programs primarily involve the "renas/evaluation/" directory.

## Output File Format

### result/{rq1, rq1_manual}/rq1.csv
(Generated by renas/evaluator.py)

Evaluation result of RQ1
- project name: Evaluated project name 
- approach: Approach name
- precision average  
- recall average
- fscore average

### result/{rq2, rq2_manual}/ranking_evaluation.csv
(Generated by renas/evaluator.py)

Evaluation result of RQ2
- alpha: Parameter which is used in culculating priority
- MAP: Mean Average Precision
- MRR: Mean Reciprocal Rank
- top1 Recall
- top5 Recall
- top10 Recall

### result/similarity/similarity.csv
(Generated by renas/evaluator.py)

Evaluation result of Section III E-(1)
- commit: Hash of commit
- name1 file: File where name1 is defined
- name1 line: Line where name1 is defined
- name1: Identifier after normalization
- name2 file: File where name2 is defined
- name2 line: Line where name2 is defined
- name2: Identifier after normalization
- similarity: Similarity score calculated by Dice coefficient

### result/preliminary/value_by_alpha_beta.csv
(Generated by renas/evaluator.py)

Evaluation result of Section III E-(3)
- alpha: Parameter which is used in culculating priority
- beta: Threshold of priority
- precision average
- recall average
- fscore average

### projects/\*project name\*/recommend.json.gz
(Generated by renas/recommendation.py)

Each commit has the following structure:
- goldset is the renaming database obtained from RefactoringMiner. 
- "none", "relation", "retionshipNormalize", "renas" are recommendation results for each method. 
  - "0" is the recommendation result when the 0th renaming of "goldset" is done.
```
"0b169b7d2286620eb346ddc625f97cd6ce6bb392": {
        "goldset": [
            {
                goldset_infomation
            },
            ...
        ]
        "none": {
              "0": [
                {
                  recommend_infomation   
                },
              ...]
        }
        "relation":{
              "0": [
                {
                  recommend_infomation   
                },
              ...]
        }
        "relationNormalize":{
              "0": [
                {
                  recommend_infomation   
                },
              ...]
        }    
        "renas":{
              "0": [
                {
                  recommend_infomation   
                },
              ...]
        }  
      }
```
Below is the goldset_information.
```
{
    "type": "Rename Parameter",
    "commit": "0b169b7d2286620eb346ddc625f97cd6ce6bb392",
    "oldname": "toUnfollow",
    "newname": "theFollowed",
    "typeOfIdentifier": "ParameterName",
    "line": 850,
    "files": "app/com/baasbox/controllers/Admin.java",
    "operation": [
        [
            "replace",
            [
                "to",
                "unfollow"
            ],
            [
                "the",
                "follow"
            ]
        ]
    ],
    "normalized": [
        "to",
        "unfollow"
    ],
    "id": "Lcom/baasbox/controllers/Admin;.removeFollowRelationship(Ljava/lang/String;Ljava/lang/String;)LResult;#toUnfollow#0#1"
},
```

Below is the recommend_information.
- similarity is Score_sim
- relationship is Score_rel
```
{
  "id": "Lcom/baasbox/service/storage/DocumentService;
  "files": "app/com/baasbox/service/storage/DocumentService.java",
  "line": 171,
  "name": "grantPermissionToUser",
  "typeOfIdentifier": "MethodName",
  "similarity": 0.6666666666666667,
  "relationship": 7.0,
},
```

### projects/\*project name\*/archives/\*commit id\*/exTable.csv.gz
(Generated by renas/repository_analyzer.py)

| column | description |
| ---- | ---- |
| id | Unique ID attached to the identifier |
| files| File where the identifier is defined |
| line | Line where the identifier is defined | 
| name | The identifier name | 
| typeOfIdentifier | Type of identifier | 
| subclass | Part of the relationship "parent" (a class → its subclass) |
| descendant | Part of the relationship "ancestor" (a class → the subclass of its subclass or more) |
| parent | Part of the relationship "parent" (a class → its parent class) |
| ancestor | Part of the relationship "ancestor" (a class → its ancestor class) |
| method | The relationship "method" |
| field | The relationship "field" |
| sibling-members | The relationship "sibling-members" |
| comment | comment |
| type | The relationship "type" |
| enclosingClass | The relationship "enclosingClass" |
| assignmentEquation | The relationship "assignmentEquation" |
| pass | The relationship "pass" |
| argumentToParameter | Part of the relationship "argument" (argument of a method → parameter of the method) |
| parameter | The relationship "parameter" |
| enclosingMethod | The relationship "enclosingMethod" |
| parameterToArgument | Part of the relationship "argument" (parameter of a method → argument of the method) |
| split | Identifier after splitting |
| delimiter | Delimiter character |
| case | Case of words |
| pattern | Naming pattern of identifier |
| heuristic | Abbreviated forms |
| expanded | Identifier after expanding abbreviations |
| postag | POS tag for each word |
| normalized |　Normalized identifier |
| parameterOverload | The relationship "parameterOverload"|

### projects/\*project name\*/archives/\*commit id\*/classRecord.json.gz
(Generated by renas/repository_analyzer.py)

A file that records the expanded abbreviations for each file. 
For example, if "buf" is expanded to buffer four times in temp.java, it shows below.
```
{
"temp.java": 
    {
    "buf==buffer":4
    }
}
```

### projects/\*project name\*/archives/\*commit id\*/record.json.gz
(Generated by renas/repository_analyzer.py)

A file that records the abbreviations expanded within the project.

## Related Publications
If you use or mention this tool in a scientific publication, we would appreciate citations to the following paper:

Naoki Doi, Yuki Osumi, and Shinpei Hayashi, "RENAS: Prioritizing Co-Renaming Opportunities of Identifiers," in Proceedings of the 40th IEEE International Conference on Software Maintenance and Evolution (ICSME 2024), pp. TBD, Arizona, United States, 2024, doi: TBD. Preprint: http://arxiv.org/abs/2408.09716

```
@inproceedings{doi-icsme2024,
  author = {Naoki Doi and Yuki Osumi and Shinpei Hayashi},
  title = {{RENAS}: Prioritizing Co-Renaming Opportunities of Identifiers}, 
  booktitle = {Proceedings of the 40th IEEE International Conference on Software Maintenance and Evolution (ICSME 2024)},
  pages = {TBD},
  doi = {TBD},
  year = {2024},
}
```
