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
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

@Component
@Slf4j
class RequestLoggingFilter extends OncePerRequestFilter implements Ordered {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            // Include request attributes.
            MDC.put("req.uri", request.getRequestURI());
            MDC.put("req.userAgent", request.getHeader("User-Agent"));
            MDC.put("req.xForwardedFor", request.getHeader("X-Forwarded-For"));
            filterChain.doFilter(request, response);
        } catch (Throwable ex) {
            // This request failed: write a log entry including stack trace as attribute.
            final StringWriter sw = new StringWriter(1024);
            ex.printStackTrace(new PrintWriter(sw, true));
            log.error("Request error", Map.of(
                    "error.message", ex.getMessage(),
                    "error.stacktrace", sw.getBuffer()));
            throw ex;
        } finally {
            // Remove attributes to prevent memory leaks.
            MDC.remove("req.uri");
            MDC.remove("req.userAgent");
            MDC.remove("req.xForwardedFor");
        }
    }

    @Override
    public int getOrder() {
        // Make sure this filter is executed first in the filter chain.
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
