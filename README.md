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


### Setup


1. Create a directory with the name of the project to be analyzed in the **projects** directory.
For example, if the directories to be analyzed are named "temp" or "temp2", the directory structure will be as follows:
<pre>
  Renas
  ├ projects
  │  ├ temp
  │  ├ temp2 </pre>

2. Create a directory called **repo** in the created directory and place the repository you want to analyze in it.

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


3. Start docker
4. Run the following command.
```
docker compose up -d
docker compose exec renas bash
```
5. please see "Reproduction" below.

6. Stop the tool
    - `docker compose down`


## Reproduction

The projects we used are as follows:
<details><summary>17 projects</summary>

**dataset which uses preliminary research (Section 3-E in paper)**
1. [baasbox](https://github.com/baasbox/baasbox.git)
2. [cordova-plugin-local-notifications](https://github.com/katzer/cordova-plugin-local-notifications)
3. [morphia](https://github.com/mongodb/morphia.git)
4. [spring-integration](https://github.com/spring-projects/spring-integration.git)

**Automatically identified dataset**
1. [testng](https://github.com/cbeust/testng.git)
2. [jackson-databind](https://github.com/FasterXML/jackson-databind.git)
3. [restli](https://github.com/linkedin/rest.li.git)
4. [activiti](https://github.com/Activiti/Activiti.git)
5. [thunderbird-android](https://github.com/k9mail/k-9.git)
6. [genie](https://github.com/Netflix/genie)
7. [eucalyptus](https://github.com/eucalyptus/eucalyptus)
8. [graylog2-server](https://github.com/Graylog2/graylog2-server.git)
9. [core](https://github.com/wicketstuff/core.git)
10. [gnucash-android](https://github.com/codinguser/gnucash-android)
11. [giraph](https://github.com/apache/giraph.git)

**Manually validated dataset**
1. [ratpack](https://github.com/ratpack/ratpack)
2. [argouml](https://github.com/argouml-tigris-org/argouml)

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
