#!/usr/bin/env bash

openssl genpkey -out privateKey.pem -algorithm RSA -pkeyopt rsa_keygen_bits:4096
openssl rsa -pubout -in privateKey.pem -out publicKey.pem

mv -f publicKey.pem src/main/resources/META-INF/resources/
mv -f privateKey.pem src/test/resources/
