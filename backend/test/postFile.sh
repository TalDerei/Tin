#!/bin/bash
www="$(cat www.dat)"
curl -X POST --silent -F 'upload_file=@./protein.png' -F 'mime=application/png' $www/upload
