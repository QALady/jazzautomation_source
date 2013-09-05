package com.jazzautomation.cucumber.parser;

public class IllegalCucumberFormatException extends Exception
{
  private static final long serialVersionUID = -5783322387735167307L;
  private String            message;

  public IllegalCucumberFormatException()
  {
    message = super.getMessage();
  }

  public IllegalCucumberFormatException(String message)
  {
    setMessage(message);
  }

  public void setMessage(String message)
  {
    if (this.message == null)
    {
      this.message = message;
    }
    else
    {
      this.message += message;
    }
  }

  public String getMessage()
  {
    return message;
  }
}
