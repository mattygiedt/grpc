package com.mattygiedt.grpc;

import com.google.flatbuffers.FlatBufferBuilder;
import com.google.common.util.concurrent.ListenableFuture;

import com.mattygiedt.flatbuffer.HelloRequest;
import com.mattygiedt.flatbuffer.ManyHellosRequest;
import com.mattygiedt.flatbuffer.HelloReply;
import com.mattygiedt.flatbuffer.GreeterGrpc;
import com.mattygiedt.flatbuffer.GreeterGrpc.GreeterFutureStub;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ListenableFutureClient extends BaseClient {
    private static final Logger logger = LoggerFactory.getLogger(ListenableFutureClient.class);

    private final GreeterFutureStub stub;


    public ListenableFutureClient(final int port) {
        super(port);
        stub = GreeterGrpc.newFutureStub(channel);
    }

    public void sayHello(final String message) {
        final HelloRequest request = RequestFactory.createHelloRequest(message);
        final ListenableFuture<HelloReply> future = stub.sayHello(request);

        future.addListener(() -> {
            try {
                logger.info("sayHello {} -> {}", message, future.get().message());
                addResponse();
            } catch(final Throwable t) {
                logger.error("sayHello callback error", t);
            }
        }, executor);

        addRequest();
    }

    public void sayManyHellos(final String message, final int greetingCount) {
        //
        //  https://grpc.io/docs/languages/java/generated-code/
        //
        //  The future stub contains one Java method for each unary method
        //  in the service definition. Future stubs do not support streaming calls.
        //

        throw new UnsupportedOperationException("Future stubs do not support streaming calls");
    }
}
