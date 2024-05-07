package com.murong.rpc.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * description
 *
 * @author yaochuang 2024/05/07 15:22
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OptionalVal<T> {

    /**
     * 静态生成
     *
     * @param data 数据实体
     * @param <E>  泛型
     * @return 示例
     */
    public static <E> OptionalVal<E> of(E data) {
        OptionalVal<E> objectOptionalVal = new OptionalVal<>();
        objectOptionalVal.data = data;
        return objectOptionalVal;
    }


    /**
     * 替换其他对象
     *
     * @param data 数据实体
     * @return 示例
     */
    public OptionalVal<T> other(T data) {
        this.data = data;
        return this;
    }


    public boolean isPresent() {
        return this.data != null;
    }

    public T orElse(T data) {
        if (this.data == null) {
            return data;
        }
        return this.data;
    }


    public T get() {
        return this.data;
    }

    private T data;

}
