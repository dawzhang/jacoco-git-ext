# Jacoco-Git-Ext

## Introduction
Jacoco can provide all codes coverage, but cannot focus on every version. Jacoco-git-ext base on git diff and help you pick up the changed code that version updates.  

## Installation
#### step1:
download the jar / download the resources and build runnable jar.
#### step2:
prepare the jacoco code coverage report
#### step3:
config the necessary params and create tmp folder
#### step4:
run command: 
java -DGitURI=git@xxx/xxx.git -DGitUsername=xxx -DGitPassword=xxx  -DNewVersion=refs/remotes/origin/daily -DOldVersion=refs/heads/master -DDiffFolder=/home/it/tmp/ 
 -DJacocoReport=/home/it/codeCoverage/Check_Order_related/ -jar jacoco-gitdiff-0.0.1-release.jar
