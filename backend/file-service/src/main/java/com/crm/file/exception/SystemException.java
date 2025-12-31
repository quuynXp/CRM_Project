package com.crm.file.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SystemException extends AppException{
    private ErrorCode errorCode;
}
