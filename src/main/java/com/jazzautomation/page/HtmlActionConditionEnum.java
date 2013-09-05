package com.jazzautomation.page;

public enum HtmlActionConditionEnum
{
  EXISTS            ("exists"),
  VISIBLE           ("visible"),
  INVISIBLE         ("invisible"),
  GREATER_THAN_ZERO ("greater_than_0"),
  EQUALS            ("=="),
  NOT_EQUALS        ("!="),
  GREATER_THAN      (">"),
  GREATER_EQUAL_THAN(">="),
  LESS_EQUAL_THAN   ("<="),
  LESS_THAN         ("<");

  private String value;

  HtmlActionConditionEnum(String value)
  {
    this.value = value;
  }

  public String getValue()
  {
    return value;
  }
}
