# Jacoco-Git-Ext

## Introduction
Jacoco can provide all codes coverage, but cannot focus on every version. Jacoco-git-ext base on git diff and help you pick up the changed codes every version.  

## Installation
#### step1:
download the jar or download the resources and build runnable jar.
#### step2:
prepare the jacoco code coverage report
#### step3:
config the necessary params and make dir
#### step4:
java -DGitURI=git@xxx/xxx.git -DNewVersion=refs/remotes/origin/daily -DOldVersion=refs/heads/master -DDiffFolder=/home/it/tmp/ 
 -DJacocoReport=/home/it/codeCoverage/Check_Order_related/ -jar jacoco-gitdiff-0.0.1-release.jar
