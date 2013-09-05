package com.jazzautomation.report;

public class ResultBase
{
  private double  duration;
  private boolean success = true;
  private String  message;

  public double getDuration()
  {
    return duration;
  }

  public void setDuration(double duration)
  {
    this.duration = duration;
  }

  public boolean isSuccess()
  {
    return success;
  }

  public void setSuccess(boolean success)
  {
    this.success = success;
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
