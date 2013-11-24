package com.jazzautomation.cucumber;

import com.jazzautomation.page.Page;

import org.codehaus.jackson.annotate.JsonIgnore;

import java.util.HashMap;
import java.util.Map;

public class Given extends CucumberBase
{
  @JsonIgnore private Page    page          = null;
  private boolean             forBackground;
  private Map<String, String> settings      = new HashMap<>();

  public void addSettings(String key, String value)
  {
    settings.put(key, value);
  }

  public String getValue(String key)
  {
    return settings.get(key);
  }

  // --------------------- GETTER / SETTER METHODS ---------------------
  public Page getPage()
  {
    return page;
  }

  public void setPage(Page page)
  {
    this.page = page;
  }

  public Map<String, String> getSettings()
  {
    return settings;
  }

  public void setSettings(Map<String, String> settings)
  {
    this.settings = settings;
  }

  public boolean isForBackground()
  {
    return forBackground;
  }

  public void setForBackground(boolean forBackground)
  {
    this.forBackground = forBackground;
  }
}
