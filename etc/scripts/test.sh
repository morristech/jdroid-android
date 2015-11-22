#!/bin/sh

# Note the set -ev at the top. The -e flag causes the script to exit as soon as one command
# returns a non-zero exit code. The -v flag makes the shell print all lines in the script
# before executing them, which helps identify which steps failed.
set -e

UPLOAD=$1
ENABLE_JAVA_WEB=$2
ENABLE_ANDROID=$3
DEBUG=$4

if [ -z "$ENABLE_JAVA_WEB" ]
then
	ENABLE_JAVA_WEB="true"
fi

if [ -z "$ENABLE_ANDROID" ]
then
	ENABLE_ANDROID="true"
fi

if [ -z "$UPLOAD" ]
then
	UPLOAD="false"
fi

# ************************
# jdroid gradle plugin
# ************************

./gradlew :jdroid-java:clean :jdroid-java:uploadArchives :jdroid-gradle-plugin:clean :jdroid-gradle-plugin:uploadArchives --configure-on-demand -PLOCAL_UPLOAD=true

# ************************
# jdroid javaweb sample
# ************************

cmd="./gradlew clean"

if [ "$ENABLE_JAVA_WEB" = "true" ]
then
	cmd="${cmd} :jdroid-java:build :jdroid-java:test"
	cmd="${cmd} :jdroid-java-http-okhttp:build :jdroid-java-http-okhttp:test"
	cmd="${cmd} :jdroid-javaweb:build :jdroid-javaweb:test"
	cmd="${cmd} :jdroid-javaweb-sample:build"
fi

# ************************
# jdroid android sample
# ************************

if [ "$ENABLE_ANDROID" = "true" ]
then
	cmd="${cmd} :jdroid-android:assemble :jdroid-android:testDebug :jdroid-android:lintDebug"
	cmd="${cmd} :jdroid-android-about:assemble :jdroid-android-about:testDebug :jdroid-android-about:lintDebug"
	cmd="${cmd} :jdroid-android-crashlytics:assemble :jdroid-android-crashlytics:testDebug :jdroid-android-crashlytics:lintDebug"
	cmd="${cmd} :jdroid-android-facebook:assemble :jdroid-android-facebook:testDebug :jdroid-android-facebook:lintDebug"
	cmd="${cmd} :jdroid-android-google-admob:assemble :jdroid-android-google-admob:testDebug :jdroid-android-google-admob:lintDebug"
	cmd="${cmd} :jdroid-android-google-gcm:assemble :jdroid-android-google-gcm:testDebug :jdroid-android-google-gcm:lintDebug"
	cmd="${cmd} :jdroid-android-google-maps:assemble :jdroid-android-google-maps:testDebug :jdroid-android-google-maps:lintDebug"
	cmd="${cmd} :jdroid-android-google-plus:assemble :jdroid-android-google-plus:testDebug :jdroid-android-google-plus:lintDebug"
	cmd="${cmd} :jdroid-android-sample:assemble :jdroid-android-sample:testDebug :jdroid-android-sample:lintDebug :jdroid-android-sample:countMethodsSummary"
fi

# ************************
# Upload Snapshot to Maven repository
# ************************

if [ "$UPLOAD" = "true" ]
then

	cmd="${cmd} :jdroid-gradle-plugin:uploadArchives"

	if [ "$ENABLE_JAVA_WEB" = "true" ]
	then
		cmd="${cmd} :jdroid-java:uploadArchives"
		cmd="${cmd} :jdroid-java-http-okhttp:uploadArchives"
		cmd="${cmd} :jdroid-javaweb:uploadArchives"
	fi

	if [ "$ENABLE_ANDROID" = "true" ]
	then
		cmd="${cmd} :jdroid-android:uploadArchives"
		cmd="${cmd} :jdroid-android-about:uploadArchives"
		cmd="${cmd} :jdroid-android-crashlytics:uploadArchives"
		cmd="${cmd} :jdroid-android-facebook:uploadArchives"
		cmd="${cmd} :jdroid-android-google-admob:uploadArchives"
		cmd="${cmd} :jdroid-android-google-gcm:uploadArchives"
		cmd="${cmd} :jdroid-android-google-maps:uploadArchives"
		cmd="${cmd} :jdroid-android-google-plus:uploadArchives"
	fi
fi

cmd="${cmd} --configure-on-demand"

if [ "$DEBUG" = "true" ]
then
	cmd="${cmd} --debug"
fi

echo "Executing the following command"
echo "${cmd}"

eval "${cmd}"


