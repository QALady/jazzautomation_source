package com.jazzautomation;

import com.jazzautomation.cucumber.Feature;
import com.jazzautomation.report.SuiteResult;
import java.util.List;

/**
 * Test suite containing a collections of features.
 */
public class Suite
{
  private SuiteResult result;
  private List<Feature> features;

  public Suite()
  {
  }

  public Suite(List<Feature> features)
  {
    this();
    if(null == features || features.isEmpty())
    {
      throw new IllegalArgumentException("The feature list cannot be null or empty");
    }

    this.features = features;
  }

  public SuiteResult getResult()
  {
    return result;
  }

  public void setResult(SuiteResult result)
  {
    this.result = result;
  }

  public List<Feature> getFeatures()
  {
    return features;
  }

  public void setFeatures(List<Feature> features)
  {
    if(null == features || features.isEmpty())
    {
      throw new IllegalArgumentException("The feature list cannot be null or empty");
    }

    this.features = features;
  }
}

