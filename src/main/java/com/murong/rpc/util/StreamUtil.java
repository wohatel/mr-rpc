package com.murong.rpc.util;

import com.murong.rpc.annotation.MrAutowired;
import com.murong.rpc.annotation.MrClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * description
 *
 * @author yaochuang 2024/04/16 16:44
 */
public class StreamUtil {

    /**
     * @param inputStream  输入流
     * @param outputStream 输出流
     * @return void
     * @author yaochuang 2024-04-16 16:45
     */
    public static long inputStreamToOutputStream(InputStream inputStream, OutputStream outputStream) {
        long length = 0;
        try (inputStream) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                length += bytesRead;
                outputStream.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return length;
    }


    /**
     * 是否是上传的方法
     *
     * @param method 方法定义
     */
    public static boolean isUpload(Method method) {
        Class<?>[] paramTypes = method.getParameterTypes();
        return Arrays.stream(paramTypes).anyMatch(t -> t == InputStream.class);
    }


    public static List<Method> getNoneStaticAndDefaultMethods(Class interfaceClass) {
        List<Method> result = new ArrayList<>();
        Method[] methods = interfaceClass.getMethods();
        for (Method method : methods) {
            if (!Modifier.isStatic(method.getModifiers()) && !method.isDefault()) {
                result.add(method);
            }
        }
        return result;
    }

    public static List<Field> getLeafClientField(Class<?> fieldClass) {
        return Arrays.stream(fieldClass.getDeclaredFields()).filter(f -> {
            boolean annotationPresent = f.isAnnotationPresent(MrAutowired.class);
            if (!annotationPresent) {
                return false;
            }
            Class<?> rpcClass = f.getType();
            return rpcClass.isAnnotationPresent(MrClient.class);
        }).toList();
    }

    public static boolean isDownload(Method method) {
        return method.getReturnType() == InputStream.class;
    }

    public static boolean isSimpleUpload(Class<?>[] parameterTypes) {
        List<Class<?>> list = Arrays.stream(parameterTypes).filter(t -> InputStream.class.isAssignableFrom(t)).toList();
        if (list.size() > 1) {
            return false;
        }
        long count = list.stream().filter(t -> t == InputStream.class).count();
        if (count == 1) {
            return true;
        }
        return false;
    }
}
