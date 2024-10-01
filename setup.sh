#!/bin/sh

# RefactoringMiner
mkdir -p RefactoringMiner \
  && curl -SL -O https://github.com/tsantalis/RefactoringMiner/releases/download/2.0.2/RefactoringMiner-2.0.2.zip \
  && unzip RefactoringMiner-2.0.2.zip -d RefactoringMiner/ \
  && rm RefactoringMiner-2.0.2.zip

# RenameExpander
git clone https://github.com/salab/AbbrExpansion
