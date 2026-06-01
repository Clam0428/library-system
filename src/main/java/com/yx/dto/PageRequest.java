package com.yx.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分页请求参数DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageRequest {
    /**
     * 当前页码（默认1）
     */
    private Integer page;

    /**
     * 每页大小（默认10）
     */
    private Integer limit;

    /**
     * 获取实际page，确保最小值为1
     */
    public Integer getPage() {
        if (page == null || page < 1) {
            return 1;
        }
        return page;
    }

    /**
     * 获取实际limit，确保范围在1-100之间
     */
    public Integer getLimit() {
        if (limit == null || limit < 1) {
            return 10;
        }
        if (limit > 100) {
            return 100;
        }
        return limit;
    }
}
