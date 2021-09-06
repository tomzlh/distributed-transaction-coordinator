package com.ops.sc.common.utils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.ops.sc.common.enums.ClientErrorCode;
import com.ops.sc.common.exception.ScMessageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.Resource;
import sun.misc.URLClassPath;


public class ClassLoaderHelper extends URLClassLoader {
    private static Logger logger = LoggerFactory.getLogger(ClassLoaderHelper.class);

    private String pluginName;
    private ClassLoader parent;
    private URLClassPath ucppath;

    public ClassLoaderHelper(String dirPath, ClassLoader parent) {
        super(new URL[] {}, parent);
        Path dirPathObject = Paths.get(dirPath);
        Path path = dirPathObject != null ? dirPathObject.getFileName() : null;
        if (path != null) {
            this.pluginName = path.toString();
        }
        this.parent = parent;
        try {
            this.ucppath = new URLClassPath(new URL[] { Paths.get(dirPath).toUri().toURL() });
        } catch (MalformedURLException e) {
            throw new ScMessageException(ClientErrorCode.INTERNAL_ERROR, e);
        }
        loadJarInDir(dirPath);
    }

    @Override
    public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        logger.debug("Load class:{}", name);
        Class c = findLoadedClass(name);
        if (c == null) {
            // load plugin class
            c = findPluginClass(name);
            // load other class
            if (null == c) {
                c = parent.loadClass(name);
            }
        }
        if (resolve) {
            resolveClass(c);
        }
        return c;
    }

    @Override
    protected void addURL(URL url) {
        ucppath.addURL(url);
        super.addURL(url);
    }

    private void loadJarInDir(String dirPath) {
        File dir = new File(dirPath);
        // 自动加载目录下的jar包
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files == null) {
                return;
            }
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".jar")) {
                    try {
                        this.addURL(file.toURI().toURL());
                    } catch (IOException e) {
                        throw new ScMessageException(ClientErrorCode.INTERNAL_ERROR,
                                "Load " + pluginName + " Failed," + e.getMessage());
                    }
                }
            }
        }
    }

    private Class<?> findPluginClass(final String name) throws ClassNotFoundException {
        String path = name.replace('.', '/').concat(".class");
        Resource res = ucppath.getResource(path, false);
        if (res != null) {
            // System.out.println("res:" + res.getURL().toString());
            return super.findClass(name);
        } else {
            return null;
        }
    }

}
