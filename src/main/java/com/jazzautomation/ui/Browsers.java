package com.jazzautomation.ui;

/** representation of the browsers allowed. */
public enum Browsers
{
  Firefox,
  Chrome,
  Safari,
  IE;

  public String getLowercaseName()
  {
    return name().toLowerCase();
  }
}
