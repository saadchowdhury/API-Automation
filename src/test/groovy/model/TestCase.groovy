package model

import groovy.transform.builder.Builder

@Builder
class TestCase {
    String testNo
    String testDescription
    ApiRequest apiRequest
    int expectedStatus
    String expectedResponseBody
    String validation
    String requestCurl

    @Override
    public String toString() {
        return "TestCase{" +
                "testNo='" + testNo + '\'' +
                ", testDescription='" + testDescription + '\'' +
                ", apiRequest=" + apiRequest +
                ", expectedStatus=" + expectedStatus +
                ", expectedResponseBody='" + expectedResponseBody + '\'' +
                ", validation='" + validation + '\'' +
                ", requestCurl='" + requestCurl + '\'' +
                '}';
    }
}
