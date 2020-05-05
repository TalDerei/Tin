#!/bin/bash
token="$(cat token.dat)"
userID="$(cat userID.dat)"
www="$(cat www.dat)"
message=$1 # 1st arg
curl --header "Content-Type: application/json" --request POST --silent --data "{'mTitle':'Movie', 'mMessage':$message, 'mLink':'http://test.com', 'tokenId':$token, 'userID':$userID}" $www/messages
