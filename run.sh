#!/bin/sh
#
#
JAVA_OPTS=$JAVA_OPTIONS
#
#
echo "JAVA_OPTS=${JAVA_OPTS}"
#
exec java ${JAVA_OPTS} org.springframework.boot.loader.JarLauncher
