#!/bin/sh
token="$(cat token.dat)"
userID="$(cat userID.dat)"
fileId="$(cat fileID.dat)"
www="$(cat www.dat)"
curl --header "Content-Type: application/json" -X GET --data "{'tokenId':$token, 'userID':$userID}" $www/file/$fileId
