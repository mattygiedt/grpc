package com.mattygiedt.grpc;

import com.google.flatbuffers.FlatBufferBuilder;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import com.mattygiedt.flatbuffer.HelloRequest;
import com.mattygiedt.flatbuffer.ManyHellosRequest;
import com.mattygiedt.flatbuffer.HelloReply;
import com.mattygiedt.flatbuffer.GreeterGrpc;
import com.mattygiedt.flatbuffer.GreeterGrpc.GreeterBlockingStub;

import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Client {
    private static final Logger logger = LoggerFactory.getLogger(Client.class);

    private final ManagedChannel channel;
    private final GreeterBlockingStub blockingStub;

    public Client(final int port) {

        logger.info("connecting to gRPC address localhost:{}", port);

        channel = ManagedChannelBuilder
            .forAddress("localhost", port)
            .usePlaintext()
            .build();

        blockingStub = GreeterGrpc.newBlockingStub(channel);
    }

    public void shutdown() {
        channel.shutdown();
    }

    public void sayHello(final String message) {
        final HelloRequest request = createHelloRequest(message);
        final HelloReply response = blockingStub.sayHello(request);

        logger.info("sayHello {} -> {}", message, response.message());
    }

    public void sayManyHellos(final String message, final int greetingCount) {
        final ManyHellosRequest request = createManyHellosRequest(message, greetingCount);
        final Iterator<HelloReply> iter = blockingStub.sayManyHellos(request);

        while(iter.hasNext()) {
            final HelloReply response = iter.next();
            logger.info("sayManyHellos {} -> {}", message, response.message());
        }
    }

    private HelloRequest createHelloRequest(final String message) {
        final FlatBufferBuilder builder = new FlatBufferBuilder(64);

        int nameOffset = builder.createString(message);
        int requestOffset = HelloRequest.createHelloRequest(builder, nameOffset);
        builder.finish(requestOffset);

        return HelloRequest.getRootAsHelloRequest(builder.dataBuffer());
    }

    private ManyHellosRequest createManyHellosRequest(final String message, final int greetingCount) {
        final FlatBufferBuilder builder = new FlatBufferBuilder(64);

        int nameOffset = builder.createString(message);
        int requestOffset = ManyHellosRequest.createManyHellosRequest(builder, nameOffset, greetingCount);
        builder.finish(requestOffset);

        return ManyHellosRequest.getRootAsManyHellosRequest(builder.dataBuffer());
    }

}
