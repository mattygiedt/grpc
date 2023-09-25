
import com.google.flatbuffers.FlatBufferBuilder;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import com.mattygiedt.flatbuffer.GreeterGrpc;
import com.mattygiedt.flatbuffer.HelloRequest;
import com.mattygiedt.flatbuffer.ManyHellosRequest;
import com.mattygiedt.flatbuffer.HelloReply;

import java.io.IOException;
import java.util.concurrent.Executors;

public class Server extends GreeterGrpc.GreeterImplBase {

    private final int port;
    private final io.grpc.Server server;

    public Server(final int port) throws IOException {
        this.port = port;
        this.server = ServerBuilder.forPort(port)
                 .addService(this)
                 .executor(Executors.newFixedThreadPool(1))
                 .build();

        this.server.start();
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

    }

    private HelloReply createHelloReply(final String name) {
        final FlatBufferBuilder builder = new FlatBufferBuilder(64);

        int nameOffset = builder.createString("Hello " + name);
        int replyOffset = HelloReply.createHelloReply(builder, nameOffset);
        builder.finish(replyOffset);

        return HelloReply.getRootAsHelloReply(builder.dataBuffer());
    }
}
