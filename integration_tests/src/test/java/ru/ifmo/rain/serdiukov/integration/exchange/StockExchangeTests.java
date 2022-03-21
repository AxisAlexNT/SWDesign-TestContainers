package ru.ifmo.rain.serdiukov.integration.exchange;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.ifmo.rain.serdiukov.integration.exchange.domain.commands.StockOperationType;
import ru.ifmo.rain.serdiukov.integration.exchange.domain.model.Stock;
import ru.ifmo.rain.serdiukov.integration.exchange.domain.model.User;
import ru.ifmo.rain.serdiukov.integration.exchange.dto.StockOperationRequestDTO;
import ru.ifmo.rain.serdiukov.integration.exchange.dto.StockOperationResponseDTO;

import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.fail;

@ExtendWith(SpringExtension.class)
@Testcontainers
public class StockExchangeTests {
    private static String apiPrefix;
    @Container
    private final GenericContainer<?> exchangeContainer =
            new GenericContainer<>("exchange:1.0-SNAPSHOT")
                    .withExposedPorts(8080);
    private final Random random = new Random();


    @BeforeEach
    public void initPort() {
        int serverPort = exchangeContainer.getMappedPort(8080);
        apiPrefix = String.format("http://localhost:%d/api/1/", serverPort);
    }

    @Test
    public void contextLoads() {
        // Should start without any issue
    }


    @Test
    public void createUserOk() {
        final RestTemplate restTemplate = new RestTemplate();
        final String name = String.format("NAME<%s>", UUID.randomUUID());
        final ResponseEntity<User> responseCreate = restTemplate.getForEntity(
                apiPrefix + "createUser?login={login}&name={name}",
                User.class,
                Map.of(
                        "login", "testLogin",
                        "name", name
                )
        );
        assertThat("Registration should have been successful", responseCreate.getStatusCode(), is(equalTo(HttpStatus.OK)));
        assertThat("API call should have returned body", responseCreate.getBody(), is(not(nullValue())));
        assertThat("Created user should have the same login as provided", responseCreate.getBody().getLogin(), is(equalTo("testLogin")));
        assertThat("Created user should have the same name as provided", responseCreate.getBody().getName(), is(equalTo(name)));
        assertThat("User should have an ID after registration", responseCreate.getBody().getId(), is(not(equalTo(nullValue()))));
        assertThat("User should have an empty stock portfolio after registration", responseCreate.getBody().getPortfolio(), is(not(equalTo(nullValue()))));
        assertThat("User should have an empty stock portfolio after registration", responseCreate.getBody().getPortfolio().isEmpty(), is(equalTo(true)));
        assertThat("User should have zero balance after registration", responseCreate.getBody().getBalance(), is(equalTo(0L)));
    }

    @Test
    public void createUserFail() {
        final RestTemplate restTemplate = new RestTemplate();
        final String name = String.format("NAME<%s>", UUID.randomUUID());
        try {
            final ResponseEntity<User> response = restTemplate.getForEntity(
                    apiPrefix + "createUser?login={login}&name={name}",
                    User.class,
                    Map.of(
                            "login", "   ",
                            "name", name
                    )
            );
            fail("Login must not be null or empty, registration should fail");
        } catch (final HttpClientErrorException.BadRequest e) {
            // Ok
        }
    }


    @Test
    public void createStock() {
        final RestTemplate restTemplate = new RestTemplate();
        final String index = String.format("NAME<%s>", UUID.randomUUID());
        final String name = "Name might be not unique";
        final long price = random.nextLong(1L, Long.MAX_VALUE / 2L - 1L);
        final ResponseEntity<Stock> responseCreate = restTemplate.getForEntity(
                apiPrefix + "createStock?index={index}&name={name}&price={price}",
                Stock.class,
                Map.of(
                        "index", index,
                        "name", name,
                        "price", price

                )
        );
        assertThat("Stock creation should have been successful", responseCreate.getStatusCode(), is(equalTo(HttpStatus.OK)));
        assertThat("API call should have returned body", responseCreate.getBody(), is(not(nullValue())));
        assertThat("Created stock should have the same index as provided", responseCreate.getBody().getIndex(), is(equalTo(index)));
        assertThat("Created stock should have the same name as provided", responseCreate.getBody().getName(), is(equalTo(name)));
        assertThat("Created stock should have the same price as provided", responseCreate.getBody().getPrice(), is(equalTo(price)));
        assertThat("Created stock should have zero availability after creation", responseCreate.getBody().getAvailableAmount(), is(equalTo(0L)));
        assertThat("Stock should have an ID after creation", responseCreate.getBody().getId(), is(not(equalTo(nullValue()))));

        try {
            restTemplate.getForEntity(
                    apiPrefix + "createStock?index={index}&name={name}&price={price}",
                    Stock.class,
                    Map.of(
                            "index", index,
                            "name", (name + name),
                            "price", (2 * price)

                    )
            );
            fail("Creation of stock with the same index should not be possible");
        } catch (final HttpClientErrorException.BadRequest e) {
            // Ok
        }
    }


    @Test
    public void updateStockPrice() {
        final RestTemplate restTemplate = new RestTemplate();
        final String index = String.format("NAME<%s>", UUID.randomUUID());
        final String name = "Name might be not unique";
        final long price = random.nextLong(1L, Long.MAX_VALUE / 2L - 1L);
        final ResponseEntity<Stock> responseCreate = restTemplate.getForEntity(
                apiPrefix + "createStock?index={index}&name={name}&price={price}",
                Stock.class,
                Map.of(
                        "index", index,
                        "name", name,
                        "price", price

                )
        );

        assertThat("Stock creation should have been successful", responseCreate.getStatusCode(), is(equalTo(HttpStatus.OK)));
        assertThat("API call should have returned body", responseCreate.getBody(), is(not(nullValue())));
        assertThat("Created stock should have the same index as provided", responseCreate.getBody().getIndex(), is(equalTo(index)));
        assertThat("Created stock should have the same name as provided", responseCreate.getBody().getName(), is(equalTo(name)));
        assertThat("Created stock should have the same price as provided", responseCreate.getBody().getPrice(), is(equalTo(price)));
        assertThat("Created stock should have zero availability after creation", responseCreate.getBody().getAvailableAmount(), is(equalTo(0L)));
        assertThat("Stock should have an ID after creation", responseCreate.getBody().getId(), is(not(equalTo(nullValue()))));

        final long newPrice = price * 2L;

        final ResponseEntity<Stock> responseUpdate = restTemplate.getForEntity(
                apiPrefix + "updateStockPrice?index={index}&price={price}",
                Stock.class,
                Map.of(
                        "index", index,
                        "price", newPrice
                )
        );

        assertThat("Stock creation should have been successful", responseUpdate.getStatusCode(), is(equalTo(HttpStatus.OK)));
        assertThat("API call should have returned body", responseUpdate.getBody(), is(not(nullValue())));
        assertThat("Stock should have updated price", responseUpdate.getBody().getPrice(), is(equalTo(newPrice)));
        assertThat("Updated stock should have the same index as provided", responseUpdate.getBody().getIndex(), is(equalTo(index)));
        assertThat("Updated stock should have the same name as provided", responseUpdate.getBody().getName(), is(equalTo(name)));
        assertThat("Updated stock should have zero availability after creation", responseUpdate.getBody().getAvailableAmount(), is(equalTo(0L)));
        assertThat("Stock should have the same ID after price update", responseUpdate.getBody().getId(), is(equalTo(responseCreate.getBody().getId())));


        try {
            restTemplate.getForEntity(
                    apiPrefix + "updateStockPrice?index={index}&price={price}",
                    Stock.class,
                    Map.of(
                            "index", index,
                            "price", -price
                    )
            );
            fail("Stock price might not be updated to negative values");
        } catch (final HttpClientErrorException.BadRequest e) {
            // Ok
        }

        try {
            restTemplate.getForEntity(
                    apiPrefix + "updateStockPrice?index={index}&newPrice={newPrice}",
                    Stock.class,
                    Map.of(
                            "index", index,
                            "newPrice", -price
                    )
            );
            fail("Parameter name should match controller's");
        } catch (final HttpServerErrorException.InternalServerError e) {
            // Ok
        }
    }


    @Test
    public void topUpBalance() {
        final RestTemplate restTemplate = new RestTemplate();
        final String name = String.format("NAME<%s>", UUID.randomUUID());
        final ResponseEntity<User> responseCreate = restTemplate.getForEntity(
                apiPrefix + "createUser?login={login}&name={name}",
                User.class,
                Map.of(
                        "login", "testLogin",
                        "name", name
                )
        );
        assertThat("Registration should have been successful", responseCreate.getStatusCode(), is(equalTo(HttpStatus.OK)));
        assertThat("API call should have returned body", responseCreate.getBody(), is(not(nullValue())));
        assertThat("Created user should have the same login as provided", responseCreate.getBody().getLogin(), is(equalTo("testLogin")));
        assertThat("Created user should have the same name as provided", responseCreate.getBody().getName(), is(equalTo(name)));
        assertThat("User should have an ID after registration", responseCreate.getBody().getId(), is(not(equalTo(nullValue()))));
        assertThat("User should have an empty stock portfolio after registration", responseCreate.getBody().getPortfolio(), is(not(equalTo(nullValue()))));
        assertThat("User should have an empty stock portfolio after registration", responseCreate.getBody().getPortfolio().isEmpty(), is(equalTo(true)));

        final long newBalance = random.nextLong(1L, Long.MAX_VALUE / 2L - 1L);

        final ResponseEntity<User> responseUpdate = restTemplate.getForEntity(
                apiPrefix + "topUp?login={login}&amount={amount}",
                User.class,
                Map.of(
                        "login", "testLogin",
                        "amount", newBalance
                )
        );

        assertThat("Balance update should have been successful", responseUpdate.getStatusCode(), is(equalTo(HttpStatus.OK)));
        assertThat("API call should have returned body", responseUpdate.getBody(), is(not(nullValue())));
        assertThat("Updated user should have the same login as provided", responseUpdate.getBody().getLogin(), is(equalTo("testLogin")));
        assertThat("Updated user should have the same name as provided", responseUpdate.getBody().getName(), is(equalTo(name)));
        assertThat("User should have the same ID after top up", responseUpdate.getBody().getId(), is(equalTo(responseCreate.getBody().getId())));
        assertThat("User should have an updated balance after top up", responseUpdate.getBody().getBalance(), is(equalTo(newBalance)));


        try {
            restTemplate.getForEntity(
                    apiPrefix + "topUp?login={login}&amount={amount}",
                    User.class,
                    Map.of(
                            "login", "testLogin",
                            "amount", -newBalance
                    )
            );
            fail("Negative top up should not be possible");
        } catch (final HttpClientErrorException.BadRequest e) {
            // Ok
        }
    }

    @Test
    public void increaseStockAvailability() {
        final RestTemplate restTemplate = new RestTemplate();
        final String index = String.format("NAME<%s>", UUID.randomUUID());
        final String name = "Name might be not unique";
        final long price = random.nextLong(1L, Long.MAX_VALUE / 2L - 1L);
        final ResponseEntity<Stock> responseCreate = restTemplate.getForEntity(
                apiPrefix + "createStock?index={index}&name={name}&price={price}",
                Stock.class,
                Map.of(
                        "index", index,
                        "name", name,
                        "price", price

                )
        );

        assertThat("Stock creation should have been successful", responseCreate.getStatusCode(), is(equalTo(HttpStatus.OK)));
        assertThat("API call should have returned body", responseCreate.getBody(), is(not(nullValue())));
        assertThat("Created stock should have the same index as provided", responseCreate.getBody().getIndex(), is(equalTo(index)));
        assertThat("Created stock should have the same name as provided", responseCreate.getBody().getName(), is(equalTo(name)));
        assertThat("Created stock should have the same price as provided", responseCreate.getBody().getPrice(), is(equalTo(price)));
        assertThat("Created stock should have zero availability after creation", responseCreate.getBody().getAvailableAmount(), is(equalTo(0L)));
        assertThat("Stock should have an ID after creation", responseCreate.getBody().getId(), is(not(equalTo(nullValue()))));

        final long amount = random.nextLong(1L, Long.MAX_VALUE / 2L - 1);

        final ResponseEntity<Stock> responseUpdate = restTemplate.getForEntity(
                apiPrefix + "increaseStockAmount?index={index}&amount={amount}",
                Stock.class,
                Map.of(
                        "index", index,
                        "amount", amount
                )
        );

        assertThat("Stock creation should have been successful", responseUpdate.getStatusCode(), is(equalTo(HttpStatus.OK)));
        assertThat("API call should have returned body", responseUpdate.getBody(), is(not(nullValue())));
        assertThat("Stock should have the same price", responseUpdate.getBody().getPrice(), is(equalTo(price)));
        assertThat("Updated stock should have the same index as provided", responseUpdate.getBody().getIndex(), is(equalTo(index)));
        assertThat("Updated stock should have the same name as provided", responseUpdate.getBody().getName(), is(equalTo(name)));
        assertThat("Updated stock should have required availability after an update", responseUpdate.getBody().getAvailableAmount(), is(equalTo(amount)));
        assertThat("Stock should have the same ID after price update", responseUpdate.getBody().getId(), is(equalTo(responseCreate.getBody().getId())));


        try {
            restTemplate.getForEntity(
                    apiPrefix + "increaseStockAmount?index={index}&amount={amount}",
                    Stock.class,
                    Map.of(
                            "index", index,
                            "amount", -amount
                    )
            );
            fail("Decreasing stock amount should not be possible");
        } catch (final HttpClientErrorException.BadRequest e) {
            // Ok
        }
    }


    @Test
    public void buySellTest() {
        final RestTemplate restTemplate = new RestTemplate();
        final int userCount = random.nextInt(2, 10);
        final int stockCount = random.nextInt(2, 10);
        final int testCount = 100;
        final String[] logins = new String[userCount];
        final String[] indices = new String[stockCount];
        final List<User> users = new ArrayList<>();
        final List<Stock> stocks = new ArrayList<>();

        for (int i = 0; i < userCount; i++) {
            final String name = String.format("NAME<%d>", i);
            final String login = String.format("LOGIN<%d>", i);
            logins[i] = login;
            final ResponseEntity<User> responseCreate = restTemplate.getForEntity(
                    apiPrefix + "createUser?login={login}&name={name}",
                    User.class,
                    Map.of(
                            "login", login,
                            "name", name
                    )
            );
            assertThat("Registration should have been successful", responseCreate.getStatusCode(), is(equalTo(HttpStatus.OK)));
            assertThat("API call should have returned body", responseCreate.getBody(), is(not(nullValue())));
            assertThat("Created user should have the same login as provided", responseCreate.getBody().getLogin(), is(equalTo(login)));
            assertThat("Created user should have the same name as provided", responseCreate.getBody().getName(), is(equalTo(name)));
            assertThat("User should have an ID after registration", responseCreate.getBody().getId(), is(not(equalTo(nullValue()))));
            assertThat("User should have an empty stock portfolio after registration", responseCreate.getBody().getPortfolio(), is(not(equalTo(nullValue()))));
            assertThat("User should have an empty stock portfolio after registration", responseCreate.getBody().getPortfolio().isEmpty(), is(equalTo(true)));


            final long newBalance = random.nextLong(0, 100000L);

            final ResponseEntity<User> responseUpdate = restTemplate.getForEntity(
                    apiPrefix + "topUp?login={login}&amount={amount}",
                    User.class,
                    Map.of(
                            "login", login,
                            "amount", newBalance
                    )
            );

            assertThat("Balance update should have been successful", responseUpdate.getStatusCode(), is(equalTo(HttpStatus.OK)));
            assertThat("API call should have returned body", responseUpdate.getBody(), is(not(nullValue())));
            assertThat("Updated user should have the same login as provided", responseUpdate.getBody().getLogin(), is(equalTo(login)));
            assertThat("Updated user should have the same name as provided", responseUpdate.getBody().getName(), is(equalTo(name)));
            assertThat("User should have the same ID after top up", responseUpdate.getBody().getId(), is(equalTo(responseCreate.getBody().getId())));
            assertThat("User should have an updated balance after top up", responseUpdate.getBody().getBalance(), is(equalTo(newBalance)));

            users.add(responseUpdate.getBody());
        }


        for (int i = 0; i < stockCount; i++) {
            final String index = String.format("NAME<%s>", UUID.randomUUID());
            final String name = "Name might be not unique";
            final long price = random.nextLong(1L, 100L);
            indices[i] = index;
            final ResponseEntity<Stock> responseCreate = restTemplate.getForEntity(
                    apiPrefix + "createStock?index={index}&name={name}&price={price}",
                    Stock.class,
                    Map.of(
                            "index", index,
                            "name", name,
                            "price", price

                    )
            );

            assertThat("Stock creation should have been successful", responseCreate.getStatusCode(), is(equalTo(HttpStatus.OK)));
            assertThat("API call should have returned body", responseCreate.getBody(), is(not(nullValue())));
            assertThat("Created stock should have the same index as provided", responseCreate.getBody().getIndex(), is(equalTo(index)));
            assertThat("Created stock should have the same name as provided", responseCreate.getBody().getName(), is(equalTo(name)));
            assertThat("Created stock should have the same price as provided", responseCreate.getBody().getPrice(), is(equalTo(price)));
            assertThat("Created stock should have zero availability after creation", responseCreate.getBody().getAvailableAmount(), is(equalTo(0L)));
            assertThat("Stock should have an ID after creation", responseCreate.getBody().getId(), is(not(equalTo(nullValue()))));

            final long amount = random.nextLong(1L, 100);

            final ResponseEntity<Stock> responseUpdate = restTemplate.getForEntity(
                    apiPrefix + "increaseStockAmount?index={index}&amount={amount}",
                    Stock.class,
                    Map.of(
                            "index", index,
                            "amount", amount
                    )
            );

            assertThat("Stock creation should have been successful", responseUpdate.getStatusCode(), is(equalTo(HttpStatus.OK)));
            assertThat("API call should have returned body", responseUpdate.getBody(), is(not(nullValue())));
            assertThat("Stock should have the same price", responseUpdate.getBody().getPrice(), is(equalTo(price)));
            assertThat("Updated stock should have the same index as provided", responseUpdate.getBody().getIndex(), is(equalTo(index)));
            assertThat("Updated stock should have the same name as provided", responseUpdate.getBody().getName(), is(equalTo(name)));
            assertThat("Updated stock should have required availability after an update", responseUpdate.getBody().getAvailableAmount(), is(equalTo(amount)));
            assertThat("Stock should have the same ID after price update", responseUpdate.getBody().getId(), is(equalTo(responseCreate.getBody().getId())));
            stocks.add(responseUpdate.getBody());
        }

        for (int i = 0; i < userCount; i++) {
            final ResponseEntity<User> responseGet = restTemplate.getForEntity(
                    apiPrefix + "getUser?login={login}",
                    User.class,
                    Map.of(
                            "login", logins[i]
                    )
            );
            assertThat("Registration should have been successful", responseGet.getStatusCode(), is(equalTo(HttpStatus.OK)));
            assertThat("API call should have returned body", responseGet.getBody(), is(not(nullValue())));
            assertThat("Created user should have the same login as provided", responseGet.getBody().getLogin(), is(equalTo(logins[i])));
            users.set(i, responseGet.getBody());
        }


        for (int i = 0; i < testCount; i++) {
            if (random.nextBoolean()) {
                // Try&Buy stock
                final int userId = random.nextInt(userCount);
                final int stockId = random.nextInt(stockCount);
                final String login = logins[userId];
                final String index = indices[stockId];
                final Stock stock = stocks.get(stockId);
                final User user = users.get(userId);
                final long buyAmount = random.nextLong(1, 100);

                final StockOperationRequestDTO operation = StockOperationRequestDTO
                        .builder()
                        .userLogin(logins[userId])
                        .stockIndex(indices[stockId])
                        .type(StockOperationType.BUY)
                        .amount(buyAmount)
                        .build();

                if (users.get(userId).getBalance() >= stocks.get(stockId).getPrice() * buyAmount && stocks.get(stockId).getAvailableAmount() >= buyAmount) {
                    final ResponseEntity<StockOperationResponseDTO> responseUpdate = restTemplate.postForEntity(
                            apiPrefix + "stockOp",
                            operation,
                            StockOperationResponseDTO.class
                    );

                    assertThat("Balance update should have been successful", responseUpdate.getStatusCode(), is(equalTo(HttpStatus.OK)));
                    assertThat("API call should have returned body", responseUpdate.getBody(), is(not(nullValue())));
                    assertThat("Updated user should have the same login as provided", responseUpdate.getBody().getOperation().getUser().getLogin(), is(equalTo(login)));
                    assertThat("Updated user should have balance changed", responseUpdate.getBody().getOperation().getUser().getBalance(), is(equalTo(user.getBalance() - stock.getPrice() * buyAmount)));
                    user.setBalance(user.getBalance() - stocks.get(stockId).getPrice() * buyAmount);
                    user.getPortfolio().compute(stock, (s, a) -> (a != null) ? (a + buyAmount) : (buyAmount));


                    final ResponseEntity<Stock> responseDecrease = restTemplate.getForEntity(
                            apiPrefix + "getStock?index={index}",
                            Stock.class,
                            Map.of(
                                    "index", index
                            )
                    );

                    assertThat("API call should have been successful", responseDecrease.getStatusCode(), is(equalTo(HttpStatus.OK)));
                    assertThat("API call should have returned body", responseDecrease.getBody(), is(not(nullValue())));
                    assertThat("Stock should have the same index as provided", responseDecrease.getBody().getIndex(), is(equalTo(index)));
                    assertThat("Stock availability should have been decreased", responseDecrease.getBody().getAvailableAmount(), is(equalTo(stocks.get(stockId).getAvailableAmount() - buyAmount)));
                    assertThat("Stock should not have changed", responseDecrease.getBody(), is(equalTo(responseUpdate.getBody().getOperation().getStock())));
                    stocks.set(stockId, responseUpdate.getBody().getOperation().getStock());
                } else {
                    try {
                        final ResponseEntity<User> responseUpdate = restTemplate.postForEntity(
                                apiPrefix + "stockOp",
                                operation,
                                User.class
                        );
                        fail("Sell operation should not be possible");
                    } catch (final Exception e) {
                        // Ok
                    }
                }
            } else {
                // Sell stock
                final int userId = random.nextInt(userCount);
                final int stockId = random.nextInt(stockCount);
                final String login = logins[userId];
                final String index = indices[stockId];
                final long sellAmount = random.nextLong(1, 100);
                final Stock stock = stocks.get(stockId);
                final User user = users.get(userId);

                final StockOperationRequestDTO operation = StockOperationRequestDTO
                        .builder()
                        .userLogin(login)
                        .stockIndex(index)
                        .type(StockOperationType.SELL)
                        .amount(sellAmount)
                        .build();

                if (user.getPortfolio().containsKey(stock) && user.getPortfolio().get(stock) >= sellAmount) {
                    final ResponseEntity<StockOperationResponseDTO> responseUpdate = restTemplate.postForEntity(
                            apiPrefix + "stockOp",
                            operation,
                            StockOperationResponseDTO.class
                    );

                    assertThat("Balance update should have been successful", responseUpdate.getStatusCode(), is(equalTo(HttpStatus.OK)));
                    assertThat("API call should have returned body", responseUpdate.getBody(), is(not(nullValue())));
                    assertThat("Updated user should have the same login as provided", responseUpdate.getBody().getOperation().getUser().getLogin(), is(equalTo(login)));
                    assertThat("Updated user should have balance changed", responseUpdate.getBody().getOperation().getUser().getBalance(), is(equalTo(user.getBalance() + stock.getPrice() * sellAmount)));
                    user.setBalance(user.getBalance() + stocks.get(stockId).getPrice() * sellAmount);
                    user.getPortfolio().compute(stock, (s, a) -> (a > sellAmount) ? (a - sellAmount) : null);


                    final ResponseEntity<Stock> responseDecrease = restTemplate.getForEntity(
                            apiPrefix + "getStock?index={index}",
                            Stock.class,
                            Map.of(
                                    "index", index
                            )
                    );

                    assertThat("API call should have been successful", responseDecrease.getStatusCode(), is(equalTo(HttpStatus.OK)));
                    assertThat("API call should have returned body", responseDecrease.getBody(), is(not(nullValue())));
                    assertThat("Stock should have the same index as provided", responseDecrease.getBody().getIndex(), is(equalTo(index)));
                    assertThat("Stock availability should have been decreased", responseDecrease.getBody().getAvailableAmount(), is(equalTo(stocks.get(stockId).getAvailableAmount() + sellAmount)));
                    assertThat("Stock should not have changed", responseDecrease.getBody(), is(equalTo(responseUpdate.getBody().getOperation().getStock())));
                    stocks.set(stockId, responseUpdate.getBody().getOperation().getStock());
                } else {
                    try {
                        final ResponseEntity<User> responseUpdate = restTemplate.postForEntity(
                                apiPrefix + "stockOp",
                                operation,
                                User.class
                        );
                        fail("Sell operation should not be possible: " + operation.toString());
                    } catch (final Exception e) {
                        // Ok
                    }
                }
            }

            // Refresh stock prices
            for (int j = 0; j < stockCount; j++) {
                if (random.nextBoolean()) {
                    restTemplate.getForEntity(
                            apiPrefix + "updateStockPrice?index={index}&price={price}",
                            Stock.class,
                            Map.of(
                                    "index", indices[j],
                                    "price", Long.max(stocks.get(j).getPrice() + random.nextLong(-100, 100), 1L)
                            )
                    );

                    final var result = restTemplate.getForEntity(
                            apiPrefix + "increaseStockAmount?index={index}&amount={amount}",
                            Stock.class,
                            Map.of(
                                    "index", indices[j],
                                    "amount", random.nextLong(1, 10)
                            )
                    );

                    stocks.set(j, result.getBody());
                }
            }
        }


        for (int i = 0; i < userCount; i++) {
            final ResponseEntity<Long> responseAudit = restTemplate.getForEntity(
                    apiPrefix + "getTotalUserActivesPrice?login={login}",
                    Long.class,
                    Map.of(
                            "login", users.get(i).getLogin()
                    )
            );
            assertThat("Stock creation should have been successful", responseAudit.getStatusCode(), is(equalTo(HttpStatus.OK)));
            assertThat("API call should have returned body", responseAudit.getBody(), is(not(nullValue())));
            long totalAmount = users.get(i).getBalance();
            for (final Map.Entry<Stock, Long> stockLongEntry : users.get(i).getPortfolio().entrySet()) {
                final Stock posessedStock = stockLongEntry.getKey();
                final long amount = stockLongEntry.getValue();
                final ResponseEntity<Stock> responseQuery = restTemplate.getForEntity(
                        apiPrefix + "getStock?index={index}",
                        Stock.class,
                        Map.of(
                                "index", posessedStock.getIndex()
                        )
                );
                assertThat("Stock creation should have been successful", responseQuery.getStatusCode(), is(equalTo(HttpStatus.OK)));
                assertThat("API call should have returned body", responseQuery.getBody(), is(not(nullValue())));
                totalAmount += responseQuery.getBody().getPrice() * amount;
            }
            assertThat("Total balance should be correct", responseAudit.getBody(), is(equalTo(totalAmount)));
        }


    }

}
