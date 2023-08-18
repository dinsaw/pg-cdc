#!/bin/bash

export GRADLE_OPTS=${GRADLE_OPTS}" -Dorg.gradle.daemon=false"

./gradlew clean fatJar