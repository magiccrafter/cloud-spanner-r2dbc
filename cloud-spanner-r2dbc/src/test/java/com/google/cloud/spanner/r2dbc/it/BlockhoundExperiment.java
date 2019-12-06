/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.cloud.spanner.r2dbc.it;

import static com.google.cloud.spanner.r2dbc.SpannerConnectionFactoryProvider.DRIVER_NAME;
import static com.google.cloud.spanner.r2dbc.SpannerConnectionFactoryProvider.INSTANCE;
import static io.r2dbc.spi.ConnectionFactoryOptions.DATABASE;
import static io.r2dbc.spi.ConnectionFactoryOptions.DRIVER;

import com.google.cloud.ServiceOptions;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import io.r2dbc.spi.Option;
import java.time.Duration;
import reactor.blockhound.BlockHound;
import reactor.core.publisher.Mono;

/**
 * Trying blockhound integration as a standalone experiment.
 */
public class BlockhoundExperiment {

  private static final ConnectionFactory connectionFactory =
      ConnectionFactories.get(ConnectionFactoryOptions.builder()
          .option(Option.valueOf("project"), ServiceOptions.getDefaultProjectId())
          .option(DRIVER, DRIVER_NAME)
          .option(INSTANCE, DatabaseProperties.INSTANCE)
          .option(DATABASE, DatabaseProperties.DATABASE)
          .build());

  public static void main(String[] args) {

    BlockHound.install(builder -> {
      builder
          .nonBlockingThreadPredicate(current -> current.or(thread -> thread.getName().startsWith("grpc-default-executor")))
          .allowBlockingCallsInside("sun.security.ssl.SSLContextImpl", "engineInit")
          .allowBlockingCallsInside("sun.security.ssl.SSLSocketImpl", "startHandshake")
          .allowBlockingCallsInside("sun.security.ssl.SSLSocketImpl", "writeRecord")
          .allowBlockingCallsInside("com.google.auth.Credentials", "blockingGetToCallback")
          .blockingMethodCallback(bm -> System.out.println("Blocking method: " + bm.getName()));
    });


    Long result = Mono.delay(Duration.ofSeconds(1))
        .flatMap(unused -> Mono.from(connectionFactory.create()))
        .flatMapMany(c -> c.createStatement("SELECT 1").execute())
        .flatMap(res -> res.map((r,m) -> (long)r.get(0)))
        .blockLast();

    System.out.println("Result = " + result);

  }
}
