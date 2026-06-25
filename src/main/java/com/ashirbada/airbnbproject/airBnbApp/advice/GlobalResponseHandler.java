package com.ashirbada.airbnbproject.airBnbApp.advice;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import static org.aspectj.weaver.tools.cache.SimpleCacheFactory.path;

@RestControllerAdvice
public class GlobalResponseHandler implements ResponseBodyAdvice<Object> {
//    @Override
//    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
//        return true;
//    }
@Override
public boolean supports(MethodParameter returnType,
                        Class<? extends HttpMessageConverter<?>> converterType) {

    return MappingJackson2HttpMessageConverter.class
            .isAssignableFrom(converterType);
}

    @Nullable
    @Override
    public Object beforeBodyWrite(@Nullable Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        String requestPath = request.getURI().getPath();

        // Exclude Swagger/OpenAPI endpoints
        if (requestPath.contains("/v3/api-docs")
                || requestPath.contains("/swagger-ui")) {
            return body;
        }
        // VERY IMPORTANT FIX
        // Skip byte array converters
        if (ByteArrayHttpMessageConverter.class.isAssignableFrom(selectedConverterType)) {
            return body;
        }
        if(body instanceof ApiResponse<?>){
            return body;
        }
        return new ApiResponse<>(body);
    }
}

