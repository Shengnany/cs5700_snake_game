# cs5700_assignment3

## Introduction

- User will press up/left/right/down key to control the snake
- Game speed is set to 200 ms to avoid snake running too fast and it hits the wall before you can control it
- SnakeApp is the entry point and is named snake_app in bazel build file

## Usage

```
server running:  bazel run snake_app -- start-server
after which, the command line will print out the sever ip
client1 running:  bazel run snake_app -- create 123 <client_name1> <server_ip> <client1_port> 
client2 running:  bazel run snake_app -- join 123 <client_name2> <server_ip> <client2_port>

// Sample Commands (server IP is 192.168.0.1)
bazel run snake_app -- start_server 
bazel run snake_app -- create 123 snakemaker 192.168.0.1 8282
bazel run snake_app  -- join 123 masterkiller 192.168.0.1 8383
```
