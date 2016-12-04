FROM	nderwin/docker-wildfly:latest

MAINTAINER	Nathan Erwin <nathan.d.erwin@gmail.com>

ARG finalName

USER	wildfly

# Replace the configuration
ADD	src/main/resources-wildfly/standalone.xml /opt/wildfly/standalone/configuration/standalone.xml

# Add the file to be deployed
ADD	target/$finalName.war /opt/wildfly/standalone/deployments/$finalName.war

EXPOSE	8080 9990
