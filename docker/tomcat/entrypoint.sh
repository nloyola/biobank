#!/bin/bash

set -e

# Generate db.properties from environment variables so Ant can substitute
# credentials into application-config.xml when building the WAR.
cat > /opt/biobank/db.properties <<EOF
database.host=$DB_HOST
database.name=$DB_NAME
database.username=$DB_USER
database.password=$DB_PASSWORD
database.driver=com.mysql.jdbc.Driver
database.url=jdbc:mysql://$DB_HOST:$DB_PORT/$DB_NAME
jdbc.driver.jar=mysql-connector-java-5.1.15-bin.jar
EOF

cd /opt/biobank
ant -Doffline=1 deploy_tomcat

CONTEXT_DIR="$CATALINA_HOME/conf/Catalina/localhost"
mkdir -p "$CONTEXT_DIR"

cat > "$CONTEXT_DIR/biobank.xml" <<EOF
<?xml version='1.0' encoding='utf-8'?>
<Context>
    <Resource name="jdbc/biobank"
              auth="Container"
              type="javax.sql.DataSource"
              factory="org.apache.tomcat.jdbc.pool.DataSourceFactory"
              testWhileIdle="true"
              testOnBorrow="true"
              testOnReturn="false"
              validationQuery="SELECT 1"
              validationInterval="30000"
              timeBetweenEvictionRunsMillis="30000"
              maxActive="20"
              maxIdle="2"
              minIdle="1"
              maxWait="10000"
              initialSize="1"
              removeAbandonedTimeout="60"
              removeAbandoned="true"
              logAbandoned="true"
              minEvictableIdleTimeMillis="30000"
              jmxEnabled="true"
              username="$DB_USER"
              password="$DB_PASSWORD"
              driverClassName="com.mysql.jdbc.Driver"
              url="jdbc:mysql://$DB_HOST:$DB_PORT/$DB_NAME"/>

    <Resource name="jdbc/csmupt"
              auth="Container"
              type="javax.sql.DataSource"
              factory="org.apache.tomcat.jdbc.pool.DataSourceFactory"
              testWhileIdle="true"
              testOnBorrow="true"
              testOnReturn="false"
              validationQuery="SELECT 1"
              validationInterval="30000"
              timeBetweenEvictionRunsMillis="30000"
              maxActive="20"
              maxIdle="2"
              minIdle="1"
              maxWait="10000"
              initialSize="1"
              removeAbandonedTimeout="60"
              removeAbandoned="true"
              logAbandoned="true"
              minEvictableIdleTimeMillis="30000"
              jmxEnabled="true"
              username="$DB_USER"
              password="$DB_PASSWORD"
              driverClassName="com.mysql.jdbc.Driver"
              url="jdbc:mysql://$DB_HOST:$DB_PORT/$DB_NAME"/>
</Context>
EOF

exec "$CATALINA_HOME/bin/catalina.sh" run
