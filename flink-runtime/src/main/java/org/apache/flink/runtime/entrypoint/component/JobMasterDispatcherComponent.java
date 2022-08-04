package org.apache.flink.runtime.entrypoint.component;

import org.apache.flink.runtime.clusterframework.ApplicationStatus;
import org.apache.flink.runtime.dispatcher.DispatcherOperationCaches;
import org.apache.flink.runtime.dispatcher.runner.DispatcherRunner;
import org.apache.flink.runtime.rpc.FatalErrorHandler;
import org.apache.flink.util.AutoCloseableAsync;

import org.apache.flink.util.concurrent.FutureUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public class JobMasterDispatcherComponent implements AutoCloseableAsync {

    private static final Logger LOG =
            LoggerFactory.getLogger(JobMasterDispatcherComponent.class);

    @Nonnull
    private final DispatcherRunner dispatcherRunner;

    private final CompletableFuture<Void> terminationFuture;

    private final CompletableFuture<ApplicationStatus> shutDownFuture;

    private final AtomicBoolean isRunning = new AtomicBoolean(true);

    private final FatalErrorHandler fatalErrorHandler;

    private final DispatcherOperationCaches dispatcherOperationCaches;

    JobMasterDispatcherComponent(
            @Nonnull DispatcherRunner dispatcherRunner,
            @Nonnull FatalErrorHandler fatalErrorHandler,
            @Nonnull DispatcherOperationCaches dispatcherOperationCaches) {
        this.dispatcherRunner = dispatcherRunner;
        this.fatalErrorHandler = fatalErrorHandler;
        this.terminationFuture = new CompletableFuture<>();
        this.shutDownFuture = new CompletableFuture<>();
        this.dispatcherOperationCaches = dispatcherOperationCaches;
        registerShutDownFuture();
    }

    private void registerShutDownFuture() {
        FutureUtils.forward(dispatcherRunner.getShutDownFuture(), shutDownFuture);
    }

    @Override
    public CompletableFuture<Void> closeAsync() {
        return null;
    }
}
