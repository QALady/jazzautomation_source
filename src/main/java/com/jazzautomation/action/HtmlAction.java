package com.jazzautomation.action;

public enum HtmlAction
{
  CLICK  ("click"),
  HOVER  ("hover"),
  WAIT   ("wait"),
  REFRESH("refresh"),
  ENTER  ("enter"),
  SELECT ("select"),
  FORWARD("forward"),
  BACKWAR("backward");

  private String actionName;

  HtmlAction(String actionName)
  {
    this.actionName = actionName;
  }

  public String getActionName()
  {
    return actionName;
  }

  public String toString()
  {
    return this.getActionName().toUpperCase();
  }
}
