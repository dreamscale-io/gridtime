#Postgres Testdata Setup

This doc covers how to setup testdata from a postgres data backup done using heroku data backup utilities, then importing the testdata into your local docker postgres, such that when you boot up the app, the app is pointed to the testdata.

## Connect to your local DB using psql

First, install postgres command line tools locally:

https://www.postgresql.org/download/macosx/

Once you install the database tools, the command line tools are still not in your path.  So you'll need to edit your path as well, by adding a line like this to your .bash_profile file in your home directory:

`export PATH=$PATH:/Library/PostgreSQL/12/bin`

You may also need to edit your /etc/hosts file and add:

`127.0.0.1 local.docker`

Then test your connection to the local postgres:

`psql -h local.docker -d postgres -U postgres -p 5432`

## Create a new clean DB to load the testdata

If you've already been running and testing with an existing DB, first, reset your docker postgres to a clean state, from gridtime project:

`./gradlew refreshP`

`./gradlew createTDB`

Then connect to your local docker postgres:

`psql -h local.docker -d postgres -U postgres -p 5432`

And run...

`create database gridtime;`

## Load the testdata

To load the testdata into the DB, run:

`pg_restore -h local.docker -d gridtime -U postgres -p 5432 <datafile>`

Login to the database and verify the data imported okay:

`psql -h local.docker -d gridtime -U postgres -p 5432`

`select * from root_account;`

Now upgrade the DB to the latest version, by booting up the application so that the liquibase updates all run on top of the imported DB:

`./gradlew bootRun`

