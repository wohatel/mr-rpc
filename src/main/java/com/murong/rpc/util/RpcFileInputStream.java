package com.murong.rpc.util;

import lombok.SneakyThrows;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;

/**
 * rpc文件流
 *
 * @author yaochuang 2024/04/17 10:53
 */
public class RpcFileInputStream extends InputStream {

    private final File file;

    private final FileInputStream fileInputStream;

    /**
     * 准成文件流
     *
     * @param inputStream 流
     */
    @SneakyThrows
    public RpcFileInputStream(InputStream inputStream) {
        if (inputStream == null) {
            this.fileInputStream = null;
            this.file = null;
        } else {
            this.file = OsUtil.genTmpFile();
            try (FileOutputStream outputStream = new FileOutputStream(file);) {
                // 读到文件里面
                StreamUtil.inputStreamToOutputStream(inputStream, outputStream);
                this.fileInputStream = new FileInputStream(file);
            }
        }
    }

    /**
     * String inputStream转换为本地流
     *
     * @param inputStream 流
     */
    @SneakyThrows
    public RpcFileInputStream(InputStream inputStream, String parentDir) {
        if (inputStream == null) {
            this.fileInputStream = null;
            this.file = null;
        } else {
            this.file = OsUtil.genTmpFile(parentDir);
            try (FileOutputStream outputStream = new FileOutputStream(file);) {
                // 读到文件里面
                StreamUtil.inputStreamToOutputStream(inputStream, outputStream);
                this.fileInputStream = new FileInputStream(file);
            }
        }
    }

    @Override
    public int read() throws IOException {
        return fileInputStream.read();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return fileInputStream.read(b, off, len);
    }

    @Override
    public byte[] readAllBytes() throws IOException {
        return fileInputStream.readAllBytes();
    }

    @Override
    public int readNBytes(byte[] b, int off, int len) throws IOException {
        return fileInputStream.readNBytes(b, off, len);
    }

    @Override
    public byte[] readNBytes(int len) throws IOException {
        return fileInputStream.readNBytes(len);
    }

    @Override
    public long skip(long n) throws IOException {
        return fileInputStream.skip(n);
    }

    @Override
    public void skipNBytes(long n) throws IOException {
        fileInputStream.skipNBytes(n);
    }

    @Override
    public long transferTo(OutputStream out) throws IOException {
        return fileInputStream.transferTo(out);
    }

    @Override
    public void close() throws IOException {
        try {
            fileInputStream.close();
        } finally {
            if (this.file != null) {
                Files.deleteIfExists(file.toPath());
            }
        }
    }

}
