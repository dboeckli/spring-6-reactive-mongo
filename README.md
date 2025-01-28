# spring-6-reactive-mongo
Examples of Reactive Programming with Spring Framework.

## Getting started
Server runs on port 8083. Requires the auth server running on port 9000.
There are three profiles:
* default profile: expects a MongoDB installed and running on port 27017
* docker-with-compose: is using the MongoDB created with docker-compose running on port 27018
* docker-with-testcontainer: is using the MongoDB provided by testcontainer/docker running on random port

TODO: currently the docker container is created for all profiles. we should avoid that

Then we have a second profile which starts the mongo-db with the testContainer feature which use docker. the exposed port is changing at each start.
You can find the part in the spring boot startup log or via the "docker ps" command

In Unit Test we are using the TestContainer within Docker which requires Docker Desktop installed. In that case the port does change with each test.

## Urls

openapi api-docs: http://localhost:8083/v3/api-docs
openapi gui: http://localhost:8083/swagger-ui/index.html
openapi-yaml: http://localhost:8083/v3/api-docs.yaml

## Docker

### create image
```shell
.\mvnw clean package spring-boot:build-image
```
or just run
```shell
.\mvnw clean install
```

### run image

Hint: remove the daemon flag -d to see what is happening, else it run in background

```shell
docker run --name mongo -d -e MONGO_INITDB_ROOT_USERNAME=root -e MONGO_INITDB_ROOT_PASSWORD=secret -p 27017:27017 mongo 
docker stop mongo
docker rm mongo
docker start mongo
docker logs mongo

docker run --name reactive-mongo -d -p 8083:8080 -e SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI=http://auth-server:9000 -e SERVER_PORT=8080 -e SPRING_DATA_MONGODB_URI=mongodb://mongo:27017/sfg -e SPRING_DATA_MONGODB_USERNAME=root -e SPRING_DATA_MONGODB_PASSWORD=secret --link auth-server:auth-server --link mongo:mongo spring-6-reactive-mongo:0.0.1-SNAPSHOT
 
docker stop reactive-mongo
docker rm reactive-mongo
docker start reactive-mongo
docker logs reactive-mongo
```

This repository has examples from my course [Reactive Programming with Spring Framework 5](https://www.udemy.com/reactive-programming-with-spring-framework-5/?couponCode=GITHUB_REPO_SF5B2G)

## All Spring Framework Guru Courses
### Spring Framework 6
* [Spring Framework 6 - Beginner to Guru](https://www.udemy.com/course/spring-framework-6-beginner-to-guru/?referralCode=2BD0B7B7B6B511D699A9)
* [Spring AI: Beginner to Guru](https://www.udemy.com/course/spring-ai-beginner-to-guru/?referralCode=EF8DB31C723FFC8E2751)
* [Hibernate and Spring Data JPA: Beginner to Guru](https://www.udemy.com/course/hibernate-and-spring-data-jpa-beginner-to-guru/?referralCode=251C4C865302C7B1BB8F)
* [API First Engineering with Spring Boot](https://www.udemy.com/course/api-first-engineering-with-spring-boot/?referralCode=C6DAEE7338215A2CF276)
* [Introduction to Kafka with Spring Boot](https://www.udemy.com/course/introduction-to-kafka-with-spring-boot/?referralCode=15118530CA63AD1AF16D)
* [Spring Security: Beginner to Guru](https://www.udemy.com/course/spring-security-core-beginner-to-guru/?referralCode=306F288EB78688C0F3BC)
