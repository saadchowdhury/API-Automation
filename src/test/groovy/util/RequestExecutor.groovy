package util

import kong.unirest.HttpResponse
import kong.unirest.Unirest
import kong.unirest.UnirestInstance
import model.ApiRequest

class RequestExecutor {
    static HttpResponse executeRequest(ApiRequest apiRequest) {

        String endpoint = apiRequest.getEndpoint()
        String method = apiRequest.getMethod()
        String url = apiRequest.getUrl() + (endpoint? endpoint: '')
        Map requestHeaders = apiRequest.getHeaderParams()
        Map queryParameters = apiRequest.getQueryParams()
        String requestBody = apiRequest.getRequestBody()


        HttpResponse httpResponse
        UnirestInstance unirestInstance = Unirest.spawnInstance()
        switch (method.toUpperCase()) {
            case "GET":
            case "get":
                httpResponse = get(unirestInstance, url, requestHeaders, queryParameters)
                print(httpResponse)
                break
            case "POST":
            case "post":
                httpResponse = post(unirestInstance, url, requestHeaders, queryParameters, requestBody)
                break
            case 'PUT':
            case 'put':
                httpResponse = put(unirestInstance, url, requestHeaders, queryParameters, requestBody)
                break
            case 'DELETE':
            case 'delete':
                httpResponse = Delete(unirestInstance, url, requestHeaders, queryParameters)
                break
            default:
                throw new Exception("Invalid request method: ${method}")
        }
        unirestInstance.shutDown()
        httpResponse
    }
    private static HttpResponse post (UnirestInstance unirestInstance, String url, Map requestHeaders,
                                      Map queryParameters, String requestBody) throws IOException {
        def response = unirestInstance.post(url).
                headers(requestHeaders)
                .queryString(queryParameters)
                .body(requestBody)
                .asString()
        response
        }

    private static HttpResponse put (UnirestInstance unirestInstance, String url, Map requestHeaders,
                                      Map queryParameters, String requestBody) throws IOException {
        def response = unirestInstance.put(url).
                headers(requestHeaders)
                .queryString(queryParameters)
                .body(requestBody)
                .asString()
        response
    }

    private static HttpResponse get(UnirestInstance unirestInstance, String url,
                                    Map requestHeaders, Map queryParameters) throws IOException {
            def response = unirestInstance.get(url)
                    .headers(requestHeaders)
                    .queryString(queryParameters)
                    .asString()
            response
//        }


    }

    private static  HttpResponse Delete(UnirestInstance unirestInstance, String url, Map requestHeaders, Map queryParameters){
        def response = unirestInstance.delete(url)
                .headers(requestHeaders)
                .queryString(queryParameters)
                .asString()
        response
    }
}
