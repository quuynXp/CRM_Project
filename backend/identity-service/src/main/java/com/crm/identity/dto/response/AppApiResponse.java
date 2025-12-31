package com.crm.auth.dto.response;

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