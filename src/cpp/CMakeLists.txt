cmake_minimum_required(VERSION 3.1...3.16.8 FATAL_ERROR)

find_package(spdlog REQUIRED)
find_package(Protobuf CONFIG REQUIRED)
find_package(gRPC CONFIG REQUIRED)

add_executable( client "./src/client.cc" )

set_target_properties( client
                       PROPERTIES
                       CXX_STANDARD 20
                       CXX_EXTENSIONS OFF
                       CXX_STANDARD_REQUIRED ON
                       CXX_POSITION_INDEPENDENT_CODE ON )

target_include_directories( client
                            PUBLIC
                            ${CMAKE_CURRENT_SOURCE_DIR}/include )

target_link_libraries( client
                       PUBLIC
                       flatbuffer_messages
                       gRPC::grpc++
                       spdlog::spdlog )

add_executable( async_client "./src/async_client.cc" )

set_target_properties( async_client
                       PROPERTIES
                       CXX_STANDARD 20
                       CXX_EXTENSIONS OFF
                       CXX_STANDARD_REQUIRED ON
                       CXX_POSITION_INDEPENDENT_CODE ON )

target_include_directories( async_client
                            PUBLIC
                            ${CMAKE_CURRENT_SOURCE_DIR}/include )

target_link_libraries( async_client
                       PUBLIC
                       flatbuffer_messages
                       gRPC::grpc++
                       spdlog::spdlog )

add_executable( server "./src/server.cc" )

set_target_properties( server
                       PROPERTIES
                       CXX_STANDARD 20
                       CXX_EXTENSIONS OFF
                       CXX_STANDARD_REQUIRED ON
                       CXX_POSITION_INDEPENDENT_CODE ON )

target_include_directories( server
                            PUBLIC
                            ${CMAKE_CURRENT_SOURCE_DIR}/include )

target_link_libraries( server
                       PUBLIC
                       flatbuffer_messages
                       gRPC::grpc++
                       spdlog::spdlog )

add_executable( async_server "./src/async_server.cc" )

set_target_properties( async_server
                       PROPERTIES
                       CXX_STANDARD 20
                       CXX_EXTENSIONS OFF
                       CXX_STANDARD_REQUIRED ON
                       CXX_POSITION_INDEPENDENT_CODE ON )

target_include_directories( async_server
                            PUBLIC
                            ${CMAKE_CURRENT_SOURCE_DIR}/include )

target_link_libraries( async_server
                       PUBLIC
                       flatbuffer_messages
                       gRPC::grpc++
                       spdlog::spdlog )
