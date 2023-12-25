package com.lundong.sync.service;

import com.lundong.sync.entity.BitableParam;

/**
 * @author RawChen
 * @date 2023-12-11 14:54
 */
public interface BitableService {


    /**
     * 生成审批单并处理状态
     *
     * @param bitable
     * @param bitableParam
     * @param <T>
     */
    <T> void processBitable(T bitable, BitableParam bitableParam);

    /**
     * 生成审批单并处理状态（冲销）
     *
     * @param bitable
     * @param bitableParam
     * @param <T>
     */
    <T> void processBitableWriteOff(T bitable, BitableParam bitableParam);
}
