package com.b02.peep_it.common.model;

import com.b02.peep_it.common.util.HttpLoggingUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.util.Map;

public class HttpLogMessage {
    private final String httpMethod;
    private final String requestUri;
    private final HttpStatus httpStatus;
    private final String clientIp;
    private final double elapsedTime;
    private final Map<String, String> headers;
    private final Map<String, String> requestParam;
    private final String requestBody;
    private final String responseBody;

    public HttpLogMessage(String httpMethod, String requestUri, HttpStatus httpStatus, String clientIp, double elapsedTime,
                          Map<String, String> headers, Map<String, String> requestParam, String requestBody, String responseBody) {
        this.httpMethod = httpMethod;
        this.requestUri = requestUri;
        this.httpStatus = httpStatus;
        this.clientIp = clientIp;
        this.elapsedTime = elapsedTime;
        this.headers = headers;
        this.requestParam = requestParam;
        this.requestBody = requestBody;
        this.responseBody = responseBody;
    }

    public static HttpLogMessage createInstance(ContentCachingRequestWrapper requestWrapper,
                                                ContentCachingResponseWrapper responseWrapper,
                                                double elapsedTime) {
        return new HttpLogMessage(
                requestWrapper.getMethod(),
                requestWrapper.getRequestURI(),
                HttpStatus.valueOf(responseWrapper.getStatus()),
                HttpLoggingUtils.getClientIp(requestWrapper),
                elapsedTime,
                HttpLoggingUtils.getRequestHeaders(requestWrapper),
                HttpLoggingUtils.getRequestParams(requestWrapper),
                HttpLoggingUtils.getRequestBody(requestWrapper),
                HttpLoggingUtils.getResponseBody(responseWrapper)
        );
    }

    public String toPrettierLog() {
        return "\n" +
                "[REQUEST] " + httpMethod + " " + requestUri + " " + httpStatus + " (" + elapsedTime + "s)\n" +
                ">> CLIENT_IP: " + clientIp + "\n" +
                ">> HEADERS: " + headers + "\n" +
                ">> REQUEST_PARAM: " + requestParam + "\n" +
                ">> REQUEST_BODY: " + requestBody + "\n" +
                ">> RESPONSE_BODY: " + responseBody;
    }
}

