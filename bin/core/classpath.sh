#!/bin/sh
for DIR in $*; do
    if [ "x`ls $DIR`" != "x" ]; then
        for JAR in `ls $DIR/*.jar`; do
            if [ "x$CLASSPATH" = "x" ]; then
                CLASSPATH=$JAR
            else
                CLASSPATH=$CLASSPATH:$JAR
            fi
        done
    fi
done

echo $CLASSPATH
