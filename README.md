# RENAS

## Installation

### Requirements

- Any environment
  - Confirmed working at macOS (ProductVersion is 12.6.4)
- docker
  - Confirmed working at 24.0.2
- docker compose plugin v2
  - Confirmed working at v2.19.1
  - Docker can use at least **14GB** of memory
- if you reproduce our result, you need **60GB** of free disk space at least


### Setup

1. Get the source code for relationship analysis from the following URL and place it under Renas.
https://github.com/salab/AbbrExpansion  
This will result in a directory structure like this:
<pre>
  Renas
  ├ AbbrExpansion
  │  ├ code
  │     ├ ... </pre>

2. Create a directory with the name of the project to be analyzed in the **projects** directory.
For example, if the directories to be analyzed are named "temp" or "temp2", the directory structure will be as follows:
<pre>
  Renas
  ├ projects
  │  ├ temp
  │  ├ temp2 </pre>

3. Create a directory called **repo** in the created directory and place the repository you want to analyze in it.

<pre>
  renas
  ├ projects
  │  ├ temp
  │  │  ├ repo 
  │  │    ├ hoge.java
  │  │    ├ foo.java
  │  │    ├ huga
  │  │      ├ ... 
  │  │
  │  ├ temp2 
  │  │  ├ repo 
  │  │    ├ far.java
  │  │    ├ fee</pre>


4. Start docker
5. Run the following command.
```
docker compose up -d
docker compose exec renas bash
```
6. Use our tool
    -  if you'd like to reproduce our result, please see "Reproduction" below.
    -  if you'd like to use our tools, See "Usage" below.

7. Stop the tool
    - `docker compose down`


## Reproduction

The projects we used are as follows:
<details><summary>17 projects</summary>

() indicates the latest commit.

**dataset which uses preliminary research (Section 3-E in paper)**
1. [baasbox](https://github.com/baasbox/baasbox.git) (42a265288906070f031ce9e0e24aeeac26c3a952)
2. [cordova-plugin-local-notifications](https://github.com/katzer/cordova-plugin-local-notifications) (eb0ac58a8a8a9b4602f9c795c285abe089d5d10f)
3. [morphia](https://github.com/mongodb/morphia.git) (cd0426c32b7c8426fbbcd4cbbfad3596246265f0)
4. [spring-integration](https://github.com/spring-projects/spring-integration.git) (6207bca3bd74cee3f37e2e9df18156a89aa90ab9)

**Automatically identified dataset**
1. [testng](https://github.com/cbeust/testng.git) (d01a4f1079e61b3f6990ba55a1ef1138266baedd)
2. [jackson-databind](https://github.com/FasterXML/jackson-databind.git) (bd9bf1b89195051a127d0a946aaf95259058c0e8)
3. [restli](https://github.com/linkedin/rest.li.git) (1d43edee1a9277324f75b4e90362dd6dc367ecdf)
4. [activiti](https://github.com/Activiti/Activiti.git) (d9277212b01279079cfe71465e16398310d1c216)
5. [thunderbird-android](https://github.com/k9mail/k-9.git)     (cba9ca31aa6bdb8911a2787afc145c27cf366bec)
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


1. As shown in "Setup", you create directories for the above 17 projects and place each repository in the repo.

2. Place manualValidation.csv in the **ratpack** and **argouml** directories. This CSV file is located in Dataset/projects/{ratpack, argouml}

3. Run the following commands in order (from top to bottom: preliminary investigation, evaluation with the automatically identified dataset, evaluation with the manually validated dataset).
```
bash renas/preliminaryResearch.sh
bash renas/researchQuestion.sh
bash renas/researchQuestionManually.sh
```

4. The results are placed in the result directory.
    - "Output File" contains a description of each file.



## Usage

### Perform Recommendation
1. Enter the names of projects you'd like to recommend in projects.txt, separated by lines, as shown below.
```
temp
temp2
temp3
```

2. Create "projects/\*\*projects name\*\*/rename.json" and write the renames.  
Recommendations are made based on the renames specified here. If you'd like to make recommendations based on the renames obtained from RefactoringMiner, there is no need to create it.  
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
- "commit"  
    Hash of commit
- "oldname"  
    identifier before renaming
- "newname"
    identifier after renaming
- "typeOfIdentifier"
    Type of Identifier  
    you can specify "ClassName", "FieldName", "MethodName", "ParameterName" and "VariableName"
- "line"
    Line where the identifier is defined
- "files"
    The path from "repo" to the file where the identifier is defined


3. Run `sh renas/execRenas`. you can get "projects/\*\*project name\*\*/recommend.json.gz".

### Evaluation
Create co-renamed sets from "projects/\*\*projects name\*\*/rename.json" and evaluate.
1. Run `python3 -m renas.evaluator **option** projects.txt`.  
The available options are:

| option | description |
| ---- | ---- |
| -pre | preliminary research. output is result/preliminary |
| -rq1 | research question 1. output is result/rq1|
| -rq2 | research question 2. output is result/rq2 |
| -sim | research similarity. output is result/similarity |

For example、if you'd like to research similarity and RQ1, you execution below command.  
`python3 -m renas.evalutor -sim -rq1 projects.txt`


#### similarity
Investigate the similarity of identifiers that are thought to have been co-renamed. (Section 3-E (1) in our paper)

#### preliminary research
Investigate the performance of the proposed approach for each parameter (α) and threshold (β). (Section 3-E (3) in our paper)

#### RQ1  
RQ1: How well does the proposed approach perform?   
Evaluate the performance of our approach (RENAS) and other approaches (None, Relation, Relation + Normalize).
The evaluation metrics are Precision, Recall, and F1-measure.

#### RQ2
RQ2: How does the performance vary depending on how to prioritize?  
Evaluate whether priorities should be used, by taking into account both relationship and similarity.
Evaluation metrics are MAP (Mean Average Precision), MRR (Mean Reciprocal Rank), and top-{1, 5, 10} Recall.


## Source Files

### renas/repository_analyzer.py  
By running the following command, the renames are extracted from RefactoringMiner. The relationships of source code are analyzed, and the identifiers are normalized.  
`python3 -m renas.repository_analyzer projects/**project name**`  
Input:
- repository which you'd like to analyze

Output:
- projects/\*\*project name\*\*/archives/\*\*commit id\*\*/exTable.csv.gz
- projects/\*\*project name\*\*/archives/\*\*commit id\*\*/classRecord.json.gz
- projects/\*\*project name\*\*/archives/\*\*commit id\*\*/record.json.gz
- projects/\*\*project name\*\*/goldset.json.gz

The above programs mainly involve the below files.
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
- projects/\*\*project name\*\*/archives/\*\*commit id\*\*/exTable.csv.gz
- projects/\*\*project name\*\*/archives/\*\*commit id\*\*/classRecord.json.gz
- projects/\*\*project name\*\*/archives/\*\*commit id\*\*/record.json.gz
- projects/\*\*project name\*\*/goldset.json.gz

Output:
- projects/\*\*project name\*\*/recommend.json.gz

The above programs primarily involve the "renas/approaches/" directory.



## Output File

### result/{rq1, rq1_manual}/rq1.csv
Evaluation result of RQ1
- project name  
Evaluated project name 
- approach  
Approach name
- precision average  
- recall average
- fscore average
### result/{rq2, rq2_manual}/ranking_evaluation.csv
Evaluation result of RQ2
- alpha  
Parameter which is used in culculating priority
- MAP  
Mean Average Precision
- MRR  
Mean Reciprocal Rank
- top1 Recall
- top5 Recall
- top10 Recall

### result/similarity/similarity.csv
Evaluation result of Section E-(1)
- commit  
Hash of commit
- name1 file  
File where name1 is defined
- name1 line  
Line where name1 is defined
- name1   
Identifier after normalization
- name2 file  
File where name2 is defined
- name2 line  
Line where name2 is defined
- name2  
Identifier after normalization
- similarity  
Similarity score calculated by Dice coefficient


### result/preliminary/value_by_alpha_beta.csv
Evaluation result of Section E-(3)
- alpha  
Parameter which is used in culculating priority
- beta  
Threshold of priority
- precision average
- recall average
- fscore average


### projects/\*project name\*/recommend.json.gz

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

| column | description |
| ---- | ---- |
| id | Unique ID attached to the identifier |
|files| File where the identifier is defined |
|line| Line where the identifier is defined  | 
|name| The identifier name | 
|typeOfIdentifier| Type of identifier | 
|subclass| Part of the relationship "parent" (an class → its subclass) |
|descendant| Part of the relationship "ancestor" (an class → the subclass of its subclass or more) |
|parent| Part of the relationship "parent"(an class → its parent class) |
|ancestor| Part of the relationship "ancestor" (an class → its ancestor class) |
|method| The relationship "method" |
|field| The relationship "field" |
|sibling-members| The relationship "sibling-members" |
|comment| comment |
|type| The relationship "type" |
|enclosingClass| The relationship "enclosingClass" |
|assignmentEquation| The relationship "assignmentEquation" |
|pass| The relationship "pass" |
|argumentToParameter| Part of the relationship "argument"(argument of a method → parameter of the method)
|parameter| The relationship "parameter" |
|enclosingMethod| The relationship "enclosingMethod" |
|parameterToArgument| Part of the relationship "argument"(parameter of a method → argument of the method) |
|split| Identifier after splitting
|delimiter| Delimiter character
|case| Case of words
|pattern| Naming pattern of identifier
|heuristic| Abbreviated forms
|expanded| Identifier after expanding abbreviations
|postag| POStag for each word
|normalized|　Normalized identifier |
|parameterOverload| The relationship "parameterOverload"|

### projects/**project name**/archives/**commit id**/classRecord.json.gz
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

### projects/**project name**/archives/**commit id**/record.json.gz
A file that records the abbreviations expanded within the project.

## Related Publications

If you use or mention this tool in a scientific publication, we would appreciate citations to the following paper:

Naoki Doi, Yuki Osumi and Shinpei Hayashi, "RENAS: Prioritizing Co-Renaming Opportunities of Identifiers," in Proceedings of the 40th IEEE International Conference on Software Maintenance and Evolution (ICSME 2024), pp. TBD, Arizona, United States, 2024, doi: TBD.
Preprint:<span style="color:red"> <strong>TODO: input arxiv URL</strong> </span>

```
@inproceedings{doi-icsme2024,
  author = {Naoki Doi, Yuki Osumi and Shinpei Hayashi},
  booktitle = {Proceedings of the 40th IEEE International Conference on Software Maintenance and Evolution (ICSME 2024)},
  title = {RENAS: Prioritizing Co-Renaming Opportunities of Identifiers}, 
  year = {2024},
  pages = {TBD},
  doi = {TBD}
}
```
