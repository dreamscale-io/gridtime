package com.dreamscale.gridtime.core.service;

public class ProjectAnalyticsConfigService {

    //for any project, there is a stateful scope-bound service that will spin up to support the project
    //and we will eventually need some way, to fan these out horizontally

    //for now, we can have an admin service that configures a table with a project analytics configuration
    //then a service that can read that configuration, instantiate a structure that can operate on the incoming stream

    //so for today, I should write the admin service, and the persistence service,
    // and the service that loads the project configuration that can resolve components for files,
    // will do this part tomorrow

    //1. Define a table, entity, and repo for the config
    //2. Define an admin service that can do add/add/add and generate the OP config in the DB
    //3. The project config, is bound to what exactly? A Jira project?  Seems like I need this associative table


    //tomorrow

    //stream over all the existing file activity data, and create a component column for each entry
    //as new data is coming in, populate the component column

    //analyzer will post process, model the Rooms over time

    //generate new enter/exit room events
    //generate hallways and traversal counts
    //generate summary total activity time within each room
    //generate flames for each room, with attributes for each, and summary flame

    //generate blocks within rooms by files, and total activity within each block, sequenced by earliest entry


    //to do this, I need to be able to pull this stuff in, feed it into some kind of windowing thing

}
