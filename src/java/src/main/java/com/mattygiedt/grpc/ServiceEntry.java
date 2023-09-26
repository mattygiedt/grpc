package com.mattygiedt.grpc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

public final class ServiceEntry {
    private static final Logger logger = LoggerFactory.getLogger(ServiceEntry.class);
    private static final CountDownLatch shutdownLatch = new CountDownLatch(1);

    private static void doClient(final int port) throws Exception {
        final Client client = new Client(port);

        client.sayHello("JavaFlatbuffer");
        client.sayManyHellos("JavaFlatbuffers", 10);

        logger.info("client.sayHello done...");
        client.shutdown();
    }

    private static void doServer(final int port) throws Exception {
        final Server server = new Server(port);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                server.shutdown();
            } catch(Exception ex) {
                logger.error("Shutdown error:", ex);
            }

            shutdownLatch.countDown();
        }));

        shutdownLatch.await();
    }

    public static void main(final String[] args) throws Exception {
        if(args.length != 2) {
            throw new Exception("usage: ServiceEntry [CLIENT|SERVER] [port]");
        }

        logger.info("Starting '{}' service on port: {}", args[0], args[1]);

        final String serviceType = args[0];
        final int port = Integer.parseInt(args[1]);

        if("CLIENT".equals(serviceType)) {
            doClient(port);
        } else if("SERVER".equals(serviceType)) {
            doServer(port);
        } else {
            throw new Exception("unknown service type: " + serviceType);
        }
    }
}
