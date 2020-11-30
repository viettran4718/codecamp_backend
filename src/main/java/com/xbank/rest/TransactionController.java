package com.xbank.rest;

import com.xbank.config.Constants;
import com.xbank.domain.Transaction;
import com.xbank.dto.TransactionDTO;
import com.xbank.dto.UserDTO;
import com.xbank.security.AuthoritiesConstants;
import com.xbank.service.TransactionService;
import io.github.jhipster.web.util.HeaderUtil;
import io.github.jhipster.web.util.PaginationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

/**
 * REST controller for managing the Transaction.
 */
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Value("${clientApp.name}")
    private String applicationName;
    private final Logger log = LoggerFactory.getLogger(TransactionController.class);
    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
//    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public Mono<ResponseEntity<Transaction>> createTransaction(@Valid @RequestBody TransactionDTO transactionDTO) {
        log.debug("REST request to check if the current user is authenticated");
        return transactionService.createTransaction(transactionDTO).map(tran -> {
            try {
                return ResponseEntity.created(new URI("/api/users/" + tran.getId()))
                        .headers(HeaderUtil.createAlert(applicationName, "TransactionManagement.created", String.valueOf(tran.getId())))
                        .body(tran);
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @PutMapping
//    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public Mono<ResponseEntity<Transaction>> editTransaction(@Valid @RequestBody Transaction transactionDTO) {
        return transactionService.editTransaction(transactionDTO).map(tran -> {
            try {
                return ResponseEntity.created(new URI("/api/users/" + tran.getId()))
                        .headers(HeaderUtil.createAlert(applicationName, "TransactionManagement.edit", String.valueOf(tran.getId())))
                        .body(tran);
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @DeleteMapping("/{id:" + Constants.ID_REGEX + "}")
//    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    @ResponseStatus(code = HttpStatus.OK)
    public Mono<ResponseEntity<Void>> deleteTransaction(@PathVariable long id) {
        log.debug("REST request to delete Transaction: {}", id);
        return transactionService.deleteTransaction(id)
                .map(it -> ResponseEntity.noContent().headers(HeaderUtil.createAlert( applicationName, "deleteTransaction.deleted", String.valueOf(id))).build());
    }
    /**
     * {@code GET /transactions} : get all transactions.
     *
     * @param request  a {@link ServerHttpRequest} request.
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body all transactions.
     */
    @GetMapping
    public Mono<ResponseEntity<Flux<Transaction>>> getAllTransactions(ServerHttpRequest request, Pageable pageable) {
        return transactionService.countTransactions()
                .map(total -> new PageImpl<>(new ArrayList<>(), pageable, total))
                .map(page -> ResponseEntity.ok().headers(PaginationUtil.generatePaginationHttpHeaders(UriComponentsBuilder.fromHttpRequest(request), page))
                        .body(transactionService.getAllTransactions(pageable)));
    }
}
