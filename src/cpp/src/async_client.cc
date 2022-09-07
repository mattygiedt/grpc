#include <iostream>
#include <span>
#include <string>

#include "grpcpp/grpcpp.h"
#include "messages.grpc.fb.h"
#include "messages_generated.h"
#include "spdlog/spdlog.h"

// https://github.com/google/flatbuffers/blob/master/grpc/samples/greeter/client.cpp
// https://github.com/grpc/grpc/blob/v1.46.3/examples/cpp/helloworld/greeter_async_client.cc

using namespace com::mattygiedt::flatbuffer;

class GreeterClient {
 private:
  using ChannelPtr = std::shared_ptr<grpc::Channel>;
  using GreeterPtr = std::unique_ptr<Greeter::Stub>;
  using Callback = std::function<void(const std::string&)>;
  using HelloReplyMessage = flatbuffers::grpc::Message<HelloReply>;
  using ClientAsyncResponseReaderPtr = std::unique_ptr<
      grpc::ClientAsyncResponseReader<flatbuffers::grpc::Message<HelloReply>>>;

 public:
  explicit GreeterClient(const ChannelPtr& channel)
      : stub_(Greeter::NewStub(channel)) {}

  auto SayHelloAsync(const std::string& name) -> std::string {
    flatbuffers::grpc::MessageBuilder mb;
    auto name_offset = mb.CreateString(name);
    auto request_offset = CreateHelloRequest(mb, name_offset);
    mb.Finish(request_offset);
    auto request_msg = mb.ReleaseMessage<HelloRequest>();

    // The producer-consumer queue we use to communicate asynchronously with the
    // gRPC runtime.
    grpc::CompletionQueue cq;

    // Storage for the status of the RPC upon completion.
    grpc::Status status;

    // Context for the client. It could be used to convey extra information to
    // the server and/or tweak certain RPC behaviors.
    grpc::ClientContext context;

    // Container for the data we expect from the server.
    HelloReplyMessage response_msg;

    // stub_->PrepareAsyncSayHello() creates an RPC object, returning
    // an instance to store in "call" but does not actually start the RPC.
    // Because we are using the asynchronous API, we need to hold on to
    // the "call" instance in order to get updates on the ongoing RPC.
    ClientAsyncResponseReaderPtr rpc(
        stub_->PrepareAsyncSayHello(&context, request_msg, &cq));

    // StartCall initiates the RPC call
    rpc->StartCall();

    // Request that, upon completion of the RPC, "reply" be updated with the
    // server's response; "status" with the indication of whether the operation
    // was successful. Tag the request with the pointer address of this RPC.
    auto rpc_addr = reinterpret_cast<std::uintptr_t>(rpc.get());  // NOLINT
    rpc->Finish(&response_msg, &status, &rpc_addr);

    void* got_tag = nullptr;
    bool ok = false;

    // Block until the next result is available in the completion queue "cq".
    // The return value of Next should always be checked. This return value
    // tells us whether there is any kind of event or the cq_ is shutting down.
    cq.Next(&got_tag, &ok);

    if (status.ok()) {
      const auto* response = response_msg.GetRoot();
      return response->message()->str();
    }

    spdlog::warn(" SayHelloAsync {}: {}", status.error_code(),
                 status.error_message());

    return "SayHelloAsync RPC failed";
  }

 private:
  GreeterPtr stub_;
};

auto main(int argc, char** argv) -> int {
  auto args = std::span(argv, std::size_t(argc));

  if (argc < 2) {
    std::cout << "usage: " << args[0] << " [server address]" << std::endl;
    std::cout << "   ex: " << args[0] << " localhost:50051" << std::endl;
    return 1;
  }

  std::string server_address = args[1];
  spdlog::info("server addr: {}", server_address);

  auto channel =
      grpc::CreateChannel(server_address, grpc::InsecureChannelCredentials());

  GreeterClient greeter(channel);

  const auto* request = "World";
  auto response = greeter.SayHelloAsync(request);

  spdlog::info("SayHelloAsync request: {}, response: {}", request, response);
}
