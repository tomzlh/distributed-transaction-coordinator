package com.ops.sc.core.util;


import com.google.protobuf.ByteString;
import com.ops.sc.common.enums.ClientErrorCode;
import com.ops.sc.common.exception.ScClientException;

import java.io.*;
import java.nio.charset.Charset;

import static com.ops.sc.common.constant.Constants.SC_DEFAULT_CHARSET;


public class GrpcUtils {

    public static ByteString toByteString(String text) {
        return toByteString(text.getBytes(SC_DEFAULT_CHARSET));
    }

    public static ByteString toByteString(byte[] bytes) {
        return ByteString.copyFrom(bytes);
    }

    public static ByteString toByteString(Object[] objectArray) throws ScClientException{
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(objectArray);
            oos.flush();
            byte[] byteArray = bos.toByteArray();
            oos.close();
            bos.close();
            return ByteString.copyFrom(byteArray);
        } catch (IOException e) {
            throw new ScClientException(ClientErrorCode.CLIENT_REQUEST_FAILED,
                    "Assemble tcc register uData param failed!", e);
        }
    }

    public static Object[] toObjectArray(byte[] bytes) throws ScClientException{
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(bis);
            Object[] objArray = (Object[]) ois.readObject();
            ois.close();
            bis.close();
            return objArray;
        } catch (IOException | ClassNotFoundException e) {
            throw new ScClientException(ClientErrorCode.CLIENT_REQUEST_FAILED, "Parse tcc invoke params failed!",
                    e);
        }
    }

    public static String toString(byte[] bytes) {
        return toStringInternal(toByteString(bytes));
    }

    public static Object[] toObjectArray(ByteString byteString) throws ScClientException{
        return toObjectArray(toByteArray(byteString));
    }

    public static byte[] toByteArray(ByteString byteString) {
        return byteString.toByteArray();
    }

    public static String toString(ByteString byteString, Charset charset) {
        return byteString.toString(charset);
    }

    public static String toStringInternal(ByteString byteString) {
        return toString(byteString, SC_DEFAULT_CHARSET);
    }

}
