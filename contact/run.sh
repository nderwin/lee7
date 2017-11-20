#!/bin/bash

set -e

function shutdown() {
    echo "Received TERM signal, shutting down now..."
    /opt/bin/wildfly/jboss-cli.sh -c ":shutdown(timeout=3)"
    exit 0
}

trap shutdown SIGTERM

dbhost="database"
dbport=5432

until nc -z "$dbhost" "$dbport"; do
    >&2 echo "$dbhost is unavailable - sleeping"
    sleep 1
done

/opt/wildfly/bin/standalone.sh -b 0.0.0.0 -bmanagement 0.0.0.0 &

wf_pid=$!

until nc -z localhost 9990; do
    >&2 echo "...waiting for Wildfly to start..."
    sleep 1
done

/opt/wildfly/bin/jboss-cli.sh --connect --command="deploy --force /opt/postgresql-42.1.1.jar"

/opt/wildfly/bin/jboss-cli.sh --connect --command="data-source add \
        --name=contactDS \
        --connection-url=jdbc:postgresql://database:5432/lee7 \
        --driver-name=postgresql-42.1.1.jar \
        --jndi-name=java:/contactDS \
        --driver-class=org.postgresql.Driver \
        --jta=true \
        --user-name=`cat /run/secrets/postgres_username` \
        --password=`cat /run/secrets/postgres_password` \
        --enabled=true \
        --use-ccm=true \
        --valid-connection-checker-class-name=org.jboss.jca.adapters.jdbc.extensions.postgres.PostgreSQLValidConnectionChecker \
        --background-validation=true \
        --exception-sorter-class-name=org.jboss.jca.adapters.jdbc.extensions.postgres.PostgreSQLExceptionSorter"

/opt/wildfly/bin/jboss-cli.sh --connect --command="deploy --force `ls /opt/*.war`"

wait $wf_pid