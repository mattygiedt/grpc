package com.mattygiedt.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class BaseClient {

    protected final AtomicInteger pendingResponse;
    protected final ManagedChannel channel;
    protected final ExecutorService executor;

    public BaseClient(final int port) {
        this("localhost", port);
    }

    public BaseClient(final String host, final int port) {
        pendingResponse = new AtomicInteger(0);
        executor = Executors.newSingleThreadExecutor();
        channel = ManagedChannelBuilder
            .forAddress(host, port)
            .executor(executor)
            .usePlaintext()
            .build();
    }

    public void shutdown() throws Exception {
        channel.shutdown().awaitTermination(1, TimeUnit.SECONDS);
        executor.shutdown();
    }

    public boolean isPendingResponse() {
        return pendingResponse.get() != 0;
    }

    public int pendingResponseCount() {
        return pendingResponse.get();
    }

    protected void addRequest() {
        pendingResponse.incrementAndGet();
    }

    protected void addRequests(final int count) {
        pendingResponse.addAndGet(count);
    }

    protected void addResponse() {
        pendingResponse.decrementAndGet();
    }

    abstract void sayHello(final String message);
    abstract void sayManyHellos(final String message, final int greetingCount);
}
