package org.apache.flink.runtime.dispatcher.runner;

import org.apache.flink.runtime.dispatcher.DispatcherFactory;
import org.apache.flink.runtime.dispatcher.PartialDispatcherServices;
import org.apache.flink.runtime.jobmanager.JobPersistenceComponentFactory;
import org.apache.flink.runtime.rpc.FatalErrorHandler;
import org.apache.flink.runtime.rpc.RpcService;

import java.util.concurrent.Executor;

public class SessionJobMasterDispatcherLeaderProcessFactoryFactory implements DispatcherLeaderProcessFactoryFactory {

    private final DispatcherFactory dispatcherFactory;

    private SessionJobMasterDispatcherLeaderProcessFactoryFactory(DispatcherFactory dispatcherFactory) {
        this.dispatcherFactory = dispatcherFactory;
    }

    @Override
    public DispatcherLeaderProcessFactory createFactory(
            JobPersistenceComponentFactory jobPersistenceComponentFactory,
            Executor ioExecutor,
            RpcService rpcService,
            PartialDispatcherServices partialDispatcherServices,
            FatalErrorHandler fatalErrorHandler) {
        final AbstractDispatcherLeaderProcess.DispatcherGatewayServiceFactory
                dispatcherGatewayServiceFactory =
                new JobMasterDispatcherGatewayServiceFactory(
                        dispatcherFactory, rpcService, partialDispatcherServices);

        return new SessionDispatcherLeaderProcessFactory(
                dispatcherGatewayServiceFactory,
                jobPersistenceComponentFactory,
                ioExecutor,
                fatalErrorHandler);
    }

    public static SessionJobMasterDispatcherLeaderProcessFactoryFactory create(
            DispatcherFactory dispatcherFactory) {
        return new SessionJobMasterDispatcherLeaderProcessFactoryFactory(dispatcherFactory);
    }
}
