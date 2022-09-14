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

import io.dropwizard.hibernate.UnitOfWork;
import nl.knaw.dans.dku.api.OverdueTodosView;
import nl.knaw.dans.dku.db.TodoItemDao;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.time.OffsetDateTime;

// An example of a resource that uses a html template
@Path("/overdue")
@Produces({ MediaType.TEXT_HTML, MediaType.APPLICATION_JSON })
public class OverdueTodosResource {

    private final TodoItemDao todoItemDao;

    public OverdueTodosResource(TodoItemDao todoItemDao) {
        this.todoItemDao = todoItemDao;
    }

    @GET
    @UnitOfWork
    public OverdueTodosView index() {
        var todoItems = todoItemDao.findDueDateBefore(OffsetDateTime.now());
        return new OverdueTodosView(todoItems);
    }

}
