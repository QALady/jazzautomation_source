package com.jazzautomation.cucumber.parser;

public class IllegalCucumberFormatException extends Exception
{
  private static final long serialVersionUID = -5783322387735167307L;

  public IllegalCucumberFormatException()
  {
  }

  public IllegalCucumberFormatException(String message)
  {
    super(message);
  }

  public IllegalCucumberFormatException(String message, Throwable t)
  {
    super(message, t);
  }

}
