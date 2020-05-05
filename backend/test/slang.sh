#!/bin/bash
token="$(cat token.dat)"
userID="$(cat userID.dat)"
www="$(cat www.dat)"
curl --header "Content-Type: application/json" --request POST --silent --data "{'mTitle':'Movie', 'mMessage':'bitch! fuck!', 'mLink':'http://slang.com', 'tokenId':$token, 'userID':$userID}" $www/messages
