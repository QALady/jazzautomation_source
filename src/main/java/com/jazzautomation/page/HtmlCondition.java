package com.jazzautomation.page;

public class HtmlCondition
{
  private String                  componentName;
  private HtmlActionConditionEnum condition;

  public String getComponentName()
  {
    return componentName;
  }

  public void setComponentName(String componentName)
  {
    this.componentName = componentName;
  }

  public HtmlActionConditionEnum getCondition()
  {
    return condition;
  }

  public void setCondition(HtmlActionConditionEnum condition)
  {
    this.condition = condition;
  }
}
