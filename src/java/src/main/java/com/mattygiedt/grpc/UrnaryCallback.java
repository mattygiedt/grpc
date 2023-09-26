package com.mattygiedt.grpc;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;

import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UrnaryCallback<T> implements StreamObserver<T> {
    private static final Logger logger = LoggerFactory.getLogger(UrnaryCallback.class);

    private final Consumer<T> handler;
    private T response = null;

    public UrnaryCallback(final Consumer<T> handler) {
        this.handler = handler;
    }

    public T getResponse() {
        return response;
    }

    @Override
    public void onNext(final T response) {
        this.response = response;
    }

    @Override
    public void onCompleted() {
        handler.accept(getResponse());
    }

    @Override
    public void onError(final Throwable t) {
        logger.error("UrnaryCallback error: {}", Status.fromThrowable(t));
    }
}
