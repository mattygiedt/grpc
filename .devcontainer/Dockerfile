# docker build -f Dockerfile -t grpc:local .
# docker run --network=host -it --rm --entrypoint /bin/bash --mount type=bind,source="$(pwd)",target=/app grpc:local
FROM gcc:12.2

WORKDIR /

RUN echo "Installing development tools ..."; \
    set -ex; \
    apt-get update && export DEBIAN_FRONTEND=noninteractive; \
    apt-get install -y --no-install-recommends --fix-missing \
        build-essential \
        vim \
        gdb \
        htop \
        cmake \
        python-dev \
        python3 \
        python3-venv \
        python3-pip \
        python3-setuptools \
        autotools-dev \
        clang-tidy \
        clang-format;

RUN echo "Installing openjdk-17 ..."; \
    set -ex; \
    wget https://download.java.net/java/GA/jdk17.0.2/dfd4a8d0985749f896bed50d7138ee7f/8/GPL/openjdk-17.0.2_linux-x64_bin.tar.gz; \
    tar -xvf openjdk-17.*; \
    cd jdk-17.0.2; \
    mkdir -p /usr/local/openjdk-17; \
    mv * /usr/local/openjdk-17; \
    rm -rf /openjdk-17.*;

RUN echo "Installing Maven 3.9.4 ..."; \
    set -ex; \
    wget https://dlcdn.apache.org/maven/maven-3/3.9.4/binaries/apache-maven-3.9.4-bin.tar.gz; \
    tar xzvf apache-maven-3.9.4-bin.tar.gz; \
    cd apache-maven-3.9.4; \
    mkdir -p /usr/local/maven; \
    mv * /usr/local/maven; \
    rm -rf /apache-maven-3.9.4-bin.tar.gz;

RUN echo "Upgrade cmake ..." && \
    pip3 install cmake --upgrade

RUN echo "Installing google test ..."; \
    set -ex; \
    git clone https://github.com/google/googletest.git; \
    cd googletest; \
    mkdir build; \
    cd build; \
    cmake -G "Unix Makefiles" -DCMAKE_BUILD_TYPE=RelWithDebInfo ..; \
    make -j4 install; \
    rm -rf /googletest;

RUN echo "Installing boost:1.77 ..."; \
    set -ex; \
    wget -O boost_1_77_0.tar.gz https://sourceforge.net/projects/boost/files/boost/1.77.0/boost_1_77_0.tar.gz/download; \
    tar xzvf boost_1_77_0.tar.gz; \
    cd boost_1_77_0/; \
    ./bootstrap.sh --prefix=/usr/local; \
    ./b2 --with=all -j4 release install; \
    rm -rf /boost_1_77_0*;

RUN echo "Installing spdlog ..."; \
    set -ex; \
    git clone https://github.com/gabime/spdlog.git; \
    cd spdlog; \
    mkdir build; \
    cd build; \
    cmake -G "Unix Makefiles" -DCMAKE_BUILD_TYPE=RelWithDebInfo ..; \
    make -j4 install; \
    rm -rf /spdlog;

RUN echo "Installing eventpp ..."; \
    set -ex; \
    git clone https://github.com/wqking/eventpp.git; \
    cp -r eventpp/include/eventpp /usr/local/include; \
    rm -rf /eventpp;

RUN echo "Installing grpc ..."; \
    set -ex; \
    git clone --recurse-submodules -b v1.46.3 --depth 1 --shallow-submodules https://github.com/grpc/grpc; \
    cd grpc; \
    mkdir -p cmake/build; \
    cd cmake/build; \
    cmake -G "Unix Makefiles" -DCMAKE_BUILD_TYPE=RelWithDebInfo -DgRPC_INSTALL=ON -DgRPC_BUILD_TESTS=OFF ../..; \
    make -j4 install; \
    rm -rf /grpc;

RUN echo "Installing flatbuffers ..."; \
    set -ex; \
    git clone https://github.com/google/flatbuffers.git; \
    cd flatbuffers; \
    mkdir build; \
    cd build; \
    cmake -G "Unix Makefiles" -DFLATBUFFERS_BUILD_TESTS=OFF -DCMAKE_BUILD_TYPE=RelWithDebInfo ..; \
    make -j4 install; \
    rm -rf /flatbuffers;

RUN echo "Cleanup docker image..."; \
    set -ex; \
    apt-get autoremove -y; \
    apt-get clean -y; \
    rm -rf /var/lib/apt/lists/*;

 RUN echo "export ENV VARIABLES ...";
 RUN echo '\
 export LD_LIBRARY_PATH=/usr/local/lib \n\
 export JAVA_HOME=/usr/local/openjdk-17 \n\
 export M2_HOME=/usr/local/maven \n\
 export M2=${M2_HOME}/bin \n\
 export PATH=${JAVA_HOME}/bin:${M2}:${PATH};' >> /root/.bashrc
