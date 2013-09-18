package com.jazzautomation;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.jazzautomation.action.ComponentAction;
import com.jazzautomation.action.HtmlAction;
import com.jazzautomation.cucumber.And;
import com.jazzautomation.cucumber.Background;
import com.jazzautomation.cucumber.Feature;
import com.jazzautomation.cucumber.Scenario;
import com.jazzautomation.cucumber.Then;
import com.jazzautomation.cucumber.parser.FeatureParser;
import com.jazzautomation.cucumber.parser.IllegalCucumberFormatException;
import com.jazzautomation.page.DomElementExpect;
import com.jazzautomation.page.Page;
import com.jazzautomation.report.ActionResult;
import com.jazzautomation.report.ExpectResult;
import com.jazzautomation.report.FeatureResult;
import com.jazzautomation.report.ScenarioResult;
import com.jazzautomation.report.SuiteResult;
import com.jazzautomation.report.SuiteResultLight;
import com.jazzautomation.util.WebActionException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.Augmenter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static com.jazzautomation.util.Constants.DATA_FOLDER_NAME;
import static com.jazzautomation.util.Constants.DATA_JS_PRE_JSON;
import static com.jazzautomation.util.Constants.FEATURE_NAMES_EXECUTION;
import static com.jazzautomation.util.Constants.IMG_FOLDER_NAME;
import static com.jazzautomation.util.Constants.INDEX_FILES;
import static com.jazzautomation.util.Constants.JS_LIB_FILES;
import static com.jazzautomation.util.Constants.JS_LIB_FOLDER;
import static com.jazzautomation.util.Constants.REPORT_JS_PRE_JSON;

/**
 * This is a driver class to start automation.
 */
public class AutomationDriver
{
  private static final String FEATURE_SEPERATOR = ",";
  private static final Logger LOG = LoggerFactory.getLogger(AutomationDriver.class);
  private static String featureNames;

  public static void main(String[] args)
  {
    boolean successful = true;

    try
    {
      successful = drive();
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

      System.exit(returnStatus);
    }
  }

  public static boolean drive() throws IOException, WebActionException
  {
    final WebUIManager webUIManager = WebUIManager.getInstance();
    List<String> featureNameList = new ArrayList<>();

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
        LOG.info("Preparing feature : " + aFeatureName);
        featureNameList.add(aFeatureName.trim());
      }
    }
    else
    {
      // error checking
      throw new IllegalArgumentException("No features have been specified, so exiting. Please update the jazz.properties file or system property.");
    }

    // create a new suite and get the features
    Suite suite = new Suite(loadFeatures(webUIManager, featureNameList));

    // run the test suite
    SuiteProcessor.process(suite, null);

    // generate the reports
    generateReports(suite.getResult());

    return true;
  }

  public static List<Feature> loadFeatures(WebUIManager WebUIManager, List<String> featureNameList)
  {
    String featurePath = WebUIManager.getConfigurationsPath() + File.separator + "features" + File.separator;
    List<Feature> features = new ArrayList<>(featureNameList.size());

    LOG.debug("Feature path = [" +featurePath + "]");
    for (String featureName : featureNameList)
    {
      LOG.debug("Feature name = [" +featureName + "]");
      try
      {
        FileInputStream in = new FileInputStream(featurePath + featureName + ".feature");
        FeatureParser parser = FeatureParser.getInstance();
        Feature feature = parser.parse(in);

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

    LOG.debug("Feature list size = [" + features.size() + "]");
    return features;
  }

  private static void generateReports(SuiteResult suiteResult)
  {
    String reportPath = WebUIManager.getInstance().getLogsPath();
    File logsPathFile = new File(reportPath);

    // load data.json if exist
    if (!logsPathFile.exists())
    {
      logsPathFile.mkdir();
    }

    // copy all index templates in place
    copyTempatesFiles(logsPathFile);

    // find data path
    String dataFolderPath = logsPathFile.getAbsolutePath() + File.separator + DATA_FOLDER_NAME;
    File dataFolder = new File(dataFolderPath);

    if (!dataFolder.exists())
    {
      dataFolder.mkdir();
    }

    Date now = new Date();
    SimpleDateFormat sdf = new SimpleDateFormat("MM_dd_yyyy_'at'_HH_mm");
    String reportName = WebUIManager.getInstance().getProjectName() + "_" + sdf.format(now);
    String reportFileName = dataFolder.getAbsolutePath() + File.separator + reportName + ".js";
    File reportJsonFile = new File(reportFileName);
    ObjectMapper mapper = new ObjectMapper();
    String jsonString = null;
    File dataJsonFile = new File(dataFolder.getAbsolutePath() + File.separator + DATA_FOLDER_NAME + ".js");
    List<SuiteResultLight> dataList = new ArrayList<>();
    String dataJsonString = "";
    SuiteResultLight suiteLight = new SuiteResultLight();

    suiteLight.setName(reportName);
    suiteLight.setProject(WebUIManager.getInstance().getProjectName());
    suiteLight.setTimestamp(now.toGMTString());
    suiteLight.setDuration(suiteResult.getDuration());
    suiteLight.setSuccessRate(suiteResult.getSuccessRate());

    if (dataJsonFile.exists())
    {
      try
      {
        FileInputStream fileIn = new FileInputStream(dataJsonFile);
        BufferedReader stdin = new BufferedReader(new InputStreamReader(fileIn));
        StringBuffer buffer = new StringBuffer();
        String line;

        while ((line = stdin.readLine()) != null)
        {
          buffer.append(line);
        }

        String stringData = buffer.toString();

        if (stringData.trim().startsWith(DATA_JS_PRE_JSON))
        {
          int index = stringData.trim().indexOf(DATA_JS_PRE_JSON);

          dataList = mapper.readValue(stringData.trim().substring(DATA_JS_PRE_JSON.length()), ArrayList.class);
        }
      }
      catch (Exception e)
      {
        LOG.error("Error generating report", e);
      }
    }

    dataList.add(suiteLight);

    try
    {
      jsonString = REPORT_JS_PRE_JSON + mapper.writeValueAsString(suiteResult);
      dataJsonString = DATA_JS_PRE_JSON + mapper.writeValueAsString(dataList);
    }
    catch (Exception e)
    {
      LOG.error("Error converting suite results or reading the report data.", e);
    }

    // serialize SuiteResult
    try
    {
      FileOutputStream outForReport = new FileOutputStream(reportJsonFile);
      FileOutputStream outForDataJsonFile = new FileOutputStream(dataJsonFile);

      if (jsonString != null)
      {
        outForReport.write(jsonString.getBytes());
        outForDataJsonFile.write(dataJsonString.getBytes());
      }

      outForReport.flush();
      outForDataJsonFile.flush();
      outForReport.close();
      outForDataJsonFile.close();
    }
    catch (Exception e)
    {
      LOG.error("Error serializing the report.", e);
    }
  }

  private static void copyTempatesFiles(File logsPathFile)
  {
    // copy all index files
    for (String aFileName : INDEX_FILES)
    {
      copyAReportFile(aFileName, logsPathFile.getAbsolutePath() + File.separator + aFileName);
    }

    // create jslib folder
    String jsLibPath = logsPathFile.getAbsolutePath() + File.separator + JS_LIB_FOLDER;
    File jsLibFolderFile = new File(jsLibPath);

    if (!jsLibFolderFile.exists())
    {
      jsLibFolderFile.mkdir();
    }

    // create all jsLibs files
    for (String aJsLibFileName : JS_LIB_FILES)
    {
      copyAReportFile(aJsLibFileName, jsLibFolderFile.getAbsolutePath() + File.separator + aJsLibFileName);
    }
  }

  private static void copyAReportFile(String resourceUrlPath, String reportFilePath)
  {
    File aFileInreport = new File(reportFilePath);
    try(FileOutputStream outForReport = new FileOutputStream(aFileInreport))
    {
      URL aFileUrl = Resources.getResource(resourceUrlPath);
      String aFileInString = Resources.toString(aFileUrl, Charsets.UTF_8);
      outForReport.write(aFileInString.getBytes());
      outForReport.flush();
    }
    catch (IOException ie)
    {
      LOG.error("Error copying report.", ie);
    }
  }
}
