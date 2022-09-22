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
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

// TODO finish this
@ExtendWith(DropwizardExtensionsSupport.class)
class GetFileContentsResourceTest {

    private static DropwizardAppExtension<DkuExampleProjectConfiguration> EXT = new DropwizardAppExtension<>(
        DkuExampleProjectApplication.class,
        ResourceHelpers.resourceFilePath("test-etc/default-config.yml")
    );

    @Test
    void getFileContents() {
        var response = EXT.client()
            .target(
                String.format("http://localhost:%d", EXT.getLocalPort()))
            .path("file")
            .queryParam("filename", "someone")
            .request()
            .get();

        assertEquals(200, response.getStatus());
        assertEquals("Hello someone", response.readEntity(String.class));

        //        assertEquals(2, 2);
    }

}