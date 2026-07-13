#!/bin/sh

set -eu

certificate=/etc/letsencrypt/live/commerceops.ddoriny.com/fullchain.pem
private_key=/etc/letsencrypt/live/commerceops.ddoriny.com/privkey.pem

if [ -s "$certificate" ] && [ -s "$private_key" ]; then
    echo "commerceops: selecting HTTPS nginx configuration"
    cp /etc/nginx/config-source/https.conf /etc/nginx/conf.d/default.conf
else
    echo "commerceops: certificate not found; selecting HTTP bootstrap configuration"
    cp /etc/nginx/config-source/http.conf /etc/nginx/conf.d/default.conf
fi
