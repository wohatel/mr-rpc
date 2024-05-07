package com.murong.rpc.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * <p>
 * 执行工具类
 * </p>
 *
 * @author yaochuang 2024/04/01 15:55
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RunnerUtil {

    /**
     * 静默执行
     */
    public static <T> T execSilent(Supplier<T> supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
        }
        return null;
    }

    /**
     * 静默执行
     */
    public static <T> T execSilentExceptionTo(Supplier<T> supplier, T result) {
        try {
            return supplier.get();
        } catch (Exception e) {
        }
        return result;
    }

    /**
     * 静默执行
     *
     * @param supplier 空实现
     */
    public static void execSilentVoid(VoidSupplier supplier) {
        try {
            supplier.get();
        } catch (Exception e) {
        }
    }

    /**
     * 静默执行,捕获异常处理
     *
     * @param supplier 空实现
     * @param e        异常
     */
    public static void execSilentException(VoidSupplier supplier, Consumer<Exception> e) {
        try {
            supplier.get();
        } catch (Exception ex) {
            execSilentVoid(() -> e.accept(ex));
        }
    }

    /**
     * 执行多次
     *
     * @param supplier 空结果实现
     * @param times    执行次数
     */
    public static void execSilentVoid(VoidSupplier supplier, int times) {
        for (int i = 0; i < times; i++) {
            execSilentVoid(supplier);
        }
    }

    /**
     * 空执行
     *
     * @author yaochuang 2024/04/01 15:57
     */
    public interface VoidSupplier {
        /**
         * 空执行
         *
         * @author yaochuang 2024-04-01 15:57
         */
        void get();
    }
}
