#!/bin/bash

#./activator clean stage

docker stop hmd || true
docker rm hmd || true
docker rmi hmd || true

docker build -t hmd .
rm -rf /target/pki
cp -r /etc/pki/ target/pki
docker run -ti --rm --name hmd \
  -p 9000:9000 \
  -v `pwd`/target/pki/:/etc/pki/:ro \
  hmd \
  /bin/bash -c "export JAVA_OPTS=\"\$JAVA_OPTS -Dhttp.proxyHost=www-cache.reith.bbc.co.uk -Dhttp.proxyPort=80 -Dhttps.proxyHost=www-cache.reith.bbc.co.uk -Dhttps.proxyPort=80\" && bin/guide-assets"
