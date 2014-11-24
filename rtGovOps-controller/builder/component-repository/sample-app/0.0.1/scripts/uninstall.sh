#!/bin/sh

alias echo='echo -e \\t'

echo Remove old installation at $JAVA_SAMPLE_APP_HOME
rm -r $JAVA_SAMPLE_APP_HOME

exit 0
