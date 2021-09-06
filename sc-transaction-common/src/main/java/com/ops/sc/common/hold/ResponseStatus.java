package com.ops.sc.common.hold;


public class ResponseStatus {

    private static final int SC_OK = 200;

    private int statusCode;

    private String body;

    private ResponseStatus(int statusCode, String body) {
        this.statusCode = statusCode;
        this.body = body;
    }

    public static ResponseStatus successOf(String body) {
        return new ResponseStatus(SC_OK, body);
    }

    public static ResponseStatus failOf(int statusCode, String body) {
        return new ResponseStatus(statusCode, body);
    }

    public boolean isSuccess() {
        return statusCode == SC_OK;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getBody() {
        return body;
    }

    @Override
    public String toString() {
        return "ApiResultHolder{" + "statusCode=" + statusCode + ", body='" + body + '\'' + '}';
    }
}
