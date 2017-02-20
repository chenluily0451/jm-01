package com.jm.bid.boot.util;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.BaseFont;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by joe on 1/11/15.
 */
public class PDF {


    /**
     *
     * @param html  html字符串
     * @return
     * @throws IOException
     * @throws DocumentException
     */
    public static File createByHtml(String html) throws IOException, DocumentException, com.lowagie.text.DocumentException {
        ITextRenderer renderer = new ITextRenderer();
        renderer.getFontResolver().addFont("simsun.ttc", BaseFont.IDENTITY_H, BaseFont.NOT_EMBEDDED);
        renderer.setDocumentFromString(html);
        renderer.layout();
        File file = File.createTempFile("pdf", "pdf");
        renderer.createPDF(new FileOutputStream(file));
        return file;
    }

}
