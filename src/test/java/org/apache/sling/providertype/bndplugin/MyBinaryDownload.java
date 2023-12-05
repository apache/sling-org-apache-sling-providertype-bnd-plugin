/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.sling.providertype.bndplugin;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import javax.jcr.RepositoryException;

import org.apache.jackrabbit.api.binary.BinaryDownload;
import org.apache.jackrabbit.api.binary.BinaryDownloadOptions;

public class MyBinaryDownload implements BinaryDownload {

    @Override
    public InputStream getStream() throws RepositoryException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int read(byte[] b, long position) throws IOException, RepositoryException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public long getSize() throws RepositoryException {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void dispose() {
        // TODO Auto-generated method stub

    }

    @Override
    public URI getURI(BinaryDownloadOptions downloadOptions) throws RepositoryException {
        // TODO Auto-generated method stub
        return null;
    }

}
