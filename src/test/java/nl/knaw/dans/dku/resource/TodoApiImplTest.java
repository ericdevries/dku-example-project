/*
 * Copyright (C) 2022 DANS - Data Archiving and Networked Services (info@dans.knaw.nl)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.knaw.dans.dku.resource;

import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import nl.knaw.dans.dku.DkuExampleProjectApplication;
import nl.knaw.dans.dku.DkuExampleProjectConfiguration;
import nl.knaw.dans.dku.api.TodoDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import java.io.File;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

// We need this annotation so that dropwizard sprinkles some magic on our code
@ExtendWith(DropwizardExtensionsSupport.class)
class TodoApiImplTest {
    // Here we initialize the application with a certain configuration (can be found in src/test/resources/test-etc/)
    private static DropwizardAppExtension<DkuExampleProjectConfiguration> EXT = new DropwizardAppExtension<>(
        DkuExampleProjectApplication.class,
        ResourceHelpers.resourceFilePath("test-etc/default-config.yml")
    );

    @Test
    void testCreateTodo() {
        var payload = new TodoDto()
            .title("Title goes here")
            .dueDate(OffsetDateTime.now())
            .attachment(new File("my/todo/one.txt"));

        // Because the application is actually running, we have to use http to test it
        // and get the randomly generated port from the DropwizardAppExtension.
        // Note that it is still possible to send POJO entities without manually serializing.
        var response = EXT.client()
            .target(
                String.format("http://localhost:%d/todo", EXT.getLocalPort()))
            .request()
            .post(Entity.json(payload));

        assertEquals(200, response.getStatus());

        var dto = response.readEntity(TodoDto.class);
        assertEquals("Title goes here", dto.getTitle());
        assertEquals(new File("my/todo/one.txt").getAbsoluteFile(), dto.getAttachment());
    }

    @Test
    void testListTodos() {
        var payloads = new TodoDto[] {
            new TodoDto().title("Title 1 goes here").dueDate(OffsetDateTime.now().plus(15, ChronoUnit.DAYS)).attachment(new File("my/todo/1.txt")),
            new TodoDto().title("Title 2 goes here").dueDate(OffsetDateTime.now().plus(10, ChronoUnit.DAYS)),
            new TodoDto().title("Title 3 goes here").dueDate(OffsetDateTime.now().plus(23, ChronoUnit.DAYS)).attachment(new File("my/todo/3.txt"))
        };

        // First we create 3 items using the previously tested createTodo function
        for (var payload : payloads) {
            Entity<TodoDto> json = Entity.json(payload);
            try (var r = EXT.client()
                .target(
                    String.format("http://localhost:%d/todo", EXT.getLocalPort()))
                .request()
                .post(json)) {
                // no-op
            }
        }

        // now we test that we get 3 of them back when requesting all of them
        var response = EXT.client().target(
                String.format("http://localhost:%d/todo", EXT.getLocalPort()))
            .request()
            .get(new GenericType<List<TodoDto>>(){});

        assertEquals(3, response.size());
        assertNull( response.get(1).getAttachment());
        assertEquals(new File("my/todo/1.txt").getAbsoluteFile().toString(), (response.get(0)).getAttachment().toString());
    }
}