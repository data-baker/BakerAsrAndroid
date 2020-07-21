package com.baker.asr.demo.permission;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Create by hsj55
 * 2020/2/24
 * <p>
 * 标识权限获取失败
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PermissionFail {
    int requestCode();
}
