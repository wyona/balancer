/**
 * 
 */
package com.wyona.tomcat.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletContext;

import com.wyona.tomcat.cluster.RequestStatus;

/**
 * Error page tempalte utility class
 * 
 * @author greg
 *
 */
public class TemplateUtil {

    private static final String TEMPLATE_DIR = "WEB-INF/templates";    
    
    /**
     * Checks if a template for this statusCode exists
     * @param ctx
     * @param statusCode
     * @return
     * @throws IOException
     */
    public static boolean hasTemplate(ServletContext ctx, RequestStatus status) throws IOException {
        File template = new File(ctx.getRealPath(TEMPLATE_DIR), Integer.toString(status.getStatusCode()) + ".html");        
        return template.exists() && template.isFile();
    }
    
    /**
     * Checks if a template for the given name exists
     * @param ctx
     * @param name
     * @return
     * @throws IOException
     */
    public static boolean hasTemplate(ServletContext ctx, String name) throws IOException {
        File template = new File(ctx.getRealPath(TEMPLATE_DIR), name);  
        return template.exists() && template.isFile();
    }
    
    /**
     * Returns the File object for this statusCode
     * @param ctx
     * @param statusCode
     * @return
     * @throws IOException
     */
    public static File getTemplateFile(ServletContext ctx, RequestStatus status) throws IOException {
        if (hasTemplate(ctx, status)) {
            return new File(ctx.getRealPath(TEMPLATE_DIR), Integer.toString(status.getStatusCode()) + ".html");
        } else {
            return null;
        }
    }

    /**
     * Returns the File object for the named template
     * @param ctx
     * @param name
     * @return
     * @throws IOException
     */
    public static File getTemplateFile(ServletContext ctx, String name) throws IOException {
        if (hasTemplate(ctx, name)) {
            return new File(ctx.getRealPath(TEMPLATE_DIR), name);
        } else {
            return null;
        }
    }
    
    /**
     * Return the InputStream for this statusCode
     * @param ctx
     * @param statusCode
     * @return
     * @throws IOException
     */
    public static InputStream getTemplateInputStream(ServletContext ctx, RequestStatus status) throws IOException {
        File templateFile = getTemplateFile(ctx, status);
        if (templateFile != null) {
            return new FileInputStream(templateFile);
        } else {
            return null;
        }
    }

    /**
     * Return the InputStream for this named template
     * @param ctx
     * @param name
     * @return
     * @throws IOException
     */
    public static InputStream getTemplateInputStream(ServletContext ctx, String name) throws IOException {
        File templateFile = getTemplateFile(ctx, name);
        if (templateFile != null) {
            return new FileInputStream(templateFile);
        } else {
            return null;
        }
    }
    
    /**
     * Write the template page to the specified output stream 
     * @param ctx
     * @param statusCode
     * @param out
     * @throws IOException
     */
    public static void writeTemplate(ServletContext ctx, RequestStatus status, OutputStream out) throws IOException {
        InputStream in = getTemplateInputStream(ctx, status);        
        byte[] buf = new byte[4096];
        int rb = 0, tb = 0;
        while ((rb = in.read(buf)) > 0) {
            out.write(buf, 0, rb);
            tb += rb;
        }
        status.setContentLength(tb);
        in.close();
        out.close();
    }    
}
