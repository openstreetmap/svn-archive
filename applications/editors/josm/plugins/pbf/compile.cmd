@echo off
.\tools\protoc.exe --proto_path=proto;protobuf --java_out=gen protobuf\descriptor.proto proto\fileformat.proto proto\osmformat.proto