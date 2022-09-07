#include <iostream>
#include <span>
#include <string>

#include "grpcpp/grpcpp.h"
#include "messages.grpc.fb.h"
#include "messages_generated.h"
#include "spdlog/spdlog.h"

// https://github.com/google/flatbuffers/blob/master/grpc/samples/greeter/client.cpp

using namespace com::mattygiedt::flatbuffer;

class GreeterClient {
 private:
  using ChannelPtr = std::shared_ptr<grpc::Channel>;
  using GreeterPtr = std::unique_ptr<Greeter::Stub>;
  using Callback = std::function<void(const std::string&)>;

 public:
  explicit GreeterClient(const ChannelPtr& channel)
      : stub_(Greeter::NewStub(channel)) {}

  auto SayHello(const std::string& name) -> std::string {
    flatbuffers::grpc::MessageBuilder mb;
    auto name_offset = mb.CreateString(name);
    auto request_offset = CreateHelloRequest(mb, name_offset);
    mb.Finish(request_offset);
    auto request_msg = mb.ReleaseMessage<HelloRequest>();

    grpc::ClientContext context;
    flatbuffers::grpc::Message<HelloReply> response_msg;

    auto status = stub_->SayHello(&context, request_msg, &response_msg);

    if (status.ok()) {
      const auto* response = response_msg.GetRoot();
      return response->message()->str();
    }

    spdlog::warn(" SayHello {}: {}", status.error_code(),
                 status.error_message());

    return "RPC failed";
  }

  auto SayManyHellos(const std::string& name, int num_greetings,
                     Callback&& callback) -> void {
    flatbuffers::grpc::MessageBuilder mb;
    auto name_offset = mb.CreateString(name);
    auto request_offset =
        CreateManyHellosRequest(mb, name_offset, num_greetings);
    mb.Finish(request_offset);
    auto request_msg = mb.ReleaseMessage<ManyHellosRequest>();

    flatbuffers::grpc::Message<HelloReply> response_msg;

    grpc::ClientContext context;

    auto stream = stub_->SayManyHellos(&context, request_msg);
    while (stream->Read(&response_msg)) {
      const HelloReply* response = response_msg.GetRoot();
      callback(response->message()->str());
    }

    auto status = stream->Finish();
    if (!status.ok()) {
      spdlog::warn("SayManyHellos {}: {}", status.error_code(),
                   status.error_message());
      callback("RPC failed");
    }
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
  auto response = greeter.SayHello(request);

  spdlog::info("SayHello[1] request: {}, response: {}", request, response);

  response = greeter.SayHello(request);

  spdlog::info("SayHello[2] request: {}, response: {}", request, response);

  constexpr static int kNumGreetings = 10;
  greeter.SayManyHellos(request, kNumGreetings, [](const std::string& message) {
    spdlog::info("SayManyHellos received: {}", message);
  });
}
