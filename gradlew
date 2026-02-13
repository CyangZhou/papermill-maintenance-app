#!/bin/sh

APP_BASE_NAME=`basename "$0"`
APP_HOME=`dirname "$0"`

exec "$JAVA_HOME/bin/java" -classpath "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain "$@"
