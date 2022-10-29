#!/usr/bin/env bash
mvn -Pnative -DskipTests -U clean native:compile && target/gateway