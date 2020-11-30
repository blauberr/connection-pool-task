## ConnectionManager 

- class ConnectionManager is configured with 2 database servers: Oracle, PostgreSQL
- servers can be changed in DbConstants class
- class ConnectionPool holds and gives database connections 
- failover access happens when primary database is no longer available
- whenever failover mode is on and connection is being asked from ConnectionManager, ConnectionManager checks if primary database is already up

## Logging System 
- logging is made with appender with output to .log file
- all logs are stored in both files
- ElasticSearch is added with ELK stack, which uses docker-compose.yml for all the configuration
- Kibana is accessed via localhost:5601 (can be changed in docker-compose.yml file) where log files can be imported for fulltext search
- [Pros and Cons](https://github.com/blauberr/connection-pool-task/blob/master/Pros%20and%20Cons.pdf) of implemented full-text search

To start ELK: 
- run Shell in project directory
```
	docker-compose up
```

Since project is library type, no Main class is needed
