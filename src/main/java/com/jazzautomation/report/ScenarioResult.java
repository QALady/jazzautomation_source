package com.jazzautomation.report;

import com.jazzautomation.cucumber.Scenario;

import java.util.ArrayList;
import java.util.List;

public class ScenarioResult extends ResultBase
{
  private Scenario                scenario;
  private double                  successRate    = 1.0;
  private List<ActionResult>      actionResults  = new ArrayList<>();
  private List<ExpectationResult> expectResults  = new ArrayList<>();
  private String                  screenShotPath;

  public void calculateSuccessRate()
  {
    int numFailedResults = 0;

    if (actionResults.isEmpty() && expectResults.isEmpty())
    {
      successRate = 1.0;

      return;
    }

    for (ActionResult actionResult : actionResults)
    {
      if (!actionResult.isSuccess())
      {
        numFailedResults++;
      }
    }

    for (ExpectationResult expectResult : expectResults)
    {
      if (!expectResult.isSuccess())
      {
        numFailedResults++;
      }
    }

    if (numFailedResults > 0)
    {
      setSuccess(false);
    }

    successRate = Math.round((actionResults.size() + expectResults.size() - numFailedResults) / (actionResults.size() + expectResults.size())
                               * 10000.0) / 10000.0;
  }

  public Scenario getScenario()
  {
    return scenario;
  }

  public void setScenario(Scenario scenario)
  {
    this.scenario = scenario;
  }

  public double getSuccessRate()
  {
    return successRate;
  }

  public void setSuccessRate(double successRate)
  {
    this.successRate = successRate;
  }

  public List<ActionResult> getActionResults()
  {
    return actionResults;
  }

  public void addActionResult(ActionResult actionResult)
  {
    actionResults.add(actionResult);
  }

  public void setActionResults(List<ActionResult> actionResults)
  {
    this.actionResults = actionResults;
  }

  public List<ExpectationResult> getExpectResults()
  {
    return expectResults;
  }

  public void addExpectResults(ExpectationResult expectResult)
  {
    expectResults.add(expectResult);
  }

  public void setExpectResults(List<ExpectationResult> expectResults)
  {
    this.expectResults = expectResults;
  }

  public String getScreenShotPath()
  {
    return screenShotPath;
  }

  public void setScreenShotPath(String screenShotPath)
  {
    this.screenShotPath = screenShotPath;
  }
}
