package com.jazzautomation;

import com.jazzautomation.cucumber.Feature;
import com.jazzautomation.cucumber.parser.FeatureParser;
import com.jazzautomation.cucumber.parser.IllegalCucumberFormatException;

import static com.jazzautomation.util.Constants.FEATURE_NAMES_EXECUTION;

import org.apache.commons.lang3.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** This is a driver class to start automation. */
public class AutomationDriver
{
  private static final Logger LOG               = LoggerFactory.getLogger(AutomationDriver.class);
  private static final String FEATURE_SEPERATOR = ",";
  public static final String  FEATURE           = ".feature";
  private static String       featureNames;

  private AutomationDriver() {}

  public static void main(String[] args)
  {
    boolean successful = true;

    LOG.info("Starting Jazz Automation");

    try
    {
      successful = beginTestSuite();
    }
    catch (Exception e)
    {
      LOG.error("Test failure", e);
      successful = false;
    }
    finally
    {
      int returnStatus = successful ? 0
                                    : -1;

      LOG.info("Jazz Automation Complete");
      System.exit(returnStatus);
    }
  }

  public static boolean beginTestSuite()
  {
    final WebUIManager webUIManager    = WebUIManager.getInstance();
    Set<String>        featureNameList = new LinkedHashSet<>();  // use set to prevent same feature multiple times - unless this is desired?

    // override features from jazz.properties
    if (System.getProperty(FEATURE_NAMES_EXECUTION) != null)
    {
      featureNames = System.getProperty(FEATURE_NAMES_EXECUTION);
    }
    else
    {
      featureNames = WebUIManager.getInstance().getFeatureNames();
    }

    if (StringUtils.isNotEmpty(featureNames))
    {
      String[] featureArray = featureNames.split(FEATURE_SEPERATOR);

      for (String aFeatureName : featureArray)
      {
        LOG.info("Preparing feature [" + aFeatureName + "]");
        featureNameList.add(aFeatureName.trim());
      }
    }
    else
    {  // error checking
      throw new IllegalArgumentException("No features have been specified, so exiting. Please update the jazz.properties file or system property.");
    }

    // create a new suite and get the features
    Suite suite = new Suite(loadFeatures(webUIManager, featureNameList));

    // run the test suite
    SuiteProcessor.process(suite, null);

    // generate the reports
    ReportGenerator.generateReport(suite.getResult());

    return true;
  }

  public static List<Feature> loadFeatures(WebUIManager webUIManager, Collection<String> featureNameList)
  {
    String        featurePath = webUIManager.getConfigurationsPath() + File.separator + "features" + File.separator;
    List<Feature> features    = new ArrayList<>(featureNameList.size());

    if (LOG.isDebugEnabled())
    {
      LOG.debug("Feature path = [" + featurePath + ']');
    }

    for (String featureName : featureNameList)
    {
      if (LOG.isDebugEnabled())
      {
        LOG.debug("Feature name = [" + featureName + ']');
      }

      try
      {
        FileInputStream in      = new FileInputStream(featurePath + featureName + FEATURE);
        FeatureParser   parser  = FeatureParser.getInstance();
        Feature         feature = parser.parse(in);

        feature.setName(featureName);
        features.add(feature);
      }
      catch (FileNotFoundException e)
      {
        LOG.warn("Could not locate file for Feature[" + featureName + "]. Feature has been excluded from the test run.", e);
      }
      catch (IllegalCucumberFormatException ice)
      {
        LOG.warn("Could not parse Feature[" + featureName + "]. Feature has been excluded from the test run.", ice);
      }
    }

    if (LOG.isDebugEnabled())
    {
      LOG.debug("Feature list size = [" + features.size() + ']');
    }

    return features;
  }
}
