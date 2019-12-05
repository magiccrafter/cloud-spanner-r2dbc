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
import com.google.cloud.spanner.r2dbc.SpannerConnection;
import com.google.cloud.spanner.r2dbc.SpannerStatement;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import io.r2dbc.spi.Option;
import java.time.Duration;
import java.util.List;
import reactor.blockhound.BlockHound;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 *
 */
public class Tmp {

  private static final ConnectionFactory connectionFactory =
      ConnectionFactories.get(ConnectionFactoryOptions.builder()
          // TODO: consider whether to bring autodiscovery of project ID
          .option(Option.valueOf("project"), ServiceOptions.getDefaultProjectId())
          .option(DRIVER, DRIVER_NAME)
          .option(INSTANCE, DatabaseProperties.INSTANCE)
          .option(DATABASE, DatabaseProperties.DATABASE)
          .build());

  public static void main(String[] args) {

/*
    BlockHound.builder()
        .disallowBlockingCallsInside ("com.google.cloud.spanner.r2dbc.SpannerStatement", "execute")
        .nonBlockingThreadPredicate(current -> {
          return current.or(it -> {
            return it.getName().startsWith("parallel");
          });
        })
        .blockingMethodCallback(it -> {
          System.out.println("FINALLY! An error!");
        })
        .install();

*/

    BlockHound.install(builder -> {
      builder
          .nonBlockingThreadPredicate(current -> current.or(thread -> thread.getName().startsWith("grpc-default-executor")))
          .allowBlockingCallsInside("sun.security.ssl.SSLContextImpl", "engineInit")
          .allowBlockingCallsInside("sun.security.ssl.SSLSocketImpl", "startHandshake")
      .allowBlockingCallsInside("sun.security.ssl.SSLSocketImpl", "writeRecord")
      .allowBlockingCallsInside("com.google.auth.Credentials", "blockingGetToCallback")
      .blockingMethodCallback(bm -> System.out.println("Blocking method: " + bm.getName()));



      //builder.disallowBlockingCallsInside("com.google.cloud.spanner.r2dbc.SpannerStatement", "execute");
    });


        Long result = Mono.delay(Duration.ofSeconds(1))
            .flatMap(unused -> Mono.from(connectionFactory.create()))
            .flatMapMany(c -> c.createStatement("SELECT 1").execute())
            .flatMap(res -> res.map((r,m) -> (long)r.get(0)))
            /*.doOnNext(it -> {
              try {
                System.out.println("Sleep 3: " + Thread.currentThread().getName());
                Thread.sleep(10);
              }
              catch (InterruptedException e) {
                throw new RuntimeException(e);
              }
            })*/
    //        .subscribeOn(Schedulers.newSingle("blocking-thread"))
/*            .doOnNext(a -> {
              System.out.println("I hope I am a blocking thread: " + Thread.currentThread().getName());
            })*/
            .blockLast();

        System.out.println("Result = " + result);

 /*   Mono.delay(Duration.ofSeconds(1))
        .doOnNext(it -> {
          try {
            Thread.sleep(10);
          }
          catch (InterruptedException e) {
            throw new RuntimeException(e);
          }
        })
        .block();
*/
  }
}
