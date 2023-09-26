package com.mattygiedt.grpc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

public final class ServiceEntry {
    private static final Logger logger = LoggerFactory.getLogger(ServiceEntry.class);
    private static final CountDownLatch shutdownLatch = new CountDownLatch(1);

    private static void doClient(final BaseClient client) throws Exception {

        for(int i=0; i<16; i++) {
            client.sayHello("JavaFlatbuffer");

            if(client instanceof ListenableFutureClient == false) {
                client.sayManyHellos("JavaFlatbuffers", 10);
            }

            client.sayHello("JavaFlatbufferAgain");

            if(client instanceof ListenableFutureClient == false) {
                client.sayManyHellos("JavaFlatbuffers", 20);
            }

            while(client.isPendingResponse()) {
                logger.info("client.isPendingResponse... ({})", client.pendingResponseCount());
                Thread.sleep(100);
            }
        }

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
            throw new Exception("usage: ServiceEntry [BLOCKING_CLIENT|ASYNC_CLIENT|FUTURE_CLIENT|SERVER] [port]");
        }

        //
        //  By default, logging is set at debug, which gRPC uses heavily.
        //  Here's a hacktastic way to configure log_level -> info at runtime.
        //

        final ch.qos.logback.classic.Logger rootLogger =
            (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(
                ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        rootLogger.setLevel(ch.qos.logback.classic.Level.toLevel("info"));

        logger.info("Starting '{}' service on port: {}", args[0], args[1]);

        final String serviceType = args[0];
        final int port = Integer.parseInt(args[1]);

        if("BLOCKING_CLIENT".equals(serviceType)) {
            doClient(new BlockingClient(port));
        } else if("ASYNC_CLIENT".equals(serviceType)) {
            doClient(new AsyncClient(port));
        } else if("FUTURE_CLIENT".equals(serviceType)) {
            doClient(new ListenableFutureClient(port));
        } else if("SERVER".equals(serviceType)) {
            doServer(port);
        } else {
            throw new Exception("unknown service type: " + serviceType);
        }
    }
}
