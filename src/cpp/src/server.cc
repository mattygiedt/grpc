#include <iostream>
#include <span>
#include <string>

#include "grpcpp/grpcpp.h"
#include "messages.grpc.fb.h"
#include "messages_generated.h"
#include "spdlog/spdlog.h"

// https://github.com/google/flatbuffers/blob/master/grpc/samples/greeter/server.cpp

using namespace com::mattygiedt::flatbuffer;

class GreeterServiceImpl final : public Greeter::Service {
  auto SayHello(grpc::ServerContext* /*context*/,
                const flatbuffers::grpc::Message<HelloRequest>* request_msg,
                flatbuffers::grpc::Message<HelloReply>* response_msg)
      -> grpc::Status override {
    flatbuffers::grpc::MessageBuilder mb_;

    // We call GetRoot to "parse" the message. Verification is already
    // performed by default. See the notes below for more details.
    const HelloRequest* request = request_msg->GetRoot();

    // Fields are retrieved as usual with FlatBuffers
    const std::string& name = request->name()->str();

    // 'flatbuffers::grpc::MessageBuilder' is a 'FlatBufferBuilder' with a
    // special allocator for efficient gRPC buffer transfer, but otherwise
    // usage is the same as usual.
    auto msg_offset = mb_.CreateString("Hello, " + name);
    auto hello_offset = CreateHelloReply(mb_, msg_offset);
    mb_.Finish(hello_offset);

    // The 'ReleaseMessage<T>()' function detaches the message from the
    // builder, so we can transfer the resopnse to gRPC while simultaneously
    // detaching that memory buffer from the builer.
    *response_msg = mb_.ReleaseMessage<HelloReply>();

    // Return an OK status.
    return grpc::Status::OK;
  }

  auto SayManyHellos(
      grpc::ServerContext* /*context*/,
      const flatbuffers::grpc::Message<ManyHellosRequest>* request_msg,
      grpc::ServerWriter<flatbuffers::grpc::Message<HelloReply>>* writer)
      -> grpc::Status override {
    // The streaming usage below is simply a combination of standard gRPC
    // streaming with the FlatBuffers usage shown above.
    const ManyHellosRequest* request = request_msg->GetRoot();
    const std::string& name = request->name()->str();
    int num_greetings = request->num_greetings();

    for (int i = 0; i < num_greetings; i++) {
      auto msg_offset = mb_.CreateString("Many hellos, " + name);
      auto hello_offset = CreateHelloReply(mb_, msg_offset);
      mb_.Finish(hello_offset);
      writer->Write(mb_.ReleaseMessage<HelloReply>());
    }

    return grpc::Status::OK;
  }

 private:
  flatbuffers::grpc::MessageBuilder mb_;
};

void RunServer(const std::string& server_address) {
  GreeterServiceImpl service;

  grpc::ServerBuilder builder;
  builder.AddListeningPort(server_address, grpc::InsecureServerCredentials());
  builder.RegisterService(&service);
  std::unique_ptr<grpc::Server> server(builder.BuildAndStart());

  spdlog::info("Server listening on {}", server_address);

  server->Wait();
}

auto main(int argc, char** argv) -> int {
  auto args = std::span(argv, std::size_t(argc));

  if (argc < 2) {
    std::cout << "usage: " << args[0] << " [server address]" << std::endl;
    std::cout << "   ex: " << args[0] << " 0.0.0.0:50051" << std::endl;
    return 1;
  }

  std::string server_address = args[1];
  RunServer(server_address);

  return 0;
}
