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

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.apache.camel.ProducerTemplate;
import org.junit.jupiter.api.Test;

@QuarkusTest
@QuarkusTestResource(JpaIdempotentRepositoryTestResource.class)
public class JpaIdempotentRepositoryTest {

    @Inject
    ProducerTemplate template;

    @Test
    public void messagesWithSameHeaderShouldBeConsumedOnlyOnce() {
        template.sendBodyAndHeader("direct:in", "body1", "ID", "id-1");
        template.sendBodyAndHeader("direct:in", "body2", "ID", "id-2");

        template.sendBodyAndHeader("direct:in", "body3", "ID", "id-1");
        template.sendBodyAndHeader("direct:in", "body4", "ID", "id-2");

        try {
            Thread.sleep(5*1000L);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
