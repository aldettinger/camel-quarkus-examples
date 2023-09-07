/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.acme.jpa.idempotent.repository;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.processor.idempotent.jpa.JpaMessageIdRepository;
import org.apache.camel.processor.idempotent.jpa.MessageProcessed;

import static org.apache.camel.processor.idempotent.jpa.JpaMessageIdRepository.jpaMessageIdRepository;

import jakarta.enterprise.inject.Produces;
import jakarta.inject.Named;
import jakarta.persistence.EntityManagerFactory;

public class Routes extends RouteBuilder {

    @Produces
    @Named
    JpaMessageIdRepository jpaIdempotentRepository(EntityManagerFactory entityManagerFactory) {
        /**
         * The JPA based idempotent repository will ensure that the same message is consume only once.
         * Under the hood, all entries will be marked with processorName = myProcessorName.
         * It's possible to have multiple processor names, each processor are individual, don't filter messages for others.
         * @TODO: check that this is true and find a better way to explain it.
         */
        return jpaMessageIdRepository(entityManagerFactory, "myProcessorName");
    }

    @Override
    public void configure() throws Exception {
        from("direct:in")
                .log("Received an incoming message with header ID having value ${headers.ID}")
                .idempotentConsumer(simple("${headers.ID}")).idempotentRepository("jpaIdempotentRepository")
                .log("The incoming message was not a doublon");

        // Display the state of the MessageProcessed table
        // @TODO: Not sure those implementation details are welcome in an example
        fromF("jpa:%s?consumeDelete=false", MessageProcessed.class.getName())
            .log("From-Jpa found: ${body}");
    }
}
