package com.jazzautomation.report;

import java.util.Date;

public class SuiteResultLight
{
  private String name;
  private String project;
  private double successRate;
  private String timestamp;
  private double duration;

  // --------------------- GETTER / SETTER METHODS ---------------------
  public double getDuration()
  {
    return duration;
  }

  public void setDuration(double duration)
  {
    this.duration = duration;
  }

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getProject()
  {
    return project;
  }

  public void setProject(String project)
  {
    this.project = project;
  }

  public double getSuccessRate()
  {
    return successRate;
  }

  public void setSuccessRate(double successRate)
  {
    this.successRate = successRate;
  }

  public String getTimestamp()
  {
    return timestamp;
  }

  public void setTimestamp(String timestamp)
  {
    this.timestamp = timestamp;
  }
}
