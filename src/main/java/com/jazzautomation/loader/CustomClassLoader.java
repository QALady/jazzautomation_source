package com.jazzautomation.loader;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class used to load customizations to the Jazz Automation classpath.
 */
public class CustomClassLoader
{
  private static final Logger LOG = LoggerFactory.getLogger(CustomClassLoader.class);

  public static void addPath(String s) throws Exception
  {
    File f = new File(s);
    URL u = f.toURL();
    System.out.println(u.toString());
    System.out.println(u.toURI());
    URLClassLoader urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
    Class urlClass = URLClassLoader.class;
    Method method = urlClass.getDeclaredMethod("addURL", new Class[]{URL.class});
    method.setAccessible(true);

    for (File file1 : f.listFiles())
    {
      LOG.info("Adding [" + file1.toURL() + "] to classpath.");
      method.invoke(urlClassLoader, new Object[]{file1.toURL()});
    }
  }

}
