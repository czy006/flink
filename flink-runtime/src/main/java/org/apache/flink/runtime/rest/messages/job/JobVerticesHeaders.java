package org.apache.flink.runtime.rest.messages.job;

import org.apache.flink.runtime.rest.HttpMethodWrapper;
import org.apache.flink.runtime.rest.messages.EmptyRequestBody;
import org.apache.flink.runtime.rest.messages.JobMessageParameters;
import org.apache.flink.runtime.rest.messages.MessageHeaders;
import org.apache.flink.runtime.rest.versioning.RestAPIVersion;
import org.apache.flink.runtime.rest.versioning.RuntimeRestAPIVersion;

import org.apache.flink.shaded.netty4.io.netty.handler.codec.http.HttpResponseStatus;

import java.util.Collection;
import java.util.Collections;

public class JobVerticesHeaders
        implements MessageHeaders<EmptyRequestBody, JobVerticesInfo, JobMessageParameters> {

    private static final JobVerticesHeaders INSTANCE = new JobVerticesHeaders();
    public static final String URL = String.format("/jobs/:%s/vertices/details", "jobid");

    private JobVerticesHeaders() {}

    public Class<EmptyRequestBody> getRequestClass() {
        return EmptyRequestBody.class;
    }

    public Class<JobVerticesInfo> getResponseClass() {
        return JobVerticesInfo.class;
    }

    public HttpResponseStatus getResponseStatusCode() {
        return HttpResponseStatus.OK;
    }

    public JobMessageParameters getUnresolvedMessageParameters() {
        return new JobMessageParameters();
    }

    public HttpMethodWrapper getHttpMethod() {
        return HttpMethodWrapper.GET;
    }

    public String getTargetRestEndpointURL() {
        return URL;
    }

    @Override
    public Collection<? extends RestAPIVersion<?>> getSupportedAPIVersions() {
        return Collections.singleton(RuntimeRestAPIVersion.V1);
    }

    public static JobVerticesHeaders getInstance() {
        return INSTANCE;
    }

    public String getDescription() {
        return "Returns all vertecies of a job.";
    }
}
