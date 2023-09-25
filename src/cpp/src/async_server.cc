#include <iostream>
#include <span>
#include <string>

#include "grpcpp/grpcpp.h"
#include "messages.grpc.fb.h"
#include "messages_generated.h"
#include "spdlog/spdlog.h"

// https://github.com/google/flatbuffers/blob/master/grpc/samples/greeter/server.cpp
// https://github.com/grpc/grpc/blob/v1.46.3/examples/cpp/helloworld/greeter_async_server.cc

using namespace com::mattygiedt::flatbuffer;

// Let's implement a tiny state machine with the following states.
enum CallStatus { CREATE, PROCESS, FINISH };

class CallData {
 private:
  using HelloRequestMessage = flatbuffers::grpc::Message<HelloRequest>;
  using HelloReplyMessage = flatbuffers::grpc::Message<HelloReply>;
  using HelloReplyResponseWriter =
      grpc::ServerAsyncResponseWriter<HelloReplyMessage>;

 public:
  // Take in the "service" instance (in this case representing an asynchronous
  // server) and the completion queue "cq" used for asynchronous communication
  // with the gRPC runtime.
  CallData(Greeter::AsyncService* service, grpc::ServerCompletionQueue* cq)
      : service_(service), cq_(cq) {}

  auto Proceed() -> void {
    if (status_ == CREATE) {
      // Make this instance progress to the PROCESS state.
      status_ = PROCESS;

      // As part of the initial CREATE state, we *request* that the system
      // start processing SayHello requests. In this request, "this" acts are
      // the tag uniquely identifying the request (so that different CallData
      // instances can serve different requests concurrently), in this case
      // the memory address of this CallData instance.
      service_->RequestSayHello(&ctx_, &request_, &responder_, cq_, cq_, this);
    } else if (status_ == PROCESS) {
      // Spawn a new CallData instance to serve new clients while we process
      // the one for this CallData. The instance will deallocate itself as
      // part of its FINISH state.
      auto* call_data = new CallData(service_, cq_);  // NOLINT
      call_data->Proceed();

      // The actual processing.
      auto status = SayHello();

      // And we are done! Let the gRPC runtime know we've finished, using the
      // memory address of this instance as the uniquely identifying tag for
      // the event.
      status_ = FINISH;
      responder_.Finish(reply_, status, this);
    } else {
      // Once in the FINISH state, deallocate ourselves (CallData).
      delete this;
    }
  }

  auto SayHello() -> grpc::Status {
    // 'flatbuffers::grpc::MessageBuilder' is a 'FlatBufferBuilder' with a
    // special allocator for efficient gRPC buffer transfer, but otherwise
    // usage is the same as usual.
    flatbuffers::grpc::MessageBuilder mb;

    // Fields are retrieved as usual with FlatBuffers
    const HelloRequest* message = request_.GetRoot();
    const std::string& name = message->name()->str();
    auto msg_offset = mb.CreateString("Hello, " + name);
    auto hello_offset = CreateHelloReply(mb, msg_offset);
    mb.Finish(hello_offset);

    // The 'ReleaseMessage<T>()' function detaches the message from the
    // builder, so we can transfer the resopnse to gRPC while simultaneously
    // detaching that memory buffer from the builder.
    reply_ = mb.ReleaseMessage<HelloReply>();

    // Return an OK status.
    return grpc::Status::OK;
  }

 private:
  // The means of communication with the gRPC runtime for an asynchronous
  // server.
  Greeter::AsyncService* service_;

  // The producer-consumer queue where for asynchronous server notifications.
  grpc::ServerCompletionQueue* cq_;

  // Context for the rpc, allowing to tweak aspects of it such as the use
  // of compression, authentication, as well as to send metadata back to the
  // client.
  grpc::ServerContext ctx_{};

  // The means to get back to the client.
  HelloReplyResponseWriter responder_{&ctx_};

  // What we get from the client.
  HelloRequestMessage request_{};

  // What we send back to the client.
  HelloReplyMessage reply_{};

  // The current serving state.
  CallStatus status_{CREATE};
};

class AsyncServer final {
 private:
 public:
  explicit AsyncServer(std::string server_address)
      : server_address_(std::move(server_address)) {}

  // There is no shutdown handling in this code.
  auto Run() -> void {
    grpc::ServerBuilder builder;

    // Listen on the given address without any authentication mechanism.
    builder.AddListeningPort(server_address_,
                             grpc::InsecureServerCredentials());

    // Register "service_" as the instance through which we'll communicate with
    // clients. In this case it corresponds to an *asynchronous* service.
    builder.RegisterService(&service_);

    // Get hold of the completion queue used for the asynchronous communication
    // with the gRPC runtime.
    cq_ = builder.AddCompletionQueue();

    // Finally assemble the server.
    server_ = builder.BuildAndStart();
    spdlog::info("Server listening on {}", server_address_);

    // Spawn a new CallData instance to serve new clients.
    auto* call_data = new CallData(&service_, cq_.get());  // NOLINT
    call_data->Proceed();

    // Proceed to the server's main loop.
    HandleRpcs();
  }

 private:
  // HandleRpcs can be run in multiple threads if needed.
  auto HandleRpcs() -> void {
    void* tag = nullptr;  // uniquely identifies a request.
    bool ok = false;
    while (true) {
      // Block waiting to read the next event from the completion queue. The
      // event is uniquely identified by its tag, which in this case is the
      // memory address of a CallData instance.
      // The return value of Next should always be checked. This return value
      // tells us whether there is any kind of event or cq_ is shutting down.
      cq_->Next(&tag, &ok);

      if (ok) {
        static_cast<CallData*>(tag)->Proceed();
      }
    }
  }

  std::string server_address_;
  Greeter::AsyncService service_;
  std::unique_ptr<grpc::ServerCompletionQueue> cq_;
  std::unique_ptr<grpc::Server> server_;
};

auto main(int argc, char** argv) -> int {
  auto args = std::span(argv, std::size_t(argc));

  if (argc < 2) {
    std::cout << "usage: " << args[0] << " [server address]" << std::endl;
    std::cout << "   ex: " << args[0] << " 0.0.0.0:50051" << std::endl;
    return 1;
  }

  std::string server_address = args[1];
  AsyncServer server(server_address);
  server.Run();

  return 0;
}
