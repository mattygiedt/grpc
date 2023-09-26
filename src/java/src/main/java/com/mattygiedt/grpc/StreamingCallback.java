package com.mattygiedt.grpc;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;

import java.util.function.Consumer;

import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StreamingCallback<T> implements StreamObserver<T> {
    private static final Logger logger = LoggerFactory.getLogger(StreamingCallback.class);

    private final Consumer<T> callbackHandler;
    private final Runnable completionHandler;
    private final AtomicBoolean completed = new AtomicBoolean(false);

    public StreamingCallback(final Consumer<T> callbackHandler) {
        this(callbackHandler, () -> {});
    }

    public StreamingCallback(final Consumer<T> callbackHandler,
                             final Runnable completionHandler) {
        this.callbackHandler = callbackHandler;
        this.completionHandler = completionHandler;
    }

    public boolean isCompleted() {
        return completed.get();
    }

    @Override
    public void onNext(final T response) {
        callbackHandler.accept(response);
    }

    @Override
    public void onCompleted() {
        completionHandler.run();
        completed.set(true);
    }

    @Override
    public void onError(final Throwable t) {
        logger.error("StreamingCallback error: {}", Status.fromThrowable(t));
    }
}
