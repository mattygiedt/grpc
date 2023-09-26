# grpc
[gRPC](https://github.com/grpc/grpc) exploration with [flatbuffers](https://github.com/google/flatbuffers)

## Background
gRPC is a modern, open source, high-performance remote procedure call (RPC) framework that can run anywhere. gRPC enables client and server applications to communicate transparently, and simplifies the building of connected systems.

gRPC was released right around the time I started at CTC, and I have yet to actually use the technology. Additionally, flatbuffers has recently added gRPC [support](https://grpc.io/blog/grpc-flatbuffers/) and I had a long weekend to kill ...

## Asynchronous Example
The C++ flatbuffer gRPC [example](https://github.com/google/flatbuffers/tree/master/grpc/samples/greeter) follows the protobuf documentation and demonstrates how to implement a single-threaded RPC client / server. The protobuf examples extend this with async client and server, however the flatbuffer examples neglect to include these... I (think) I was able to figure out how to do the asynchronous bits using flatbuffers. (I should submit a PR...)

## Building
As with the other more recent repositiories up here I'm using the [VS Code Docker integration](https://code.visualstudio.com/docs/remote/containers) for my development. Look at the `.devcontainer/Dockerfile` for the project dependencies if you want to roll your own.

```
git clone git@github.com:mattygiedt/grpc.git
cd grpc
code . <open project inside dev container>
mkdir build
cd build
cmake ..
make -j4
```

### Client / Server
Split your `Terminal` in half, and then start-up the server ...
```
root@:/workspaces/grpc/build# ./src/cpp/server 0.0.0.0:50051
[2022-09-06 23:21:54.486] [info] Server listening on 0.0.0.0:50051
```

And then play around with the client.
```
root@:/workspaces/grpc/build# ./src/cpp/client localhost:50051
[2022-09-06 23:22:19.095] [info] server addr: localhost:50051
[2022-09-06 23:22:19.098] [info] SayHello request: World, response: Hello, World
[2022-09-06 23:22:19.098] [info] SayManyHellos received: Many hellos, World
[2022-09-06 23:22:19.098] [info] SayManyHellos received: Many hellos, World
[2022-09-06 23:22:19.098] [info] SayManyHellos received: Many hellos, World
[2022-09-06 23:22:19.098] [info] SayManyHellos received: Many hellos, World
[2022-09-06 23:22:19.098] [info] SayManyHellos received: Many hellos, World
[2022-09-06 23:22:19.098] [info] SayManyHellos received: Many hellos, World
[2022-09-06 23:22:19.098] [info] SayManyHellos received: Many hellos, World
[2022-09-06 23:22:19.098] [info] SayManyHellos received: Many hellos, World
[2022-09-06 23:22:19.099] [info] SayManyHellos received: Many hellos, World
[2022-09-06 23:22:19.099] [info] SayManyHellos received: Many hellos, World
root@:/workspaces/grpc/build#
```

### Asynchronous Client / Server
Start the server first:
```
root@:/workspaces/grpc/build# ./src/cpp/async_server 0.0.0.0:50051
[2022-09-06 23:24:59.129] [info] Server listening on 0.0.0.0:50051
```

And then connect the client:
```
root@:/workspaces/grpc/build# ./src/cpp/async_client localhost:50051
[2022-09-06 23:25:21.035] [info] server addr: localhost:50051
[2022-09-06 23:25:21.037] [info] SayHelloAsync request: World, response: Hello, World
```

## Let's add Java to the mix!
First, using Maven, build the shaded JAR file.
```
root@398d7148e5b2:/workspaces/grpc# mvn clean package
```
Now, start the server
```
root@398d7148e5b2:/workspaces/grpc# java -jar src/java/target/grpc-java-0.0.1-SNAPSHOT-shaded.jar SERVER 50051
16:17:49.651 [main] INFO com.mattygiedt.grpc.ServiceEntry -- Starting 'SERVER' service on port: 50051
16:17:49.893 [main] INFO com.mattygiedt.grpc.Server -- started server on port: 50051
16:17:49.894 [main] INFO com.mattygiedt.grpc.Server --  listen_addr: /0.0.0.0:50051
```
The Java source demonstrates three types of client (blocking, asynchronous, listenable_future).
Once the server is started, choose one client type and run it!
```
root@398d7148e5b2:/workspaces/grpc# java -jar src/java/target/grpc-java-0.0.1-SNAPSHOT-shaded.jar ASYNC_CLIENT 50051
16:17:56.120 [main] INFO com.mattygiedt.grpc.ServiceEntry -- Starting 'ASYNC_CLIENT' service on port: 50051
16:17:56.348 [main] INFO com.mattygiedt.grpc.ServiceEntry -- client.isPendingResponse... (32)
16:17:56.448 [main] INFO com.mattygiedt.grpc.ServiceEntry -- client.isPendingResponse... (32)
16:17:56.549 [main] INFO com.mattygiedt.grpc.ServiceEntry -- client.isPendingResponse... (32)
16:17:56.623 [grpc-default-executor-1] INFO com.mattygiedt.grpc.AsyncClient -- sayManyHellos JavaFlatbuffers -> Hello, JavaFlatbuffers [4]
16:17:56.624 [grpc-default-executor-1] INFO com.mattygiedt.grpc.AsyncClient -- sayManyHellos JavaFlatbuffers -> Hello, JavaFlatbuffers [2]
16:17:56.625 [grpc-default-executor-2] INFO com.mattygiedt.grpc.AsyncClient -- sayHello JavaFlatbuffer -> Hello, JavaFlatbuffer [1]
16:17:56.625 [grpc-default-executor-1] INFO com.mattygiedt.grpc.AsyncClient -- sayHello JavaFlatbuffer -> Hello, JavaFlatbuffer [3]
...
16:17:56.637 [grpc-default-executor-2] INFO com.mattygiedt.grpc.AsyncClient -- sayManyHellos JavaFlatbuffers -> Hello, JavaFlatbuffers [30]
16:17:56.637 [grpc-default-executor-2] INFO com.mattygiedt.grpc.AsyncClient -- sayManyHellos JavaFlatbuffers -> Hello, JavaFlatbuffers [31]
16:17:56.638 [grpc-default-executor-2] INFO com.mattygiedt.grpc.AsyncClient -- sayManyHellos JavaFlatbuffers -> Hello, JavaFlatbuffers [32]
16:17:56.649 [main] INFO com.mattygiedt.grpc.ServiceEntry -- client.sayHello done...
```
