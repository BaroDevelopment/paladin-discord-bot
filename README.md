# Paladin Discord Bot

## Local Development

### Using TestContainers
Run `PaladinDiscordBotApplicationTest` and it will run all docker containers automatically. You don't have to use 
`docker` or `docker-compose`. However, you need Docker installed!
To make use of reusable testcontainers copy the `docker/.testcontainers.properties` to your `$HOME` directory. \

### Credentials
#### Postgres
| Host      | Port                             | Databasename | User | Password |
|-----------|----------------------------------|--------------|------|----------|
| localhost | check your docker container port | Paladin      | test | test     |

#### Redis
| Host      | Port                             | Password          |
|-----------|----------------------------------|-------------------|
| localhost | check your docker container port | without password! |

## Using docker-compose
Copy the application.properties.sample from the resources to `docker/application.properties`. \
Start postgres and redis `docker-compose up paladin-redis paladin-redis`
Finally run your Application with `PaladinDiscordBotApplication`