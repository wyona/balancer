#!/bin/bash

for i in `pgrep -f tomcat`; do
	kill -s KILL $i;
done

sleep 3

rm -rf /tmp/hsperfdata_greg
rm -rf /home/greg/wyona/jakarta-tomcat-5.0.28/work/Catalina/localhost

ant clean
ant install

/home/greg/wyona/jakarta-tomcat-5.0.28/bin/startup.sh
