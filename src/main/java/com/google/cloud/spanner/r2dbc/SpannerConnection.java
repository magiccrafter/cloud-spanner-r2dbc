package com.google.cloud.spanner.r2dbc;

import com.google.cloud.spanner.DatabaseClient;
import com.google.cloud.spanner.TransactionContext;
import com.google.cloud.spanner.TransactionManager;
import io.r2dbc.spi.Batch;
import io.r2dbc.spi.Connection;
import io.r2dbc.spi.IsolationLevel;
import io.r2dbc.spi.Statement;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Function;

public class SpannerConnection implements Connection {

  private final DatabaseClient databaseClient;

  private final TransactionManager transactionManager;

  private TransactionContext currentTransactionContext = null;

  public SpannerConnection(DatabaseClient databaseClient) {
    this.databaseClient = databaseClient;
    this.transactionManager = databaseClient.transactionManager();
  }

  @Override
  public Publisher<Void> beginTransaction() {
    return Mono.create(sink -> {
      if (currentTransactionContext == null) {
        currentTransactionContext = transactionManager.begin();
      }
      sink.success();
    });
  }

  @Override
  public Publisher<Void> close() {
    return Mono.create(sink -> {
      transactionManager.close();
      sink.success();
    });
  }

  @Override
  public Publisher<Void> commitTransaction() {
    return Mono.create(sink -> {
      if (currentTransactionContext != null) {
        transactionManager.commit();
      }
      sink.success();
    });
  }

  @Override
  public Batch createBatch() {
    return null;
  }

  @Override
  public Statement createStatement(String sql) {
    return null;
  }

  @Override
  public Publisher<Void> rollbackTransaction() {
    return null;
  }

  @Override
  public Publisher<Void> setTransactionIsolationLevel(IsolationLevel isolationLevel) {
    throw new UnsupportedOperationException("Isolation Levels are not supported in Cloud Spanner.");
  }

  @Override
  public Publisher<Void> createSavepoint(String name) {
    throw new UnsupportedOperationException("Save points are not supported in Cloud Spanner.");
  }

  @Override
  public Publisher<Void> releaseSavepoint(String name) {
    throw new UnsupportedOperationException("Save points are not supported in Cloud Spanner.");
  }

  @Override
  public Publisher<Void> rollbackTransactionToSavepoint(String name) {
    throw new UnsupportedOperationException("Save points are not supported in Cloud Spanner.");
  }

  private Mono<Void> useTransactionStatus(Function<TransactionManager.TransactionState, Publisher<?>> f) {
    return Flux.defer(() -> f.apply(this.transactionManager.getState())).then();
  }
}
