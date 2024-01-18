# pagoPA Biz Events Service

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=pagopa_pagopa-biz-events-service&metric=alert_status)](https://sonarcloud.io/dashboard?id=pagopa_pagopa-biz-events-service)

The microservice exposes REST APIs to get the list and the related detailed info regarding payment receipts.

---
## Summary 📖

- [API Documentation 📖](#api-documentation)
- [Technology Stack](#technology-stack)
- [Start Project Locally 🚀](#start-project-locally)
    * [Prerequisites](#prerequisites)
    * [Run docker container](#run-docker-container)
- [Develop Locally 💻](#develop-locally)
    * [Prerequisites](#prerequisites)
    * [Run the project](#run-the-project)
    * [Spring Profiles](#spring-profiles)
    * [Testing 🧪](#testing)
        + [Unit testing](#unit-testing)
        + [Integration testing](#integration-testing)
        + [Performance testing](#performance-testing)
- [Contributors 👥](#contributors)
    * [Mainteiners](#mainteiners)


---
## API Documentation 📖
See the [OpenApi 3 here.](https://editor.swagger.io/?url=https://raw.githubusercontent.com/pagopa/pagopa-biz-events-service/main/openapi/openapi.json)

---

## Technology Stack
- Java 17
- Spring Boot
- Spring Web
- Azure CosmosDB
---

## Start Project Locally 🚀

### Prerequisites
- docker

### Run docker container
from `./docker` directory

`sh ./run_docker.sh dev`

ℹ️ Note: for PagoPa ACR is required the login `az acr login -n <acr-name>`

---

## Develop Locally 💻

### Prerequisites
- git
- maven
- jdk-17

### Run the project

Start the springboot application with this command:

`mvn spring-boot:run -Dspring-boot.run.profiles=local`



### Spring Profiles

- **local**: to develop locally.
- _default (no profile set)_: The application gets the properties from the environment (for Azure).


### Testing 🧪

#### Unit testing

To run the **Junit** tests:

`mvn clean verify`

#### Integration testing
From `./integration-test/src`

1. `yarn install`
2. `yarn test`

#### Performance testing
install [k6](https://k6.io/) and then from `./performance-test/src`

1. `k6 run --env VARS=local.environment.json --env TEST_TYPE=./test-types/load.json main_scenario.js`


---

## Contributors 👥
Made with ❤️ by PagoPa S.p.A.

### Mainteiners
See `CODEOWNERS` file
