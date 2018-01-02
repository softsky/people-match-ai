
# Start system to process files

## Prerequisites 

- Docker should be installed
- Maven should be installed (in case you neeed to build image from sources)

## Build docker images

You can build images manually from sources or use existing in docker hub repository

#### Build HDFS handlers Docker image

* Clone project:

`git clone https://github.com/softsky/hdfs-handlers`

* Build executable jar file from project root:

`mvn clean install`

* Build docker image from project root:

`docker build -f src/main/docker/Dockerfile -t softsky/hdfs-handlers .`

#### Build WEB UI Docker image

* Clone project:

`git clone https://github.com/softsky/people-match-ai-ui`

* Build executable jar file from project root:

`mvn clean install`

* Build docker image from project root:

`docker build -f src/main/docker/Dockerfile -t softsky/peole-match-ui .`


## Run application with docker-compose

* Create `docker-compose.yml` from template here: [docker-compose-template.yml](https://github.com/softsky/people-match-ai-ui/blob/master/docker-compose-template.yml)

* In compose file replace AWS credentials placeholders: 

```
   ...
   AWS_ACCESS_KEY_ID: <put_your_ID_here>
   AWS_SECRET_ACCESS_KEY: <put_your_secret_here>
   ...
```

* Run docker compose: 

`docker-compose -f docker-compose.yml up`

You can access WEB UI with url: `http://<your_host>:8888/`
