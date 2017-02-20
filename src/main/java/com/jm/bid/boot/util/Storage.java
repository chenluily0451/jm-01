package com.jm.bid.boot.util;


import com.jm.bid.boot.exception.StorageException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Created by hary on 16/3/30.
 */

public interface Storage {
    /**
     *
     * @param key
     * @param input
     * @param length
     * @param contentType
     * @return
     */
    void save(String bucket, String key, InputStream input, int length, String contentType) throws StorageException;

    /**
     *
     * @param key
     * @return
     */
    InputStream download(String bucket, String key) throws StorageException;


    /**
     *
     * @param key
     * @return
     */
    File loadFile(String bucket, String key) throws FileNotFoundException;
}

