package com.baker.asr.demo.permission;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Create by hsj55
 * 2020/2/24
 * <p>
 * 标识权限获取成功
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface PermissionSuccess {
    int requestCode();
}
