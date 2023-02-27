package model

import groovy.transform.builder.Builder

@Builder
class ApiRequest {
    String method
    String url
    Map headerParams
    Map queryParams
    String requestBody
    String endpoint
    String ignore

    @Override
    public String toString() {
        return "ApiRequest{" +
                "method='" + method + '\'' +
                ", url='" + url + '\'' +
                ", headerParams=" + headerParams +
                ", queryParams=" + queryParams +
                ", requestBody='" + requestBody + '\'' +
                ", endpoint='" + endpoint + '\'' +
                ", ignore='" + ignore + '\'' +
                '}';
    }
}
