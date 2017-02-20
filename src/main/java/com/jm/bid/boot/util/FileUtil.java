package com.jm.bid.boot.util;



import com.jm.bid.boot.exception.BusinessException;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by xiangyang on 16/8/25.
 */
public class FileUtil {

    private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);

    public static File createZipFile(String zipFileName, List<File> files) {
        File zipFile = null;
        FileOutputStream fos = null;
        ZipOutputStream zos = null;
        BufferedInputStream bis = null;
        try {
            zipFile = File.createTempFile(zipFileName, ".zip");
            fos = new FileOutputStream(zipFile);
            zos = new ZipOutputStream(new BufferedOutputStream(fos));
            for (File f : files) {
                ZipEntry zipEntry = new ZipEntry(new Date().getTime() + f.getName());
                zos.putNextEntry(zipEntry);
                bis = new BufferedInputStream(FileUtils.openInputStream(f), 1024 * 10);
                int read = 0;
                byte[] bufs = new byte[1024 * 10];
                while ((read = bis.read(bufs, 0, 1024 * 10)) != -1) {
                    zos.write(bufs, 0, read);
                }
            }
        } catch (IOException e) {
           logger.error("下载文件出错",e);
           throw  new BusinessException("下载文件出错!");
        }finally {
            try {
                if (null != bis) bis.close();
                if (null != zos) zos.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return zipFile;
    }
}
