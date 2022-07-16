/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.flink.runtime.blob;

import org.apache.flink.api.common.JobID;
import org.apache.flink.configuration.BlobServerOptions;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.core.fs.FileSystem;
import org.apache.flink.core.fs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;

import static org.apache.flink.runtime.blob.BlobKey.BlobType.PERMANENT_BLOB;
import static org.apache.flink.util.Preconditions.checkNotNull;

/**
 * A blob shared client. if jobMaster run in single process, please use it to replace a {@link
 * BlobWriter}. Currently, in order not to affect POC verification, abstract classes are not used
 * for abstraction*
 */
public class BlobSharedClient implements BlobWriter, PermanentBlobService, Closeable {

    private static final Logger LOG = LoggerFactory.getLogger(BlobClient.class);

    private final InetSocketAddress serverAddress;

    private final BlobClient client;
    private final Configuration configuration;

    private final int numFetchRetries;

    /**
     * Instantiates a new BLOB client.
     *
     * @param serverAddress the network address of the BLOB server
     * @param clientConfig additional configuration like SSL parameters required to connect to the
     *     blob server
     * @throws IOException thrown if the connection to the BLOB server could not be established
     */
    public BlobSharedClient(InetSocketAddress serverAddress, Configuration clientConfig)
            throws IOException {
        this.serverAddress = serverAddress;
        this.configuration = clientConfig;
        this.client = new BlobClient(serverAddress, clientConfig);
        final int fetchRetries = configuration.getInteger(BlobServerOptions.FETCH_RETRIES);
        if (fetchRetries >= 0) {
            this.numFetchRetries = fetchRetries;
        } else {
            LOG.warn(
                    "Invalid value for {}. System will attempt no retries on failed fetch "
                            + "operations of BLOBs.",
                    BlobServerOptions.FETCH_RETRIES.key());
            this.numFetchRetries = 0;
        }
    }

    // --------------------------------------------------------------------------------------------
    //  delete
    // --------------------------------------------------------------------------------------------

    /**
     * TODO Maybe graceful to FileSystemBlobStore.Delete
     *
     * @param jobId ID of the job this blob belongs to (or <tt>null</tt> if job-unrelated)
     * @param permanentBlobKey
     * @return the key of this blob
     */
    boolean deleteInternal(JobID jobId, PermanentBlobKey permanentBlobKey) {
        final String basePath = configuration.getString(BlobServerOptions.STORAGE_DIRECTORY);
        Path file = new Path(BlobUtils.getStorageLocationPath(basePath, jobId, permanentBlobKey));
        try {
            FileSystem fileSystem = file.getFileSystem();
            boolean result = true;
            if (fileSystem.exists(file)) {
                result = fileSystem.delete(file, true);
            } else {
                LOG.debug(
                        "The given path {} is not present anymore. No deletion is required.", file);
            }
            // send a call to delete the directory containing the file. This will
            // fail (and be ignored) when some files still exist.
            try {
                fileSystem.delete(file.getParent(), false);
                fileSystem.delete(new Path(basePath), false);
            } catch (IOException ignored) {
            }
            return result;
        } catch (Exception e) {
            LOG.warn("Failed to delete blob at " + file);
            return false;
        }
    }

    @Override
    public PermanentBlobKey putPermanent(JobID jobId, byte[] value) throws IOException {
        checkNotNull(jobId);
        return (PermanentBlobKey) client.putBuffer(jobId, value, 0, value.length, PERMANENT_BLOB);
    }

    @Override
    public PermanentBlobKey putPermanent(JobID jobId, InputStream inputStream) throws IOException {
        checkNotNull(jobId);
        return (PermanentBlobKey) client.putInputStream(jobId, inputStream, PERMANENT_BLOB);
    }

    @Override
    public boolean deletePermanent(JobID jobId, PermanentBlobKey permanentBlobKey) {
        return deleteInternal(jobId, permanentBlobKey);
    }

    @Override
    public int getMinOffloadingSize() {
        return configuration.getInteger(BlobServerOptions.OFFLOAD_MINSIZE);
    }

    @Override
    public void close() throws IOException {
        client.close();
    }

    @Override
    public File getFile(JobID jobId, PermanentBlobKey key) throws IOException {
        // TODO MayBe Temp File Path
        File incomingFile = new File("");
        BlobClient.downloadFromBlobServer(
                jobId, key, incomingFile, serverAddress, configuration, numFetchRetries);
        return incomingFile;
    }
}
