package com.mattygiedt.grpc;

import com.google.flatbuffers.FlatBufferBuilder;

import com.mattygiedt.flatbuffer.HelloRequest;
import com.mattygiedt.flatbuffer.ManyHellosRequest;
import com.mattygiedt.flatbuffer.HelloReply;
import com.mattygiedt.flatbuffer.GreeterGrpc;
import com.mattygiedt.flatbuffer.GreeterGrpc.GreeterBlockingStub;

import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlockingClient extends BaseClient {
    private static final Logger logger = LoggerFactory.getLogger(BlockingClient.class);

    private final GreeterBlockingStub blockingStub;

    public BlockingClient(final int port) {
        super(port);
        blockingStub = GreeterGrpc.newBlockingStub(channel);
    }

    @Override
    public void sayHello(final String message) {
        final HelloRequest request = RequestFactory.createHelloRequest(message);
        final HelloReply response = blockingStub.sayHello(request);

        logger.info("sayHello {} -> {}", message, response.message());
    }

    @Override
    public void sayManyHellos(final String message, final int greetingCount) {
        final ManyHellosRequest request = RequestFactory.createManyHellosRequest(message, greetingCount);
        final Iterator<HelloReply> iter = blockingStub.sayManyHellos(request);

        while(iter.hasNext()) {
            final HelloReply response = iter.next();
            logger.info("sayManyHellos {} -> {}", message, response.message());
        }
    }
}
