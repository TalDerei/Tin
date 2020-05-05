#!/bin/bash
token="$(cat token.dat)"
userID="$(cat userID.dat)"
www="$(cat www.dat)"
uid=1
curl --header "Content-Type: application/json" -X GET --data "{'tokenId':$token, 'userID':$userID}" $www/history/$uid
