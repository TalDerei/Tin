#!/bin/bash
token="$(cat token.dat)"
www="$(cat www.dat)"
curl --header "Content-Type: application/json" --silent --request POST --data "{'tokenId':$token}" $www/users/login
