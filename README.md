# spring-6-reactive-mongo
Examples of Reactive Programming with Spring Framework.

## Getting started
There are three profiles:
* default profile: expects a MongoDB installed and running on port 27017
* docker-with-compose: is using the MongoDB created with docker-compose running on port 27018
* docker-with-testcontainer: is using the MongoDB provided by testcontainer/docker running on random port

TODO: currently the docker container is created for all profiles. we should avoid that

Then we have a second profile which starts the mongo-db with the testContainer feature which use docker. the exposed port is changing at each start.
You can find the part in the spring boot startup log or via the "docker ps" command

In Unit Test we are using the TestContainer within Docker which requires Docker Desktop installed. In that case the port does change with each test.


This repository has examples from my course [Reactive Programming with Spring Framework 5](https://www.udemy.com/reactive-programming-with-spring-framework-5/?couponCode=GITHUB_REPO_SF5B2G)

## All Spring Framework Guru Courses
### Spring Framework 6
* [Spring Framework 6 - Beginner to Guru](https://www.udemy.com/course/spring-framework-6-beginner-to-guru/?referralCode=2BD0B7B7B6B511D699A9)
* [Spring AI: Beginner to Guru](https://www.udemy.com/course/spring-ai-beginner-to-guru/?referralCode=EF8DB31C723FFC8E2751)
* [Hibernate and Spring Data JPA: Beginner to Guru](https://www.udemy.com/course/hibernate-and-spring-data-jpa-beginner-to-guru/?referralCode=251C4C865302C7B1BB8F)
* [API First Engineering with Spring Boot](https://www.udemy.com/course/api-first-engineering-with-spring-boot/?referralCode=C6DAEE7338215A2CF276)
* [Introduction to Kafka with Spring Boot](https://www.udemy.com/course/introduction-to-kafka-with-spring-boot/?referralCode=15118530CA63AD1AF16D)
* [Spring Security: Beginner to Guru](https://www.udemy.com/course/spring-security-core-beginner-to-guru/?referralCode=306F288EB78688C0F3BC)
