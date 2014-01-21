package com.jazzautomation.cucumber;

import static com.jazzautomation.cucumber.CucumberConstants.BACKGROUND;
import static com.jazzautomation.cucumber.CucumberConstants.FEATURE;
import static com.jazzautomation.cucumber.CucumberConstants.SCENARIO;

import org.codehaus.jackson.annotate.JsonIgnore;

import java.util.ArrayList;
import java.util.List;

public class Feature extends CucumberBase
{
  @JsonIgnore
  private String             originalText;
  private String             name;
  private Background         background;
  private List<Scenario>     scenarios = new ArrayList<>();

  public Feature()
  {
    setLeadingWords(FEATURE);
    setEndWords(SCENARIO, BACKGROUND);
  }

  @Override
  public void process() throws IllegalCucumberFormatException {}

  public void addScenario(Scenario scenario)
  {
    scenarios.add(scenario);
  }

  // --------------------- GETTER / SETTER METHODS ---------------------
  public Background getBackground()
  {
    return background;
  }

  public void setBackground(Background background)
  {
    this.background = background;
  }

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getOriginalText()
  {
    return originalText;
  }

  public void setOriginalText(String originalText)
  {
    this.originalText = originalText;
  }

  public List<Scenario> getScenarios()
  {
    return scenarios;
  }
}
