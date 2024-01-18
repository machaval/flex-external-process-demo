package github.machaval.transformation.grpc;

import io.grpc.ServerBuilder;

import java.io.IOException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Launcher for the GRPC Service
 */
public class Launcher {

    static Logger LOG = Logger.getLogger(Launcher.class.getSimpleName());

    public static void main(String[] args) throws IOException {

        // The Configuration PATH
        String path = Optional.ofNullable(System.getenv("CONFIG_PATH"))
                .or(() -> {
                    if (args.length > 1) {
                        return Optional.of(args[1]);
                    } else {
                        return Optional.empty();
                    }
                })
                .orElse("/transformations");

        // The GRPC Port
        String port = Optional.ofNullable(System.getenv("GRPC_PORT"))
                .or(() -> {
                    if (args.length > 1) {
                        return Optional.of(args[1]);
                    } else {
                        return Optional.empty();
                    }
                })
                .orElse("8980");

        final ServerBuilder<?> serverBuilder = ServerBuilder.forPort(Integer.parseInt(port));
        serverBuilder.addService(new GRPCTransformationServiceImpl(path));
        final GRPCTransformationServer server = new GRPCTransformationServer(serverBuilder.build());
        server.start();
        LOG.log(Level.INFO, "Server started at port :`" + port);
        server.waitUntilTermination();
    }
}
