#!/bin/bash
token="$(cat token.dat)"
curl -s https://oauth2.googleapis.com/tokeninfo?id_token=$token -X GET
