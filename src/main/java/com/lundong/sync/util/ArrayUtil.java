package com.lundong.sync.util;

import java.util.List;

/**
 * @author shuangquan.chen
 * @date 2023-12-05 14:58
 */
public class ArrayUtil {
    public static <T> boolean isEmpty(List<T> list) {
        if (list == null) {
            return true;
        }
        return list.isEmpty();
    }
}
