/*
 * Copyright (c) 2020 VMware, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.alexandreroman.demos.springbootstructuredlogging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import java.math.BigDecimal;
import java.util.Map;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

@RestController
class IndexController {
    @GetMapping(value = "/", produces = MediaType.TEXT_PLAIN_VALUE)
    String index() {
        final String faultUrl =
                MvcUriComponentsBuilder.fromMethodName(FaultController.class, "fault")
                        .build().toUriString();
        final String divideUrl =
                MvcUriComponentsBuilder.fromMethodName(DivideOpController.class, "divide",
                        BigDecimal.valueOf(10), BigDecimal.valueOf(2)).build().toUriString();
        return "These endpoints are available:\n" +
                " - " + faultUrl + "\n" +
                " - " + divideUrl + "\n";
    }
}

@RestController
@Slf4j
class FaultController {
    @GetMapping(value = "/fault", produces = MediaType.TEXT_PLAIN_VALUE)
    String fault() {
        log.info("I'm about to throw an error");
        throw new RuntimeException("Bad luck, this is an error");
    }
}

@RestController
@Slf4j
class DivideOpController {
    @GetMapping(value = "/divide/{a}/by/{b}", produces = MediaType.TEXT_PLAIN_VALUE)
    String divide(@PathVariable("a") BigDecimal a, @PathVariable("b") BigDecimal b) {
        // What would happen if we divide by zero?
        final BigDecimal result = a.divide(b);
        log.info("Divide op", Map.of("a", a.toPlainString(), "b", b.toPlainString()));
        return a.toPlainString() + " / " + b.toPlainString() + " = " + result.toPlainString() + "\n";
    }
}

@Configuration
@Slf4j
class EndlessLoggerConfig {
    @Bean
    CommandLineRunner endlessLogger() {
        return (args) -> logForever();
    }

    void logForever() {
        new Thread(() -> {
            // We generate log entries in a background thread, outside of any HTTP requests.
            for (int i = 0; ; ++i) {
                // Add custom attributes to the log entry (as a Map argument).
                log.info("Logging step", Map.of("step", i));
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException ignore) {
                }
            }
        }, "EndlessLogger").start();
    }
}
