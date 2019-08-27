[![Build Status](https://travis-ci.org/dreamscale/gridtime.svg?branch=master)](https://travis-ci.org/dreamscale/gridtime)

# GridTime Server
Generates zoomable tile feeds, and aggregate tile feeds from flow source data streams

## Swagger

For the Swagger UI, navigate to `/swagger.html` on the running server (e.g. for localhost, `http://localhost:8080/swagger.html`)
To retrieve the swagger json, navigate to `/v2/api-docs`


## Heroku

This application is currently deployed on Heroku at http://ds-gridtime.herokuapp.com.  For an API-Key on the shared service, 
please contact janelle@dreamscale.io.  We will have account creation setup soon, but while this project is still in early 
development, accounts are created manually.

Install heroku cli (google)

Log into heroku

`heroku login`

#### Initial Heroku Application Creation

Create the heroku application

`heroku create ds-gridtime`

Create the database (can upgrade to hobby-basic just by associating credit card w/ account)

`heroku addons:create heroku-postgresql:hobby-dev`

Configure the domain

`heroku domains:add gridtime.dreamscale.io`

Set up SSL

`heroku certs:auto:enable`

#### Ongoing Heroku Application Deployment

If you have not previously deployed to Heroku, initialize the remote

`heroku git:remote -a ds-gridtime`

Deploy the application on heroku

`git push heroku master`

### How to Troubleshooting Heroku DB specific Issue

`heroku pg:backups:capture`

`heroku pg:backups:download`

`pg_restore --verbose --clean --no-acl --no-owner -h local.docker -U postgres -d gridtime latest.dump`