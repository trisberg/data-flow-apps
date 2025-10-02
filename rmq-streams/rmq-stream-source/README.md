# Spring Streaming App

## Description

This is a Spring Streaming App that can be run in Data Flow.

## Usage

Run this locally:

```
java -jar ./target/rmq-stream-source-0.0.3-SNAPSHOT.jar \
  --server.port=8082 \
  --spring.cloud.stream.bindings.output.producer.requiredGroups=rmq-stream \
  --spring.cloud.stream.bindings.output.destination=rmq-stream.rmq-source
```
