# Spring Streaming App

## Description

This is a Spring Streaming App that can be run in Data Flow.

## Usage

Run this locally:

```
java -jar ./target/rmq-stream-sink-0.0.3-SNAPSHOT.jar \
  --server.port=8081 \
  --spring.cloud.stream.bindings.input.group=rmq-stream \
  --spring.cloud.stream.bindings.input.destination=rmq-stream.rmq-source
```
