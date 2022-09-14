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

import nl.knaw.dans.dku.api.TodoSenderResponse;
import nl.knaw.dans.dku.core.SmsNotificationService;
import nl.knaw.dans.dku.db.TodoItemDao;

import javax.ws.rs.Consumes;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/todo/notify/{id}")
public class TodoSenderResource {
    private final SmsNotificationService smsNotificationService;
    private final TodoItemDao todoItemDao;

    public TodoSenderResource(SmsNotificationService smsNotificationService, TodoItemDao todoItemDao) {
        this.smsNotificationService = smsNotificationService;
        this.todoItemDao = todoItemDao;
    }

    @POST
    @Produces({ MediaType.APPLICATION_JSON })
    public TodoSenderResponse notifySomebody(@PathParam("id") Long id) {
        // find item by ID, or throw an exception if not found
        var todoItem = todoItemDao.findById(id)
            .orElseThrow(() -> new NotFoundException("Todo not found"));

        var response = new TodoSenderResponse();
        response.setTodoId(id);

        try {
            // try to send the SMS
            smsNotificationService.sendOverdueNotification(todoItem);

            response.setMessage("It was sent OK");
            response.setOk(true);
        }
        catch (Exception e) {
            // Something went wrong!
            response.setMessage("There was a problem sending the SMS: " + e.getMessage());
            response.setOk(false);
        }

        return response;
    }
}
