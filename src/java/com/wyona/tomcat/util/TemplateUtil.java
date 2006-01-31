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
    public static boolean hasTemplate(ServletContext ctx, int statusCode) throws IOException {
        File template = new File(ctx.getRealPath(TEMPLATE_DIR), Integer.toString(statusCode) + ".html");        
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
    public static File getTemplateFile(ServletContext ctx, int statusCode) throws IOException {
        if (hasTemplate(ctx, statusCode)) {
            return new File(ctx.getRealPath(TEMPLATE_DIR), Integer.toString(statusCode) + ".html");
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
    public static InputStream getTemplateInputStream(ServletContext ctx, int statusCode) throws IOException {
        File templateFile = getTemplateFile(ctx, statusCode);
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
    public static void writeTemplate(ServletContext ctx, int statusCode, OutputStream out) throws IOException {
        InputStream in = getTemplateInputStream(ctx, statusCode);        
        byte[] buf = new byte[4096];
        int rb = 0;
        while ((rb = in.read(buf)) > 0) {
            out.write(buf, 0, rb);
        }
        in.close();
    }    
}
