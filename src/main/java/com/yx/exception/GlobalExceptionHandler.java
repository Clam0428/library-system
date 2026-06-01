package com.yx.exception;

import com.yx.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> handleBusinessException(BusinessException ex) {
        log.warn("业务异常: {}", ex.getMessage());
        return ApiResponse.fail(ex.getCode(), ex.getMessage());
    }

    /**
     * 处理其他异常
     */
    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleException(Exception ex) {
        log.error("系统异常", ex);
        return ApiResponse.fail(500, "服务器内部错误，请稍后重试");
    }

    /**
     * 处理NPE
     */
    @ExceptionHandler(NullPointerException.class)
    public ApiResponse<Void> handleNullPointerException(NullPointerException ex) {
        log.error("空指针异常", ex);
        return ApiResponse.fail(500, "空指针异常");
    }
}
