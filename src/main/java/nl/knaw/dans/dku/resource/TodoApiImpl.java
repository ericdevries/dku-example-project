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
import nl.knaw.dans.dku.api.TodoDto;
import nl.knaw.dans.dku.db.TodoItem;
import nl.knaw.dans.dku.db.TodoItemDao;

import javax.validation.Valid;
import javax.ws.rs.NotFoundException;
import java.util.List;
import java.util.stream.Collectors;

public class TodoApiImpl implements TodoApi {
    private final TodoItemDao todoItemDao;

    public TodoApiImpl(TodoItemDao todoItemDao) {
        this.todoItemDao = todoItemDao;
    }

    @Override
    // We add the UnitOfWork annotation to let dropwizard know we want this whole method
    // wrapped inside a hibernate transaction
    @UnitOfWork
    public TodoDto createTodo(@Valid TodoDto todoDto) {
        // Here we create the business object, which is not the same as the DTO (or value object)
        // that we receive from the API
        // Note that there are tools to automate these kind of actions, but for the
        // sake of this demo that would make it too complex
        var todoItem = new TodoItem();
        todoItem.setDueDate(todoDto.getDueDate());
        todoItem.setTitle(todoDto.getTitle());

        var result = todoItemDao.createTodoItem(todoItem);

        return new TodoDto()
            .id(result.getId())
            .title(result.getTitle())
            .dueDate(result.getDueDate());
    }

    @Override
    @UnitOfWork
    public Long deleteTodo(Long id) {
        return todoItemDao.findById(id)
            .map(todoItem -> {
                todoItemDao.deleteTodoItem(todoItem);
                return todoItem.getId();
            })
            .orElseThrow(() -> new NotFoundException(String.format("TODO with id %s not found", id)));

    }

    @Override
    @UnitOfWork
    public List<TodoDto> getAllTodos() {

        return todoItemDao.findAll()
            .stream()
            .map(todo -> new TodoDto()
                .id(todo.getId())
                .title(todo.getTitle())
                .dueDate(todo.getDueDate())
            )
            .collect(Collectors.toList());
    }

    @Override
    @UnitOfWork
    public TodoDto getTodoById(Long id) {

        return todoItemDao.findById(id)
            .map(todo -> new TodoDto()
                .id(todo.getId())
                .title(todo.getTitle())
                .dueDate(todo.getDueDate()))
            .orElseThrow(() -> new NotFoundException(String.format("TODO with id %s not found", id)));
    }

    @Override
    @UnitOfWork
    public TodoDto updateTodo(Long id, TodoDto todoDto) {

        return todoItemDao.findById(id)
            .map(todoItem -> {
                todoItem.setTitle(todoDto.getTitle());
                todoItem.setDueDate(todoDto.getDueDate());

                return todoItemDao.save(todoItem);
            })
            .map(todo -> new TodoDto()
                .id(todo.getId())
                .title(todo.getTitle())
                .dueDate(todo.getDueDate())
            )
            .orElseThrow(() -> new NotFoundException(String.format("TODO with id %s not found", id)));
    }
}
