package github.machaval.transformation.grpc;

import io.grpc.Server;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class GRPCTransformationServer {
    private Server server;

    public GRPCTransformationServer(Server server) {
        this.server = server;
    }

    public void start() throws IOException {
        this.server.start();
    }

    public void stop() {
        this.server.shutdown();
        try {
            this.server.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            // Ignore nothing to do
        }
    }

    public void waitUntilTermination() {
        try {
            this.server.awaitTermination();
        } catch (InterruptedException e) {
            //Ignore nothing to do here
        }
    }
}
