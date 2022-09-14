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

package nl.knaw.dans.dku;

import com.fasterxml.jackson.databind.SerializationFeature;
import io.dropwizard.Application;
import io.dropwizard.db.PooledDataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;
import nl.knaw.dans.dku.db.TodoItem;
import nl.knaw.dans.dku.db.TodoItemDao;
import nl.knaw.dans.dku.resource.GetFileContentsResource;
import nl.knaw.dans.dku.resource.OverdueTodosResource;
import nl.knaw.dans.dku.resource.TodoApiImpl;

public class DkuExampleProjectApplication extends Application<DkuExampleProjectConfiguration> {

    private final HibernateBundle<DkuExampleProjectConfiguration> hibernateBundle = new HibernateBundle<>(TodoItem.class) {

        @Override
        public PooledDataSourceFactory getDataSourceFactory(DkuExampleProjectConfiguration configuration) {
            return configuration.getDataSourceFactory();
        }
    };

    public static void main(final String[] args) throws Exception {
        new DkuExampleProjectApplication().run(args);
    }

    @Override
    public String getName() {
        return "Dku Example Project";
    }

    @Override
    public void initialize(final Bootstrap<DkuExampleProjectConfiguration> bootstrap) {
        bootstrap.addBundle(hibernateBundle);
        bootstrap.getObjectMapper().disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        bootstrap.addBundle(new ViewBundle<>());
    }

    @Override
    public void run(final DkuExampleProjectConfiguration configuration, final Environment environment) {
        // here we create our dependencies and inject them into the other dependencies if required
        var todoItemDao = new TodoItemDao(hibernateBundle.getSessionFactory());
        var todoApi = new TodoApiImpl(todoItemDao);

        var overdueTodoResource = new OverdueTodosResource(todoItemDao);

        environment.jersey().register(todoApi);
        environment.jersey().register(overdueTodoResource);
        // you can also directly instantiate the object, whatever you prefer
        environment.jersey().register(new GetFileContentsResource());
    }

}
