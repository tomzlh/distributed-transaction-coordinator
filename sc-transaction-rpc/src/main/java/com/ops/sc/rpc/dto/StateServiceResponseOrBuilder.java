// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: stateService.proto

package com.ops.sc.rpc.dto;

public interface StateServiceResponseOrBuilder extends
    // @@protoc_insertion_point(interface_extends:StateServiceResponse)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>.ParentResponse baseResponse = 1;</code>
   */
  boolean hasBaseResponse();
  /**
   * <code>.ParentResponse baseResponse = 1;</code>
   */
  com.ops.sc.rpc.dto.ParentResponse getBaseResponse();
  /**
   * <code>.ParentResponse baseResponse = 1;</code>
   */
  com.ops.sc.rpc.dto.ParentResponseOrBuilder getBaseResponseOrBuilder();

  /**
   * <code>.google.protobuf.UInt32Value status = 2;</code>
   */
  boolean hasStatus();
  /**
   * <code>.google.protobuf.UInt32Value status = 2;</code>
   */
  com.google.protobuf.UInt32Value getStatus();
  /**
   * <code>.google.protobuf.UInt32Value status = 2;</code>
   */
  com.google.protobuf.UInt32ValueOrBuilder getStatusOrBuilder();
}
