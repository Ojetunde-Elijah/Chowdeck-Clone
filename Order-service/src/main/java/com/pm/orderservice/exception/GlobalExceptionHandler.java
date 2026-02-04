package com.pm.orderservice.exception;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(StatusRuntimeException.class)
    public ResponseEntity<String> handleGrpcException(StatusRuntimeException e) {
        // Convert gRPC "NOT_FOUND" to HTTP 404
        if (e.getStatus().getCode() == Status.Code.NOT_FOUND) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Food item not found in our catalog.");
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Service communication error.");
    }
}