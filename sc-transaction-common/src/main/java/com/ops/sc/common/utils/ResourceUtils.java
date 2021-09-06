package com.ops.sc.common.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;


public class ResourceUtils {

    private static final String CLASSPATH_PREFIX = "classpath:";

    /**
     * Returns the URL of the resource on the classpath
     *
     * @param resource
     *            The resource to find
     * @return The resource
     * @throws IOException
     *             If the resource cannot be found or read
     */
    public static URL getClassUrl(String resource) throws IOException {
        if (resource.startsWith(CLASSPATH_PREFIX)) {
            String path = resource.substring(CLASSPATH_PREFIX.length());
            ClassLoader classLoader = ResourceUtils.class.getClassLoader();
            URL url = (classLoader != null ? classLoader.getResource(path) : ClassLoader.getSystemResource(path));
            if (url == null) {
                throw new FileNotFoundException("Resource [" + resource + "] does not exist");
            }
            return url;
        }

        try {
            return new URL(resource);
        } catch (MalformedURLException ex) {
            return new File(resource).toURI().toURL();
        }
    }

    /**
     * Returns a resource on the classpath as a File object
     *
     * @param url
     *            The resource url to find
     * @return The resource
     */
    public static File getResourceAsFile(URL url) {
        return new File(url.getFile());
    }

}
