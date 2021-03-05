#!/bin/sh
#
#
JAVA_OPTS=$JAVA_OPTIONS
#
#
echo "JAVA_OPTS=${JAVA_OPTS}"
#
exec java ${JAVA_OPTS} -jar application.jar
