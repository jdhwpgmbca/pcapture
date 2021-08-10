#!/usr/bin/env bash

mvn exec:java -Dexec.mainClass=com.rtds.fs.utils.JwtTokenGenerator -Dexec.classpathScope=test -Dexec.args="/JwtClaims.json 86400"
