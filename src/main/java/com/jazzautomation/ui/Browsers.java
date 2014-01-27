package com.jazzautomation.ui;

import com.google.common.base.Optional;

import com.jazzautomation.Drivers;

import java.util.HashMap;
import java.util.Map;

/** representation of the browsers allowed. */
public enum Browsers
{
  Firefox      (Drivers.NONE),
  Chrome       (Drivers.CHROME),
  Safari       (Drivers.NONE),
  IE           (Drivers.IE),
  NOT_SPECIFIED(Drivers.NONE),;

  private static final Map<String, Browsers> LOOKUP_MAP = new HashMap<>();

  static
  {
    Browsers[] values = values();

    for (Browsers value : values)
    {
      LOOKUP_MAP.put(value.name(), value);
      LOOKUP_MAP.put(value.name().toLowerCase(), value);
    }
  }

  private Drivers driver;

  Browsers(Drivers driver)
  {
    this.driver = driver;
  }

  public Drivers getDriver()
  {
    return driver;
  }

  public String getLowercaseName()
  {
    return name().toLowerCase();
  }

  public static Optional<Browsers> findValueOf(String text)
  {
    if (text == null)
    {
      return Optional.of(NOT_SPECIFIED);
    }

    Browsers value = LOOKUP_MAP.get(text.trim());

    if (value != null)
    {
      return Optional.of(value);
    }

    return Optional.absent();
  }
}
