package com.jazzautomation.util;

public class Utils
{
  private Utils() {}

  // jsheridan CODEREVIEW - why not use number utils?  or else rename isADouble
  public static boolean isANumber(String aString)
  {
    try
    {
      Double.parseDouble(aString);

      return true;
    }
    catch (NumberFormatException ne)
    {
      return false;
    }
  }
}
