#!/bin/bash
token="$(cat token.dat)"
userID="$(cat userID.dat)"
www="$(cat www.dat)"
targetMessage=$1
curl --header "Content-Type: application/json" --request DELETE --data "{'tokenId':$token, 'userID':$userID}" $www/messages/$targetMessage
