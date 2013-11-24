package com.jazzautomation.ui;

import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.io.IOException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple enum to represent the type of OS. Nice to have when you need varying behavior across OSs - how to start a batch script, open a file with the
 * default app, etc - that should all go here.
 *
 * <p>Created by douglas_bullard on 10/2/13.</p>
 */
public enum Os
{
  OS_X   ("Mac OS X", new String[] {}),
  WINDOWS("Windows", new String[] { "cmd.exe", "/C" }),
  UNIX   ("Unix", new String[] {});

  private final String   name;
  private final String[] commandLineArgs;

  public static Os getOs()
  {
    if (SystemUtils.IS_OS_MAC_OSX)
    {
      return OS_X;
    }

    return SystemUtils.IS_OS_WINDOWS ? WINDOWS
                                     : UNIX;
  }

  Os(String name, String[] commandLineArgs)
  {
    this.name            = name;
    this.commandLineArgs = commandLineArgs;
  }

  public void openFile(File filePath) throws IOException, InvocationTargetException, IllegalAccessException, NoSuchMethodException,
                                             ClassNotFoundException
  {
    openFile(filePath.getAbsolutePath());
  }

  /** Open the file in the OS's default application. */
  @SuppressWarnings("CallToRuntimeExec")
  public void openFile(String filePath) throws IOException, InvocationTargetException, IllegalAccessException, NoSuchMethodException,
                                               ClassNotFoundException
  {
    if (this == WINDOWS)
    {
      List<String> commandList = new ArrayList<>();

      commandList.add("cmd.exe");
      commandList.add("/c");
      commandList.add(filePath);

      String[] command = commandList.toArray(new String[commandList.size()]);

      // todo move to ProcessBuilder
      // logger.debug("Command to run: " + concatenate(command));
      Runtime runtime = Runtime.getRuntime();

      runtime.exec(command);
    }
    else
    {  // calling FileManager to open the URL works, if we replace spaces with %20

      String   outputFilePath = filePath.replace(" ", "%20");
      String   fileUrl        = "file://" + outputFilePath;
      Class<?> aClass         = Class.forName("com.apple.eio.FileManager");
      Method   method         = aClass.getMethod("openURL", String.class);

      method.invoke(null, fileUrl);
    }
  }

  // --------------------- GETTER / SETTER METHODS ---------------------
  public String[] getCommandLineArgs()
  {
    return commandLineArgs;
  }

  public String getName()
  {
    return name;
  }
}
