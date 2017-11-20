# Copyright 2017 Nathan Erwin
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
FROM	nderwin/docker-wildfly:latest

LABEL	Author="Nathan Erwin <nathan.d.erwin@gmail.com>"

USER	root
RUN	apt-get update && apt-get -y install netcat-openbsd

USER	wildfly

COPY	run.sh /opt/wildfly
COPY    target/postgresql-42.1.1.jar /opt
COPY	target/contact.war /opt

EXPOSE	8080 9990

HEALTHCHECK	CMD curl -f http://localhost:8080/contact/resources/health

CMD	["/opt/wildfly/run.sh"]
