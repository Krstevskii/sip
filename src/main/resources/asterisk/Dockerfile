# Use Ubuntu 16.04 as the base image
FROM ubuntu:16.04

# Install necessary packages
RUN apt-get update && apt-get install -y \
    build-essential \
    libsqlite3-dev \
    libncurses5-dev \
    libxml2-dev \
    libssl-dev \
    libjansson-dev \
    libedit-dev \
    uuid-dev \
    lsof \
    netcat \
    socat \
    net-tools \
    curl \
    nano \
    git \
    wget \
    && rm -rf /var/lib/apt/lists/*

# Download and install Asterisk 18
RUN git clone -b 18 https://github.com/asterisk/asterisk.git /usr/src/asterisk \
    && cd /usr/src/asterisk \
    && ./configure --with-jansson-bundled --with-sqlite3 --with-libedit \
    && make \
    && make install \
    && make samples \
    && make config \
    && rm -rf /usr/src/asterisk

COPY ./config /etc/asterisk

# Expose SIP and RTP ports for both UDP and TCP
EXPOSE 5060/udp
EXPOSE 5060/tcp
EXPOSE 8080/tcp
EXPOSE 8080/udp

# Set entrypoint
ENTRYPOINT ["asterisk", "-f"]
