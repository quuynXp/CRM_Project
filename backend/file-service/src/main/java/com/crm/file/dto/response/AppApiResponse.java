package com.crm.file.dto.response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppApiResponse<T> {
    private int code;
    private String message;
    private T result;
}