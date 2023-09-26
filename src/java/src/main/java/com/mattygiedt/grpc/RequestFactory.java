package com.mattygiedt.grpc;

import com.mattygiedt.flatbuffer.HelloRequest;
import com.mattygiedt.flatbuffer.ManyHellosRequest;

import com.google.flatbuffers.FlatBufferBuilder;

public final class RequestFactory {

    public static HelloRequest createHelloRequest(final String message) {
        final FlatBufferBuilder builder = new FlatBufferBuilder(64);

        int nameOffset = builder.createString(message);
        int requestOffset = HelloRequest.createHelloRequest(builder, nameOffset);
        builder.finish(requestOffset);

        return HelloRequest.getRootAsHelloRequest(builder.dataBuffer());
    }

    public static ManyHellosRequest createManyHellosRequest(final String message, final int greetingCount) {
        final FlatBufferBuilder builder = new FlatBufferBuilder(64);

        int nameOffset = builder.createString(message);
        int requestOffset = ManyHellosRequest.createManyHellosRequest(builder, nameOffset, greetingCount);
        builder.finish(requestOffset);

        return ManyHellosRequest.getRootAsManyHellosRequest(builder.dataBuffer());
    }
}
