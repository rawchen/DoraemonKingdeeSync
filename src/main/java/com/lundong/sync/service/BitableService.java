package com.lundong.sync.service;

/**
 * @author RawChen
 * @date 2023-12-11 14:54
 */
public interface BitableService {


    /**
     * 生成审批单并处理状态
     *
     * @param bitable
     * @param appToken
     * @param tableId
     * @param recordId
     * @param <T>
     */
    <T> void processBitable(T bitable, String appToken, String tableId, String recordId);
}
