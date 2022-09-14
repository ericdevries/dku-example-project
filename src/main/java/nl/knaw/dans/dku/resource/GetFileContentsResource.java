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

import javax.validation.constraints.NotEmpty;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.io.FileNotFoundException;
import java.io.IOException;

// TODO make this
// HINT: to open a file, use this:
// InputStream inputStream = getClass().getClassLoader().getResource("file.txt").openStream();
@Path("/file")
public class GetFileContentsResource {

    @GET
    public Response getFileContents(@NotEmpty @QueryParam("filename") String filename) {
        try {
            var resource = getClass().getClassLoader().getResource(filename);

            if (resource == null) {
                throw new FileNotFoundException("File not found");
            }

            return Response.ok(resource.openStream()).build();
        }
        catch (FileNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        catch (IOException e) {
            return Response.serverError().entity("Error: " + e.getMessage()).build();
        }
    }
}
