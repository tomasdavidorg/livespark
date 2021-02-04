/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.uberfire.server;

import java.net.URI;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.uberfire.io.IOService;
import org.uberfire.java.nio.file.Path;
import org.uberfire.server.util.FileServletUtil;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FileDownloadServletTest {

    private static final String PARAM_PATH = "path";

    private static final String TEST_ROOT_PATH = "default://master@test-repository/test-project/src/main/resources/test";

    @Mock
    private IOService ioService;

    @Mock
    private ServletOutputStream servletOutputStream;

    @InjectMocks
    private FileDownloadServlet downloadServlet;

    /**
     * Tests the downloading of a file given the following parameters:
     * <p>
     * 1) the file path on the server side of a file with no blank spaces in the name.
     */
    @Test
    public void downloadByPathWithNoSpaces() throws Exception {

        //test the download of a file name with no blank spaces.
        String fileName = "FileNameWithNoSpaces.someextension";
        String fileContent = "the local file content";

        doDownloadByPath(TEST_ROOT_PATH,
                         fileName,
                         fileContent);
    }

    @Test
    public void testMakeURI() throws Exception {

        final String pathURI = "default://master@MySpace/aa/src/main/resources/com/myspace/aa/vv vv.drl";
        final URI uri = downloadServlet.makeURI(pathURI);

        assertEquals("/aa/src/main/resources/com/myspace/aa/vv+vv.drl", uri.getRawPath());
    }

    private void doDownloadByPath(String sourceFolder,
                                  String sourceFileName,
                                  String fileContent) throws Exception {

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        String sourcePath = sourceFolder + "/" + sourceFileName;

        //mock the servlet parameters
        when(request.getParameter(PARAM_PATH)).thenReturn(sourcePath);

        //mock the servlet output stream
        when(response.getOutputStream()).thenReturn(servletOutputStream);

        //mock the path to be generated by the ioService
        Path path = mock(Path.class);
        Path pathFileName = mock(Path.class);
        when(path.getFileName()).thenReturn(pathFileName);
        when(pathFileName.toString()).thenReturn(sourceFileName);

        //Expected URI
        URI expectedURI = new URI(FileServletUtil.encodeFileNamePart(sourcePath));

        //mock the path generation
        when(ioService.get(expectedURI)).thenReturn(path);

        //mock the returned content
        when(ioService.readAllBytes(path)).thenReturn(fileContent.getBytes());

        downloadServlet.doGet(request,
                              response);

        verify(response,
               times(1)).setHeader("Content-Disposition",
                                   format("attachment; filename=\"%s\";",
                                          sourceFileName));
        verify(response,
               times(1)).setContentType(eq("application/octet-stream"));
        verify(response,
               times(1)).getOutputStream();

        verify(servletOutputStream,
               times(1)).write(fileContent.getBytes(),
                               0,
                               fileContent.getBytes().length);

        verify(ioService,
               times(1)).get(eq(expectedURI));
        verify(ioService,
               times(1)).readAllBytes(eq(path));
    }
}
