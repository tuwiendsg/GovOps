#!/bin/sh

alias echo='echo -e \\t'

echo Remove old installation at $SVM_HOME
rm -r $SVM_HOME

#Remove env variable to SVM_HOME
sed -i '/SVM_HOME/d' ~/.profile    
echo export SVM_HOME= >> ~/.profile 

exit 0
