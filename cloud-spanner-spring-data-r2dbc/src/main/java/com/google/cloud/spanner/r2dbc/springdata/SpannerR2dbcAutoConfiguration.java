/*
 * Copyright 2022-2022 Google LLC
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

package com.google.cloud.spanner.r2dbc.springdata;

import com.google.cloud.spanner.r2dbc.springdata.converter.DateToLocalDateConverter;
import com.google.cloud.spanner.r2dbc.springdata.converter.LocalDateTimeToTimestampConverter;
import com.google.cloud.spanner.r2dbc.springdata.converter.LocalDateToDateConverter;
import com.google.cloud.spanner.r2dbc.springdata.converter.TimestampToLocalDateTimeConverter;
import java.util.Arrays;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.r2dbc.R2dbcDataAutoConfiguration;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
import org.springframework.r2dbc.core.DatabaseClient;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for {@link DatabaseClient}.
 */
@AutoConfigureAfter(R2dbcAutoConfiguration.class)
@AutoConfigureBefore(R2dbcDataAutoConfiguration.class)
public class SpannerR2dbcAutoConfiguration {

  /**
   * Registers with a higher precedence the necessary binders for dealing with date/time data types.
   */
  @Bean
  @ConditionalOnMissingBean
  public R2dbcCustomConversions r2dbcCustomConversions() {
    return R2dbcCustomConversions.of(
        new SpannerR2dbcDialect(),
        Arrays.asList(
            new DateToLocalDateConverter(),
            new LocalDateToDateConverter(),
            new LocalDateTimeToTimestampConverter(),
            new TimestampToLocalDateTimeConverter()
        )
    );
  }
}
