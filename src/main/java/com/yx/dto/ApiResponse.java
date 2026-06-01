package com.yx.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 统一的API响应格式
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 状态码: 0=成功, 其他=失败
     */
    private Integer code;

    /**
     * 响应信息
     */
    private String msg;

    /**
     * 获取信息（兼容 message 字段）
     */
    public String getMessage() {
        return msg;
    }

    /**
     * 数据总数（用于分页）
     */
    private Long count;

    /**
     * 数据体
     */
    private T data;

    /**
     * 成功响应（无数据）
     */
    public static <T> ApiResponse<T> ok() {
        return ApiResponse.<T>builder()
                .code(0)
                .msg("请求成功")
                .build();
    }

    /**
     * 成功响应（有数据）
     */
    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder()
                .code(0)
                .msg("请求成功")
                .data(data)
                .build();
    }

    /**
     * 成功响应（分页数据）
     */
    public static <T> ApiResponse<T> ok(Long count, T data) {
        return ApiResponse.<T>builder()
                .code(0)
                .msg("请求成功")
                .count(count)
                .data(data)
                .build();
    }

    /**
     * 失败响应
     */
    public static <T> ApiResponse<T> fail(String msg) {
        return ApiResponse.<T>builder()
                .code(400)
                .msg(msg)
                .build();
    }

    /**
     * 失败响应（自定义状态码）
     */
    public static <T> ApiResponse<T> fail(Integer code, String msg) {
        return ApiResponse.<T>builder()
                .code(code)
                .msg(msg)
                .build();
    }
}
