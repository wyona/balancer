
  Load-balancing Servlet
  ======================

-2) Introduction

       This load-balancing servlet supports sticky sessions and failover for
       the case of a cluster as workers

-1) Contents

        0) Requirements

	1) Configure

	2) Compile

	3) Deploy

        4) Administration

0) Requirements

	- JDK 1.4.2
        - Apache Jakarta Tomcat 5.0.x
        - Cluster (e.g. https://svn.wyona.com/repos/public/erp/trunk)

	
1) Configure

	Copy "build.properties" to "local.build.properties" and
	edit the property "tomcat.home.dir"

	Adapt the "conf/workers.xml" files to your needs:

        <worker>...</worker>

	Add the attribute "jvmRoute" in the tomcat config files (e.g. $TOMCAT_NODE1_HOME/conf/server.xml and $TOMCAT_NODE2_HOME/conf/server.xml) of your tomcat worker servers according to your worker names, e.g.:
	
        <Engine name="Catalina" defaultHost="localhost" debug="0" jvmRoute="balancer1">

        <Engine name="Catalina" defaultHost="localhost" debug="0" jvmRoute="balancer2">

2) Compile

	ant compile

3) Deploy

	ant install

4) When the balancer is up and running you can reach the admin page at /admin.html
