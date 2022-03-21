package ru.ifmo.rain.serdiukov.integration.exchange.interfaces.rest;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.ifmo.rain.serdiukov.integration.exchange.domain.commands.StockOperation;
import ru.ifmo.rain.serdiukov.integration.exchange.domain.model.Stock;
import ru.ifmo.rain.serdiukov.integration.exchange.domain.model.User;
import ru.ifmo.rain.serdiukov.integration.exchange.interfaces.dto.StockOperationRequestDTO;
import ru.ifmo.rain.serdiukov.integration.exchange.interfaces.dto.StockOperationResponseDTO;
import ru.ifmo.rain.serdiukov.integration.exchange.service.StockService;
import ru.ifmo.rain.serdiukov.integration.exchange.service.TransactionService;
import ru.ifmo.rain.serdiukov.integration.exchange.service.UserService;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.NoSuchElementException;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/1")
public class ExchangeController {
    private final @NotNull @NonNull UserService userService;
    private final @NotNull @NonNull StockService stockService;
    private final @NotNull @NonNull TransactionService transactionService;

    @GetMapping("createUser")
    @ResponseStatus(HttpStatus.OK)
    public @NotNull @NonNull User createUser(final @NotNull @NonNull @RequestParam("login") String login, final @NotNull @NonNull @RequestParam("name") String name) {
        final User user = new User();
        user.setLogin(login);
        user.setName(name);
        user.setPortfolio(new HashMap<>());
        return userService.registerUser(user);
    }

    @GetMapping("createStock")
    @ResponseStatus(HttpStatus.OK)
    public @NotNull @NonNull Stock createStock(final @NotNull @NonNull @RequestParam("index") String index, final @NotNull @NonNull @RequestParam("name") String name, final @RequestParam("price") long price) {
        final @NotNull @NonNull Stock stock = Stock
                .builder()
                .index(index)
                .name(name)
                .price(price)
                .availableAmount(0)
                .build();
        return stockService.createStock(stock);
    }

    @GetMapping("getUser")
    @ResponseStatus(HttpStatus.OK)
    public @NotNull @NonNull User getUser(final @NotNull @NonNull @RequestParam("login") String login) {
        return userService.findByLogin(login).orElseThrow(() -> new NoSuchElementException("Cannot find user with login = " + login));
    }

    @GetMapping("getTotalUserActivesPrice")
    @ResponseStatus(HttpStatus.OK)
    public long getTotalUserActives(final @NotNull @NonNull @RequestParam("login") String login) {
        return userService.getTotalUserActivesPrice(login);
    }

    @GetMapping("getStock")
    @ResponseStatus(HttpStatus.OK)
    public @NotNull @NonNull Stock getStock(final @NotNull @NonNull @RequestParam("index") String index) {
        return stockService.findByIndex(index).orElseThrow(() -> new NoSuchElementException("Cannot find stock with index = " + index));
    }

    @GetMapping("updateStockPrice")
    @ResponseStatus(HttpStatus.OK)
    public @NotNull @NonNull Stock updateStockPrice(final @NotNull @NonNull @RequestParam("index") String index, final @RequestParam("price") long newPrice) {
        return stockService.updateStockPrice(index, newPrice);
    }

    @GetMapping("increaseStockAmount")
    @ResponseStatus(HttpStatus.OK)
    public @NotNull @NonNull Stock increaseStockAmount(final @NotNull @NonNull @RequestParam("index") String index, final @RequestParam("amount") long amount) {
        return stockService.increaseStockAmount(index, amount);
    }

    @GetMapping("topUp")
    @ResponseStatus(HttpStatus.OK)
    public @NotNull @NonNull User topUp(final @NotNull @NonNull @RequestParam("login") String login, final @RequestParam("amount") long amount) {
        return userService.topUpBalance(login, amount);
    }

    @PostMapping("stockOp")
    @ResponseStatus(HttpStatus.OK)
    public @NotNull @NonNull StockOperationResponseDTO performStockOperation(final @NotNull @NonNull @RequestBody StockOperationRequestDTO request) {
        final @NotNull @NonNull Stock stock = stockService.findByIndex(request.getStockIndex()).orElseThrow(() -> new NoSuchElementException("Cannot find requested stock with index " + request.getStockIndex()));
        final @NotNull @NonNull User user = userService.findByLogin(request.getUserLogin()).orElseThrow(() -> new NoSuchElementException("Cannot find user with login " + request.getUserLogin()));
        final StockOperation operation = StockOperation
                .builder()
                .type(request.getType())
                .stock(stock)
                .user(user)
                .amount(request.getAmount())
                .build();
        return transactionService.performStockOperation(operation);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleError(final HttpServletRequest req, final @NotNull @NonNull IllegalArgumentException ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleError(final HttpServletRequest req, final @NotNull @NonNull NoSuchElementException ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public String handleError(final HttpServletRequest req, final @NotNull @NonNull RuntimeException ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleError(final HttpServletRequest req, final @NotNull @NonNull Exception ex) {
        return ex.getMessage();
    }
}
