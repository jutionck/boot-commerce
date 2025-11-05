package com.github.jutionck.utils;

import com.github.jutionck.dto.response.ApiResponse;
import com.github.jutionck.dto.response.ErrorResponse;
import com.github.jutionck.dto.response.PagingResponse;
import com.github.jutionck.dto.response.StatusResponse;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class ResponseUtil {
    public static <T> ResponseEntity<ApiResponse<T>> buildSingleResponse(
            HttpStatus httpStatus,
            String message,
            T data) {
        StatusResponse status = StatusResponse.builder()
                .code(httpStatus.value())
                .description(message)
                .build();
        ApiResponse<T> response = ApiResponse.<T>builder()
                .status(status)
                .data(data)
                .build();

        return ResponseEntity.status(httpStatus).body(response);
    }

    public static <T> ResponseEntity<ApiResponse<List<T>>> buildPagedResponse(
            HttpStatus httpStatus,
            String message,
            Page<T> page) {
        StatusResponse status = StatusResponse.builder()
                .code(httpStatus.value())
                .description(message)
                .build();
        PagingResponse paging = PagingResponse.builder()
                .page(page.getNumber() + 1)
                .rowsPerPage(page.getSize())
                .totalRows(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();

        ApiResponse<List<T>> response = ApiResponse.<List<T>>builder()
                .status(status)
                .data(page.getContent())
                .paging(paging)
                .build();

        return ResponseEntity.status(httpStatus).body(response);
    }

    public static <T> ResponseEntity<ApiResponse<List<T>>> buildPageResponse(
            HttpStatus httpStatus,
            String message,
            Page<T> page) {
        return buildPagedResponse(httpStatus, message, page);
    }

    public static ResponseEntity<ErrorResponse> buildErrorResponse(
            HttpStatus httpStatus,
            String message,
            List<String> errors) {

        StatusResponse status = StatusResponse.builder()
                .code(httpStatus.value())
                .description(message)
                .build();
        ErrorResponse response = ErrorResponse.builder()
                .status(status)
                .errors(errors)
                .build();

        return ResponseEntity.status(httpStatus).body(response);
    }
}
