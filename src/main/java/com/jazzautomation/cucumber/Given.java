package com.jazzautomation.cucumber;

import java.util.HashMap;
import java.util.Map;
import org.codehaus.jackson.annotate.JsonIgnore;

import com.jazzautomation.page.Page;

public class Given extends CucumberBase
{
  @JsonIgnore
  private Page             page          = null;
  private boolean             forBackground;
  private Map<String, String> settings      = new HashMap<String, String>();

  public Page getPage()
  {
    return page;
  }

  public void setPage(Page page)
  {
    this.page = page;
  }

  public boolean isForBackground()
  {
    return forBackground;
  }

  public void setForBackground(boolean forBackground)
  {
    this.forBackground = forBackground;
  }

  public Map<String, String> getSettings()
  {
    return settings;
  }

  public String getValue(String key)
  {
    return settings.get(key);
  }

  public void addSettings(String key, String value)
  {
    this.settings.put(key, value);
  }

  public void setSettings(Map<String, String> settings)
  {
    this.settings = settings;
  }
}
