package com.jazzautomation.util;

public class WebActionException extends Exception
{
  private static final long serialVersionUID     = 1L;
  public static int         STATUS_FATAL         = -2;
  public static int         STATUS_MAY_RETRY     = -1;
  public static int         STATUS_EXPECT_FAILED = -3;
  private String            message;
  private int               status;

  public WebActionException(String message)
  {
    this.message = message;
  }

  public int getStatus()
  {
    return status;
  }

  public void setStatus(int status)
  {
    this.status = status;
  }

  public String getMessage()
  {
    return message;
  }

  public void setMessage(String message)
  {
    this.message = message;
  }
}
