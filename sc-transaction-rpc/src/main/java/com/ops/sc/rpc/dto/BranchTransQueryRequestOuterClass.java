// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: branchTransQueryRequest.proto

package com.ops.sc.rpc.dto;

public final class BranchTransQueryRequestOuterClass {
  private BranchTransQueryRequestOuterClass() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_BranchTransQueryRequestList_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_BranchTransQueryRequestList_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_BranchTransQueryRequest_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_BranchTransQueryRequest_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_BranchTransInfoList_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_BranchTransInfoList_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_BranchTransInfo_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_BranchTransInfo_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\035branchTransQueryRequest.proto\032\036google/" +
      "protobuf/wrappers.proto\"Y\n\033BranchTransQu" +
      "eryRequestList\022:\n\030branchTransQueryReques" +
      "ts\030\001 \003(\0132\030.BranchTransQueryRequest\"9\n\027Br" +
      "anchTransQueryRequest\022\021\n\trequestId\030\001 \001(\t" +
      "\022\013\n\003tid\030\002 \001(\t\"A\n\023BranchTransInfoList\022*\n\020" +
      "branchTransInfos\030\001 \003(\0132\020.BranchTransInfo" +
      "\"\271\001\n\017BranchTransInfo\022\020\n\010branchId\030\001 \001(\t\022\022" +
      "\n\nbranchName\030\002 \001(\t\022\021\n\trequestId\030\003 \001(\t\022\013\n" +
      "\003tid\030\004 \001(\t\022\020\n\010parentId\030\005 \003(\t\022\016\n\006params\030\006" +
      " \001(\014\022,\n\006status\030\007 \001(\0132\034.google.protobuf.U" +
      "Int32Value\022\020\n\010tranCode\030\010 \001(\tB\035\n\022com.ops." +
      "sc.rpc.dtoP\001\242\002\004HLWSb\006proto3"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
        new com.google.protobuf.Descriptors.FileDescriptor.    InternalDescriptorAssigner() {
          public com.google.protobuf.ExtensionRegistry assignDescriptors(
              com.google.protobuf.Descriptors.FileDescriptor root) {
            descriptor = root;
            return null;
          }
        };
    com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          com.google.protobuf.WrappersProto.getDescriptor(),
        }, assigner);
    internal_static_BranchTransQueryRequestList_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_BranchTransQueryRequestList_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_BranchTransQueryRequestList_descriptor,
        new java.lang.String[] { "BranchTransQueryRequests", });
    internal_static_BranchTransQueryRequest_descriptor =
      getDescriptor().getMessageTypes().get(1);
    internal_static_BranchTransQueryRequest_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_BranchTransQueryRequest_descriptor,
        new java.lang.String[] { "RequestId", "Tid", });
    internal_static_BranchTransInfoList_descriptor =
      getDescriptor().getMessageTypes().get(2);
    internal_static_BranchTransInfoList_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_BranchTransInfoList_descriptor,
        new java.lang.String[] { "BranchTransInfos", });
    internal_static_BranchTransInfo_descriptor =
      getDescriptor().getMessageTypes().get(3);
    internal_static_BranchTransInfo_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_BranchTransInfo_descriptor,
        new java.lang.String[] { "BranchId", "BranchName", "RequestId", "Tid", "ParentId", "Params", "Status", "TranCode", });
    com.google.protobuf.WrappersProto.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}