#!/bin/bash
www="$(cat www.dat)"
curl -s $www/join -X GET --silent 
