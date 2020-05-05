#!/bin/bash
token="$(cat token.dat)"
userID="$(cat userID.dat)"
www="$(cat www.dat)"
targetMessage=1
curl --header "Content-Type: application/json" --request DELETE --silent --data "{'tokenId':$token, 'userID':$userID}" $www/likes/$targetMessage
