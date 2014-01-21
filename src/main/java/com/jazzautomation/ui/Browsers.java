package com.jazzautomation.ui;

import com.google.common.base.Optional;

import java.util.HashMap;
import java.util.Map;

/** representation of the browsers allowed. */
public enum Browsers
{
  Firefox,
  Chrome,
  Safari,
  IE,
  NOT_SPECIFIED;

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

  public String getLowercaseName()
  {
    return name().toLowerCase();
  }

  public static Optional<Browsers> findValueOf(String text)
  {
    Browsers value = LOOKUP_MAP.get(text.trim());

    if (value != null)
    {
      return Optional.of(value);
    }

    return Optional.absent();
  }
}
