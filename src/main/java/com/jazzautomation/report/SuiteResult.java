package com.jazzautomation.report;

import org.threeten.bp.LocalDate;

import java.util.ArrayList;
import java.util.List;

public class SuiteResult extends ResultBase
{
  private List<FeatureResult> featureResults = new ArrayList<>();
  private double              successRate    = 1.0;
  private LocalDate           timePerformed  = LocalDate.now();

  public void addFeatureResult(FeatureResult featureResult)
  {
    featureResults.add(featureResult);
  }

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

  // ------------------------ CANONICAL METHODS ------------------------
  public String toString()
  {
    StringBuilder results = new StringBuilder();

    results.append("Overall success: ").append(isSuccess()).append('\n');
    results.append("Duration: ").append(getDuration()).append(" seconds\n");
    results.append("Success rate: ").append(successRate).append('\n');
    results.append("Features:" + '\n');

    for (FeatureResult featureResult : featureResults)
    {
      results.append("\tFeature: ").append(featureResult.getFeature().getDescription().trim()).append('\n');
      results.append("\t\tSuccess: ").append(featureResult.isSuccess()).append('\n');
      results.append("\t\tDuration: ").append(featureResult.getDuration()).append(" seconds\n");
      results.append("\t\tSuccess rate: ").append(featureResult.getSuccessRate()).append('\n');
    }

    return results.toString();
  }

  // --------------------- GETTER / SETTER METHODS ---------------------
  public List<FeatureResult> getFeatureResults()
  {
    return featureResults;
  }

  public void setFeatureResults(List<FeatureResult> featureResults)
  {
    this.featureResults = featureResults;
  }

  public double getSuccessRate()
  {
    return successRate;
  }

  public void setSuccessRate(double successRate)
  {
    this.successRate = successRate;
  }

  public LocalDate getTimePerformed()
  {
    return timePerformed;
  }

  public void setTimePerformed(LocalDate timePerformed)
  {
    this.timePerformed = timePerformed;
  }
}
