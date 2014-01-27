package com.jazzautomation;

import com.jazzautomation.ui.Os;

/** Created by douglas_bullard on 1/20/14. */
public enum Drivers
{
  CHROME("drivers/chromedriver", "drivers/chromedriver.exe"),
  IE    (null, "drivers/IEDriverServer.exe"),
  NONE  (null, null);

  private static final String BASE_DIR          = ".";
  private final String        macDriverName;
  private final String        windowsDriverName;

  Drivers(String macDriverName, String windowsDriverName)
  {
    this.macDriverName     = macDriverName;
    this.windowsDriverName = windowsDriverName;
  }

  public String getDriverName()
  {
    if (Os.getOs() == Os.WINDOWS)
    {
      return windowsDriverName;
    }

    if (Os.getOs() == Os.OS_X)
    {
      return macDriverName;
    }
    else
    {
      return null;  // nothing else supported
    }
  }
}
