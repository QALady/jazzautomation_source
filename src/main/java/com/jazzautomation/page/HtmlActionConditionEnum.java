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
  LESS_THAN         ("<"),
  ENUM_NOT_FOUND    ("");

  private String value;

  HtmlActionConditionEnum(String value)
  {
    this.value = value;
  }

  public String getValue()
  {
    return value;
  }

  /**
   * Find a value in the list of enums. If no match is found, return the ENUM_NOT_FOUND value. Note that is is a perfect example of where optional
   * objects would be a good fit.
   */
  public static HtmlActionConditionEnum findValue(String theValue)
  {
    HtmlActionConditionEnum[] values = values();

    for (HtmlActionConditionEnum htmlActionConditionEnum : values)
    {
      if (htmlActionConditionEnum.value.equals(theValue))
      {
        return htmlActionConditionEnum;
      }
    }

    return ENUM_NOT_FOUND;
  }
}
