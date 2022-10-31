#!/usr/bin/env bash
mvn -Pnative -DskipTests  clean native:compile && target/gateway
#mvn -Pnative -DskipTests -U clean native:compile && target/gateway