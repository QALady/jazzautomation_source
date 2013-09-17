package com.jazzautomation.cucumber;

import java.util.ArrayList;
import java.util.List;
import org.codehaus.jackson.annotate.JsonIgnore;

public class Feature extends CucumberBase
{
  @JsonIgnore
  private String             originalText;
  private String             name;
  private Background         background = null;
  private List<Scenario>     scenarios  = new ArrayList<>();

  public Background getBackground()
  {
    return background;
  }

  public void setBackground(Background background)
  {
    this.background = background;
  }

  public List<Scenario> getScenarios()
  {
    return scenarios;
  }

  public void addScenario(Scenario scenario)
  {
    this.scenarios.add(scenario);
  }

  public String getOriginalText()
  {
    return originalText;
  }

  public void setOriginalText(String originalText)
  {
    this.originalText = originalText;
  }

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }
}
