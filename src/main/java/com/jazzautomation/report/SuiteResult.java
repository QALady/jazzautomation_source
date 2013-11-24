package com.jazzautomation.report;

import org.threeten.bp.LocalDate;

import java.util.ArrayList;
import java.util.List;

public class SuiteResult extends ResultBase
{
  private List<FeatureResult> featureResults = new ArrayList<>();
  private double              successRate    = 1.0;
  private LocalDate           timePerformed  = LocalDate.now();

  public void calculateSuccessRate()
  {
    int numFailedResults = 0;

    for (FeatureResult featureResult : featureResults)
    {
      if (!featureResult.isSuccess())
      {
        numFailedResults++;
      }
    }

    if (numFailedResults > 0)
    {
      setSuccess(false);
    }

    if (!featureResults.isEmpty())
    {
      successRate = (featureResults.size() - numFailedResults) / featureResults.size();
    }
  }

  public List<FeatureResult> getFeatureResults()
  {
    return featureResults;
  }

  public void addFeatureResult(FeatureResult featureResult)
  {
    featureResults.add(featureResult);
  }

  public void setFeatureResults(List<FeatureResult> featureResults)
  {
    this.featureResults = featureResults;
  }

  public LocalDate getTimePerformed()
  {
    return timePerformed;
  }

  public void setTimePerformed(LocalDate timePerformed)
  {
    this.timePerformed = timePerformed;
  }

  public double getSuccessRate()
  {
    return successRate;
  }

  public void setSuccessRate(double successRate)
  {
    this.successRate = successRate;
  }

  public String toString()
  {
    StringBuilder results = new StringBuilder();

    results.append("Overall success: " + this.isSuccess() + "\n");
    results.append("Duration: " + this.getDuration() + " seconds\n");
    results.append("Success rate: " + this.successRate + "\n");
    results.append("Features:" + "\n");

    for (FeatureResult featureResult : featureResults)
    {
      results.append("\tFeature: " + featureResult.getFeature().getDescription().trim() + "\n");
      results.append("\t\tSuccess: " + featureResult.isSuccess() + "\n");
      results.append("\t\tDuration: " + featureResult.getDuration() + " seconds\n");
      results.append("\t\tSuccess rate: " + featureResult.getSuccessRate() + "\n");
    }

    return results.toString();
  }
}
