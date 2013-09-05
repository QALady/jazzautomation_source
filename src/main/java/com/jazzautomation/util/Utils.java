package com.jazzautomation.util;

public class Utils
{
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
