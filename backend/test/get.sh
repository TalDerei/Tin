#!/bin/bash
www="$(cat www.dat)"
curl -s $www/messages -X GET
