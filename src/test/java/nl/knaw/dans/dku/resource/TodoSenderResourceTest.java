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

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;
import nl.knaw.dans.dku.api.TodoSenderResponse;
import nl.knaw.dans.dku.core.SmsNotificationService;
import nl.knaw.dans.dku.db.TodoItem;
import nl.knaw.dans.dku.db.TodoItemDao;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import javax.ws.rs.client.Entity;
import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// This annotation sets up the right environment for the resource tests
@ExtendWith(DropwizardExtensionsSupport.class)
class TodoSenderResourceTest {
    // Mocking an object means we create a fake object that looks like the one
    // we are mocking, but without an actual implementation
    private static final SmsNotificationService smsNotificationService = Mockito.mock(SmsNotificationService.class);
    private static final TodoItemDao todoItemDao = Mockito.mock(TodoItemDao.class);
    // Here we do not create the entire application, but just one resource.
    // Dropwizard will then set up an environment that allows us to test it easily
    private static final ResourceExtension EXT = ResourceExtension.builder()
        .addResource(new TodoSenderResource(smsNotificationService, todoItemDao))
        .build();

    // our test object to be used during testing
    private TodoItem todoItem;

    @BeforeEach
    void setup() {
        // create a new todo item that is never actually stored in a database
        todoItem = new TodoItem();
        todoItem.setId(1L);
        todoItem.setTitle("Do groceries");
    }

    @AfterEach
    void tearDown() {
        // reset the mocks, which allows us to re-use them during the other tests
        Mockito.reset(smsNotificationService);
        Mockito.reset(todoItemDao);
    }

    @Test
    void notifySomebodySuccessfully() throws Exception {
        // when this method is called, return our fake todo item
        Mockito.when(todoItemDao.findById(1L)).thenReturn(Optional.of(todoItem));

        var response = EXT.target("/todo/notify/1")
            .request()
            .post(Entity.json(null), TodoSenderResponse.class);

        // verify the method called the SMS notification service
        Mockito.verify(smsNotificationService, Mockito.times(1)).sendOverdueNotification(Mockito.any());

        // assert our response is correct
        assertTrue(response.isOk());
    }

    @Test
    void notifySomebodyButTheSmsServiceIsBroken() throws Exception {
        Mockito.when(todoItemDao.findById(1L)).thenReturn(Optional.of(todoItem));

        // here we tell mockito to throw an exception when this method is called
        Mockito.doThrow(IOException.class).when(smsNotificationService).sendOverdueNotification(Mockito.any());

        var response = EXT.target("/todo/notify/1")
            .request()
            .post(Entity.json(null), TodoSenderResponse.class);

        Mockito.verify(smsNotificationService).sendOverdueNotification(Mockito.any());

        assertFalse(response.isOk());
    }
}