package com.jazzautomation.util;

public class WebActionException extends Exception
{
  private static final long serialVersionUID     = 1L;
  public static int         STATUS_FATAL         = -2;
  public static int         STATUS_MAY_RETRY     = -1;
  public static int         STATUS_EXPECT_FAILED = -3;
  private int               status;


  public WebActionException(String message) {
    super(message);
  }


  public WebActionException(String message, Throwable cause) {
    super(message, cause);
  }


  public int getStatus()
  {
    return status;
  }

  public void setStatus(int status)
  {
    this.status = status;
  }
}
