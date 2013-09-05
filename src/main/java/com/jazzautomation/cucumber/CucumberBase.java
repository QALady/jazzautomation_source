package com.jazzautomation.cucumber;

import org.codehaus.jackson.annotate.JsonIgnore;

public class CucumberBase
{
  private String             description = null;
  @JsonIgnore
  private String             text        = null;

  public String getDescription()
  {
    return description;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }

  public String getText()
  {
    return text;
  }

  public void setText(String text)
  {
    this.text = text;
  }

  public String toString()
  {
    return text;
  }
}
