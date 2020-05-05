#!/bin/bash
token="$(cat token.dat)"
userID="$(cat userID.dat)"
www="$(cat www.dat)"
targetMessage=1
curl --header "Content-Type: application/json" --request PUT --silent --data "{'mTitle':'Movie', 'mMessage':'put change!', 'tokenId':$token, 'userID':$userID}" $www/messages/$targetMessage
