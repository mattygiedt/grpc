package com.mattygiedt.grpc;

import com.google.flatbuffers.FlatBufferBuilder;

import com.mattygiedt.flatbuffer.HelloRequest;
import com.mattygiedt.flatbuffer.ManyHellosRequest;
import com.mattygiedt.flatbuffer.HelloReply;
import com.mattygiedt.flatbuffer.GreeterGrpc;
import com.mattygiedt.flatbuffer.GreeterGrpc.GreeterStub;

import java.util.Iterator;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AsyncClient extends BaseClient {
    private static final Logger logger = LoggerFactory.getLogger(AsyncClient.class);
    private final GreeterStub stub;

    public AsyncClient(final int port) {
        super(port);
        stub = GreeterGrpc.newStub(channel);
    }

    @Override
    public void sayHello(final String message) {
        final HelloRequest request = RequestFactory.createHelloRequest(message);

        final Consumer<HelloReply> consumer = response -> {
            logger.info("sayHello {} -> {}", message, response.message());
            addResponse();
        };

        stub.sayHello(request, new UrnaryCallback<HelloReply>(consumer));
        addRequest();
    }

    @Override
    public void sayManyHellos(final String message, final int greetingCount) {
        final ManyHellosRequest request = RequestFactory.createManyHellosRequest(message, greetingCount);

        stub.sayManyHellos(request, new StreamingCallback<HelloReply>(
            (response) -> {
                logger.info("sayManyHellos {} -> {}", message, response.message());
                addResponse();
            }
        ));

        addRequests(greetingCount);
    }
}
