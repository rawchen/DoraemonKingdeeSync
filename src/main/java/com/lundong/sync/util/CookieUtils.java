package com.lundong.sync.util;

import javax.servlet.http.Cookie;
import java.net.HttpCookie;
import java.util.List;

/**
 * @author shuangquan.chen
 * @date 2024-05-25 14:10
 */
public class CookieUtils {

    /**
     * 查找指定名称的Cookie 对象
     *
     * @param name
     * @param cookies
     * @return
     */
    public static Cookie findCookie(String name, Cookie[] cookies) {
        if (name == null || cookies == null || cookies.length == 0) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName())) {
                return cookie;
            }
        }
        return null;
    }

    /**
     * 删除指定名称的Cookie 对象
     *
     * @param cookies
     * @return
     */
    public static void clearKingdeeCookie(List<HttpCookie> cookies) {
        if (cookies == null || cookies.size() == 0) {
            return;
        }
        for (HttpCookie cookie : cookies) {
            if ("ASP.NET_SessionId".equals(cookie.getName()) || "kdservice-sessionid".equals(cookie.getName())) {
                cookie.setMaxAge(0);
            }
        }
    }
}
