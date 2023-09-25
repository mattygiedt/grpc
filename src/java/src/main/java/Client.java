
import com.google.flatbuffers.FlatBufferBuilder;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
// import io.grpc.stub.StreamObserver;

import com.mattygiedt.flatbuffer.HelloRequest;
import com.mattygiedt.flatbuffer.HelloReply;
import com.mattygiedt.flatbuffer.GreeterGrpc;
import com.mattygiedt.flatbuffer.GreeterGrpc.GreeterBlockingStub;

public class Client {
    private final ManagedChannel channel;
    private final GreeterBlockingStub blockingStub;

    public Client(final String target) {
        channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
        blockingStub = GreeterGrpc.newBlockingStub(channel);
    }

    public String sayHello(final String message) {
        final HelloRequest request = createHelloRequest(message);
        final HelloReply response = blockingStub.sayHello(request);
        return response.message();
    }

    private HelloRequest createHelloRequest(final String message) {
        final FlatBufferBuilder builder = new FlatBufferBuilder(64);

        int nameOffset = builder.createString(message);
        int requestOffset = HelloRequest.createHelloRequest(builder, nameOffset);
        builder.finish(requestOffset);

        return HelloRequest.getRootAsHelloRequest(builder.dataBuffer());
    }

}
