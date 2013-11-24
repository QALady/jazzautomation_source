package com.jazzautomation.report;

import com.jazzautomation.cucumber.Feature;

import java.util.ArrayList;
import java.util.List;

public class FeatureResult extends ResultBase
{
  private Feature              feature;
  private double               successRate     = 1.0;
  private List<ScenarioResult> scenarioResults = new ArrayList<>();

  public void calculateSuccessRate()
  {
    int numFailedScenario = 0;

    for (ScenarioResult scenarioResult : scenarioResults)
    {
      if (!scenarioResult.isSuccess())
      {
        numFailedScenario++;
      }
    }

    if (numFailedScenario > 0)
    {
      setSuccess(false);
    }

    successRate = Math.round((scenarioResults.size() - numFailedScenario) * 1.0 / (scenarioResults.size() * 1.0) * 10000.0) / 10000.0;
  }

  public Feature getFeature()
  {
    return feature;
  }

  public void setFeature(Feature feature)
  {
    this.feature = feature;
  }

  public List<ScenarioResult> getScenarioResults()
  {
    return scenarioResults;
  }

  public double getSuccessRate()
  {
    return successRate;
  }

  public void setSuccessRate(double successRate)
  {
    this.successRate = successRate;
  }

  public void setScenarioResults(List<ScenarioResult> scenarioResults)
  {
    this.scenarioResults = scenarioResults;
  }

  public void addScenarioResult(ScenarioResult scenarioResult)
  {
    scenarioResults.add(scenarioResult);
  }
}
