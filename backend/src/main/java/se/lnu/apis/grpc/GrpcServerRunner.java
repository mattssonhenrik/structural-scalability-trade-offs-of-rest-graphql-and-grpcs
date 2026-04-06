package se.lnu.apis.grpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptors;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import se.lnu.metrics.OrchestrationGrpcInterceptor;

/**
 * Starts the gRPC server on port 9090 when Spring Boot starts.
 * Stops it cleanly on shutdown.
 */
@Component
public class GrpcServerRunner implements ApplicationRunner {

    @Autowired
    private GrpcService grpcService;

    private Server server;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        server = ServerBuilder.forPort(9090)
                .addService(ServerInterceptors.intercept(grpcService, new OrchestrationGrpcInterceptor()))
                .build()
                .start();
    }

    @PreDestroy
    public void stop() {
        if (server != null) server.shutdown();
    }
}
