#!/bin/sh

alias echo='echo -e \\t'

SVM_HOME="/data/G2021/svm"
mkdir -p $SVM_HOME
cp sedona-vm/artifacts/svm $SVM_HOME/svm

chmod +x -R $SVM_HOME/svm

if [ $? -ne 0 ]
then
  echo Error installing sedona-vm!
  exit 1
fi

sed -i '/SVM_HOME/d' ~/.profile
echo export SVM_HOME=$SVM_HOME >> ~/.profile

exit 0
