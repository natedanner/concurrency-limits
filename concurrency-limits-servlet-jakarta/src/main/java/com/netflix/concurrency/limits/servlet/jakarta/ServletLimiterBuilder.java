/**
 * Copyright 2023 Netflix, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 *     http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.netflix.concurrency.limits.servlet.jakarta;

import com.netflix.concurrency.limits.Limiter;
import com.netflix.concurrency.limits.limiter.AbstractPartitionedLimiter;

import jakarta.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.Optional;
import java.util.function.Function;

/**
 * Builder to simplify creating a {@link Limiter} specific to a Servlet filter. By default,
 * the same concurrency limit is shared by all requests.  The limiter can be partitioned
 * based on one of many request attributes.  Only one type of partition may be specified.
 */
public final class ServletLimiterBuilder extends AbstractPartitionedLimiter.Builder<ServletLimiterBuilder, HttpServletRequest> {
    /**
     * Partition the limit by header
     * @return Chainable builder
     */
    public ServletLimiterBuilder partitionByHeader(String name) {
        return partitionResolver(request -> Optional.ofNullable(request.getHeader(name)).orElse(null));
    }

    /**
     * Partition the limit by {@link Principal}. Percentages of the limit are partitioned to named
     * groups.  Group membership is derived from the provided mapping function.
     * @param principalToGroup Mapping function from {@link Principal} to a named group.
     * @return Chainable builder
     */
    public ServletLimiterBuilder partitionByUserPrincipal(Function<Principal, String> principalToGroup) {
        return partitionResolver(request -> Optional.ofNullable(request.getUserPrincipal()).map(principalToGroup).orElse(null));
    }

    /**
     * Partition the limit by request attribute
     * @return Chainable builder
     */
    public ServletLimiterBuilder partitionByAttribute(String name) {
        return partitionResolver(request -> Optional.ofNullable(request.getAttribute(name)).map(Object::toString).orElse(null));
    }

    /**
     * Partition the limit by request parameter
     * @return Chainable builder
     */
    public ServletLimiterBuilder partitionByParameter(String name) {
        return partitionResolver(request -> Optional.ofNullable(request.getParameter(name)).orElse(null));
    }

    /**
     * Partition the limit by the full path. Percentages of the limit are partitioned to named
     * groups.  Group membership is derived from the provided mapping function.
     * @param pathToGroup Mapping function from full path to a named group.
     * @return Chainable builder
     */
    public ServletLimiterBuilder partitionByPathInfo(Function<String, String> pathToGroup) {
        return partitionResolver(request -> Optional.ofNullable(request.getPathInfo()).map(pathToGroup).orElse(null));
    }

    /**
     * Bypass limit if the value of the provided header name matches the specified value.
     * @param name The name of the header to check.
     *             This should match exactly with the header name in the {@link HttpServletRequest } context.
     * @param value The value to compare against.
     *              If the value of the header in the context matches this value, the limit will be bypassed.
     * @return Chainable builder
     */
    public ServletLimiterBuilder bypassLimitByHeader(String name, String value) {
        return bypassLimitResolver((context) -> value.equals(context.getHeader(name)));
    }

    /**
     * Bypass limit if the value of the provided attribute name matches the specified value.
     * @param name The name of the attribute to check.
     *             This should match exactly with the attribute name in the {@link HttpServletRequest } context.
     * @param value The value to compare against.
     *              If the value of the attribute in the context matches this value, the limit will be bypassed.
     * @return Chainable builder
     */
    public ServletLimiterBuilder bypassLimitByAttribute(String name, String value) {
        return bypassLimitResolver((context) -> value.equals(context.getAttribute(name).toString()));
    }

    /**
     * Bypass limit if the value of the provided parameter name matches the specified value.
     * @param name The name of the parameter to check.
     *             This should match exactly with the parameter name in the {@link HttpServletRequest } context.
     * @param value The value to compare against.
     *              If the value of the parameter in the context matches this value, the limit will be bypassed.
     * @return Chainable builder
     */
    public ServletLimiterBuilder bypassLimitByParameter(String name, String value) {
        return bypassLimitResolver((context) -> value.equals(context.getParameter(name)));
    }

    /**
     * Bypass limit if the request URI matches the specified URI.
     * @param uri The URI to check against the {@link HttpServletRequest } URI.
     *            If the request's URI matches this URI, the limit will be bypassed.
     * @return Chainable builder
     */
    public ServletLimiterBuilder bypassLimitByRequestUri(String uri) {
        return bypassLimitResolver((context) -> uri.equals(context.getRequestURI()));
    }

    /**
     * Bypass limit if the request method matches the specified method.
     * @param method The HTTP method (e.g. GET, POST, or PUT) to check against the {@link HttpServletRequest } method.
     *               If the request's method matches this method, the limit will be bypassed.
     * @return Chainable builder
     */
    public ServletLimiterBuilder bypassLimitByMethod(String method) {
        return bypassLimitResolver((context) -> method.equals(context.getMethod()));
    }

    @Override
    protected ServletLimiterBuilder self() {
        return this;
    }
}
