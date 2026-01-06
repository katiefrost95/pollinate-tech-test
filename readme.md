# Run Locally

## Backend

Prerequisites:

* java 21
* maven 3.9

Run Locally:

    cd taskBackend
    mvn clean install
    mvn spring-boot:run

The application will run on http://localhost:8080

The h2 console will run on http://localhost:8080/h2-console. To connect make sure the following values are set (can check in taskBackend/src/main/resources/application.properties):

    datasourceUrl: jdbc:h2:mem:testdb
    password: password

## Frontend

Prerequisites:
* node v24
* angular cli v21

Run Locally:

    cd taskFrontend
    npm install
    ng serve

The application will run on http://localhost:4200

