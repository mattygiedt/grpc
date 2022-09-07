cmake_minimum_required(VERSION 3.1...3.16.8 FATAL_ERROR)

find_package(Protobuf CONFIG REQUIRED)
find_package(gRPC CONFIG REQUIRED)

set( FLATC_COMPILER /usr/local/bin/flatc )
set( FLATC_OUTPUT_DIR ${CMAKE_CURRENT_BINARY_DIR}/include )

file( GLOB flatc_SRC "*.fbs" )
file( MAKE_DIRECTORY ${FLATC_OUTPUT_DIR} )

foreach( FILE ${flatc_SRC} )
    message(STATUS "processing flatbuffer file: ${FILE}")
    execute_process(COMMAND ${FLATC_COMPILER} --cpp --grpc --cpp-std c++17 -o ${FLATC_OUTPUT_DIR} ${FILE}
                    WORKING_DIRECTORY ${CMAKE_CURRENT_SOURCE_DIR}
                    COMMAND_ECHO STDOUT )
endforeach()


file( GLOB SOURCES
      "${CMAKE_CURRENT_BINARY_DIR}/include/*.cc" )

add_library( flatbuffer_messages ${SOURCES} )

set_target_properties( flatbuffer_messages
                       PROPERTIES
                       CXX_STANDARD 20
                       CXX_EXTENSIONS OFF
                       CXX_STANDARD_REQUIRED ON
                       CXX_POSITION_INDEPENDENT_CODE ON )

target_include_directories( flatbuffer_messages
                            PUBLIC
                            ${CMAKE_CURRENT_BINARY_DIR}/include )

target_link_libraries( flatbuffer_messages
                       gRPC::grpc++ )
