# SWDesign 'TestContainers & Integration Tests' task

## What is implemented:
* Simulator of the Stock Exchange, which is configured to be packaged into the Docker container.
* Client app which runs tests using Stock Exchange API.

## How to launch:
* Stock Exchange Microservice: `cd` into the `integration_exchange` directory and issue `mvn -am package` command. Maven should automatically resolve all dependencies, compile code and then package it into the Docker container.
* Test client: `cd` into the `integration_tests` directory and execute `mvn test` command. Maven should automatically resolve all dependencies, compile code and then start tests.

## How does it work:
* Stock Exchange is a simple Spring application, it contains two model objects: `User` and `Stock`, repositories and services to work with them (persistency is provided by H2 in-memory database) and single REST API Controller, which handles all the requests. The most *advanced* methods which process stock purchase are located in the special `TransactionService`, because I didn't want to open `save()` methods of the repositories by forwarding them through the `UserService` and `StockService`. This implementation is in no way thread-safe, but it was not the main focus of our task here.
* Test app is implemented using JUnit5, TestContainers and Spring. I use Spring here because it provides rather fancy way of conversation over HTTP using `RestTemplate`'s. Before each `@Test` method is invoked, `TestContainers` deploy a new instance of Stock Exchange on the local machine. It uses dynamic port forwarding, so each time I have to get correct port mapping to reach 8080 of the Stock Exchange in container. I've used `maven:3.8.3-openjdk-17-slim` as the base Docker image because it is enough for our task and requires less disk space to be deployed.

## More on tests:
Tests are implemented to cover each API method. Some of them are covered by the individual tests, whereas the most interesting Buy&Sell methods are tested by simulation. That test prepares environment by creating users and stocks, then plays a number of steps. At each step random user is chosen which then tries to buy or sell a random stock. Both successful and failing situations are checked. After that, prices of stocks are randomly updated.

### NOTE
I had some issues with JSON deserialization of `Map<Stock, Long>` and to be on the safe side, in the simulation I manually add/update stocks of user. That's due to some limitations of `jackson` library used by Spring for JSON deserialization. Other fields have no trouble with that.
