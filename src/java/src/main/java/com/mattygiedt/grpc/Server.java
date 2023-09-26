package com.mattygiedt.grpc;

import com.google.flatbuffers.FlatBufferBuilder;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import com.mattygiedt.flatbuffer.GreeterGrpc;
import com.mattygiedt.flatbuffer.HelloRequest;
import com.mattygiedt.flatbuffer.ManyHellosRequest;
import com.mattygiedt.flatbuffer.HelloReply;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.net.SocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Server extends GreeterGrpc.GreeterImplBase {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    private final int port;
    private final io.grpc.Server server;
    private final AtomicInteger counter;

    public Server(final int port) throws IOException {
        this.counter = new AtomicInteger(0);
        this.port = port;

        server = ServerBuilder.forPort(port)
                 .addService(this)
                 .executor(Executors.newFixedThreadPool(1))
                 .build();

        server.start();

        logger.info("started server on port: {}", port);

        for(final SocketAddress address : server.getListenSockets()) {
            logger.info(" listen_addr: {}", address);
        }
    }

    public void shutdown() throws Exception {
        server.shutdown().awaitTermination(1, TimeUnit.SECONDS);
    }

    @Override
    public void sayHello(final HelloRequest request,
                         final StreamObserver<HelloReply> responseObserver) {
        final HelloReply reply = createHelloReply(request.name());
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void sayManyHellos(final ManyHellosRequest request,
                              final StreamObserver<HelloReply> responseObserver) {
        for(int i=0; i<request.numGreetings(); i++) {
            responseObserver.onNext(createHelloReply(request.name()));
        }

        responseObserver.onCompleted();
    }

    private HelloReply createHelloReply(final String name) {
        final FlatBufferBuilder builder = new FlatBufferBuilder(64);
        final StringBuilder sb = new StringBuilder();

        final int nameOffset = builder.createString(
            sb.append("Hello, ")
              .append(name)
              .append(" [")
              .append(counter.addAndGet(1))
              .append("]")
              .toString());

        final int replyOffset = HelloReply.createHelloReply(builder, nameOffset);
        builder.finish(replyOffset);

        return HelloReply.getRootAsHelloReply(builder.dataBuffer());
    }
}
