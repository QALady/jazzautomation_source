package com.jazzautomation.action;

public enum HtmlActionStatus
{
  CONDITION_NOT_MET("condition not met"),
  SHOW_STOPPER     ("showstopper"),
  SKIP_THIS_PAGE   ("skip"),
  GOOD             ("good");

  private String value;

  HtmlActionStatus(String value)
  {
    this.value = value;
  }

  public String getValue()
  {
    return value;
  }
}
