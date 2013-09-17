package com.jazzautomation;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.io.*;
import java.util.*;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.remote.Augmenter;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.codehaus.jackson.map.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.jazzautomation.action.*;
import com.jazzautomation.cucumber.*;
import com.jazzautomation.cucumber.parser.FeatureParser;
import com.jazzautomation.cucumber.parser.IllegalCucumberFormatException;
import com.jazzautomation.page.*;
import com.jazzautomation.report.*;
import com.jazzautomation.util.WebActionException;
import static com.jazzautomation.util.Constants.*;

/** This is a driver class to start automation. */
public class AutomationDriver
{
  private static final String FEATURE_SEPERATOR = ",";
  private static final Logger    LOG          = LoggerFactory.getLogger(AutomationDriver.class);
  private static String siteUrl      = null;
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

    new HashMap<String, String>();

    List<String> featureNameList = new ArrayList<>();
//    String       siteUrl         = System.getProperty("siteUrl");
//    String       browserName     = (System.getProperty(BROWSER) == null) ? WebUIManager.getInstance().getBrowser()
//                                                                         : System.getProperty(BROWSER).trim();

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

    loadFeatures(webUIManager, featureNameList);

    return true;
  }

  public static boolean loadFeatures(WebUIManager WebUIManager, List<String> featureNameList)
  {
    String        featurePath = WebUIManager.getConfigurationsPath() + File.separator + "features" + File.separator;
    List<Feature> features    = new ArrayList<>();

    for (String featureName : featureNameList)
    {
      try
      {
        FileInputStream in      = new FileInputStream(featurePath + featureName + ".feature");
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

    SuiteResult suiteResult = runSuite(features, null);

    generateReports(suiteResult);

    return true;
  }

  private static SuiteResult runSuite(List<Feature> features, WebDriver driver)
  {
    SuiteResult suiteResult      = new SuiteResult();
    long        suiteTimeStarted = System.currentTimeMillis();

    for (Feature feature : features)
    {
      runFeature(feature, suiteResult, driver);
    }

    long suiteTimeEnded = System.currentTimeMillis();

    suiteResult.calculateSuccessRate();
    suiteResult.setDuration((suiteTimeEnded - suiteTimeStarted) / 1000.0);
    LOG.info("\nSuiteResult = \n" + suiteResult);

    return suiteResult;
  }

  private static void runFeature(Feature feature, SuiteResult suiteResult, WebDriver driver)
  {
    long          featureTimeStarted = System.currentTimeMillis();
    FeatureResult featureResult      = new FeatureResult();

    featureResult.setFeature(feature);
    suiteResult.addFeatureResult(featureResult);

    Background          background         = feature.getBackground();
    Map<String, String> backgroundSettings = background.getGiven().getSettings();

    LOG.info("Background settings = " + backgroundSettings);

    if (backgroundSettings.size() > 0)
    {
      resetSettings(backgroundSettings, false);
    }

    // setting driver
    if (driver == null)
    {
      try
      {
        driver = setWebDriver(WebUIManager.getInstance().getBrowser());
      }
      catch (MalformedURLException e)
      {
        featureResult.setMessage("Error: remoteWebDriver Url is incorrect (STOPPED): " + WebUIManager.getInstance().getRemoteWebDriverUrl()
                                   + " Please check your settings.properties ");
        featureResult.setSuccess(false);
        resetSettings(null, true);
      }
    }

    // go to the set
    JavascriptExecutor jsDriver        = null;
    String             startingSiteUrl = (backgroundSettings.get("url") != null) ? backgroundSettings.get("url").trim()
                                                                                 : siteUrl;

    LOG.info("\nGo to site: " + startingSiteUrl);
    driver.get(startingSiteUrl);

    boolean isFirstPage = true;

    LOG.info("go to feature " + feature.getDescription() + " with total " + feature.getScenarios().size() + " scenarios");

    for (Scenario scenario : feature.getScenarios())
    {
      if (isFirstPage)
      {
        jsDriver    = WebUIManager.getInstance().getJQueryDriver(driver);
        isFirstPage = false;
      }

      runScenario(scenario, featureResult, driver, jsDriver);
    }

    long featureTimeEnded = System.currentTimeMillis();

    featureResult.setDuration((featureTimeEnded - featureTimeStarted) / 1000.0);
    featureResult.calculateSuccessRate();
    driver.quit();
    driver = null;
  }

  private static void runScenario(Scenario scenario, FeatureResult featureResult, WebDriver driver, JavascriptExecutor jsDriver)
  {
    long           scenarioTimeSatrted = System.currentTimeMillis();
    long           scenarioTimeEnded   = System.currentTimeMillis();
    ScenarioResult scenarioResult      = new ScenarioResult();

    featureResult.addScenarioResult(scenarioResult);
    scenarioResult.setScenario(scenario);
    LOG.info("start working on scenario " + scenario.getText());

    Page page = scenario.getGiven().getPage();

    pageSetup(driver, jsDriver, page);

    // loading page
    if (!loadingPage(scenario, scenarioResult, page))
    {
      scenarioResult.calculateSuccessRate();
      scenarioTimeEnded = System.currentTimeMillis();
      scenarioResult.setDuration((scenarioTimeEnded - scenarioTimeSatrted) / 1000);

      if (!scenario.isOptional())
      {
        // scenarioResult.setMessage("Failed to load page: " + page.getPageName());
        LOG.info("Not able to load page " + page.getPageName() + " for scenario " + scenario.getText());
        scenarioResult.setScreenShotPath(captureScreen(driver));
      }
      else
      {
        scenarioResult.setSuccess(true);

        // scenarioResult.setMessage("Skipped - Not able to load page: " + page.getPageName() + " (optional)");
        LOG.info("Optional: Not able to load page " + page.getPageName() + " for scenario " + scenario.getText());
        scenarioResult.setScreenShotPath(captureScreen(driver));
      }

      return;
    }

    // taking actions
    for (And and : scenario.getAnds())
    {
      LOG.info("working on and " + and.getText());

      for (ComponentAction componentAction : and.getActions())
      {
        takingAction(scenarioResult, page, and, componentAction);
      }
    }

    // checking expects
    Then then = scenario.getThen();

    if (then != null)
    {
      if (then.getPageExpected() != null)
      {
        pageSetup(driver, jsDriver, then.getPageExpected());
        checkingPageExpect(scenarioResult, then);
      }
      else
      {
        for (DomElementExpect expect : then.getExpects())
        {
          checkingComponentExpect(scenarioResult, page, expect);
        }
      }
    }

    scenarioResult.calculateSuccessRate();
    scenarioTimeEnded = System.currentTimeMillis();
    scenarioResult.setDuration((scenarioTimeEnded - scenarioTimeSatrted) / 1000.0);
  }

  private static boolean loadingPage(Scenario scenario, ScenarioResult scenarioResult, Page page)
  {
    try
    {
      page.setup();

      return true;
    }
    catch (Exception wae)
    {
      if (!scenario.isOptional())
      {
        scenarioResult.setMessage("Failed to load page: " + page.getPageName() + " [" + wae.getMessage() + "].");
        scenarioResult.setSuccess(false);
        scenarioResult.setSuccessRate(0.0);
        LOG.info("Failed to load page for :" + page.getPageName() + " for " + scenario.getText() + " Please check your feature setting ");
      }
      else
      {
        scenarioResult.setMessage("Skipped page for :" + page.getPageName() + " [" + wae.getMessage() + "].");
        LOG.info("Skipped page for :" + page.getPageName() + " - optional.");
      }

      return false;
    }
  }

  private static void pageSetup(WebDriver driver, JavascriptExecutor jsDriver, Page page)
  {
    page.setWebDriver(driver);
    page.setJsDriver(jsDriver);
    WebUIManager.getInstance().loadJQuery(jsDriver);
    LOG.info("go to page " + page.getPageName());
    page.setPagePace(WebUIManager.getInstance().getPagePace());
    page.setActionPace(WebUIManager.getInstance().getActionPace());
    page.setBrowser(WebUIManager.getInstance().getBrowser());
  }

  private static void checkingPageExpect(ScenarioResult scenarioResult, Then then)
  {
    ExpectResult expectResult = new ExpectResult();

    scenarioResult.addExpectResults(expectResult);

    try
    {
      then.getPageExpected().setup();
      expectResult.setSuccess(true);
    }
    catch (WebActionException e)
    {
      expectResult.setSuccess(false);
      expectResult.setMessage("Expect to see page:" + then.getPageExpected().getPageName() + " Failed - " + e.getMessage());
      scenarioResult.setScreenShotPath(captureScreen(then.getPageExpected().getWebDriver()));
    }
  }

  private static void checkingComponentExpect(ScenarioResult scenarioResult, Page page, DomElementExpect expect)
  {
    ExpectResult expectResult = new ExpectResult();

    scenarioResult.addExpectResults(expectResult);
    expectResult.setComponentExpect(expect);

    if (page.checkExpect(expect))
    {
      expectResult.setSuccess(true);
    }
    else
    {
      expectResult.setSuccess(false);
      expectResult.setMessage(expect.getMessage());

      // if (scenarioResult.getScreenShotPath() != null)
      // {
      LOG.info("Failed to meet expect - capture screen ");
      scenarioResult.setScreenShotPath(captureScreen(page.getWebDriver()));

      // }
    }
  }

  private static void takingAction(ScenarioResult scenarioResult, Page page, And and, ComponentAction componentAction)
  {
    ActionResult actionResult = new ActionResult();

    actionResult.setAnd(and);
    actionResult.setAction(componentAction);
    scenarioResult.addActionResult(actionResult);

    try
    {
      if (componentAction.getAction().equals(HtmlAction.WAIT))
      {
        page.takeWebAction(null, componentAction.getAction(), componentAction.getActionValue());
      }
      else
      {
        page.takeWebAction(page.getDomElement(componentAction.getComponentName()), componentAction.getAction(), componentAction.getActionValue());
      }

      actionResult.setSuccess(true);
    }
    catch (WebActionException wae)
    {
      if (componentAction.isOptional())
      {
        actionResult.setSuccess(true);
        actionResult.setMessage("Skipped action :" + componentAction.getAction() + " " + componentAction.getComponentName() + " - optional");
        LOG.info("Skipped action :" + componentAction.getAction() + " " + componentAction.getComponentName() + " - optional");
      }
      else
      {
        actionResult.setMessage("Failed to take action :" + componentAction.getAction() + " " + componentAction.getComponentName() + " - "
            + wae.getMessage());
        actionResult.setSuccess(false);
        scenarioResult.setScreenShotPath(captureScreen(page.getWebDriver()));
        LOG.info("Failed to take action :" + componentAction.getAction() + " " + componentAction.getComponentName());
      }
    }
  }

  private static void resetSettings(Map<String, String> backgroundSettings, boolean resetToNull)
  {
    if (resetToNull)
    {
      WebUIManager.getInstance().setPlatform(null);
      WebUIManager.getInstance().setBrowser(null);
      WebUIManager.getInstance().setBrowserVersion(null);
    }
    else
    {
      Set<String> keys = backgroundSettings.keySet();

      for (String key : keys)
      {
        if (key.toLowerCase().equals("platform"))
        {
          WebUIManager.getInstance().setPlatform(backgroundSettings.get(key).trim());
        }
        else if (key.toLowerCase().equals("browser"))
        {
          LOG.info("browser is " + backgroundSettings.get(key));
          WebUIManager.getInstance().setBrowser(backgroundSettings.get(key).trim());
        }
        else if (key.toLowerCase().equals("browserVersion"))
        {
          WebUIManager.getInstance().setBrowserVersion(backgroundSettings.get(key).trim());
        }
      }
    }
  }

  private static WebDriver setWebDriver(String browserName) throws MalformedURLException
  {
    WebUIManager webUIManager = WebUIManager.getInstance();
    WebDriver    driver;

    LOG.debug("browser name =" + browserName + "'");

    if (browserName.trim().equalsIgnoreCase("chrome"))
    {
      driver = webUIManager.getChromeDriver();
    }
    else if (browserName.trim().equalsIgnoreCase("ie"))
    {
      driver = webUIManager.getIEDriver();
    }
    else if (browserName.trim().equalsIgnoreCase("safari"))
    {
      driver = webUIManager.getSafariDriver();
    }
    else
    {
      driver = webUIManager.getFirefoxDriver();
    }

    return driver;
  }

  private static String captureScreen(WebDriver driver)
  {
    String fileUrl;

    try
    {
      Thread.sleep(3000);

      WebDriver augmentedDriver = driver;

      if (WebUIManager.getInstance().isUseRemoteWebDriver())
      {
        augmentedDriver = new Augmenter().augment(driver);
      }

      //
      File             source   = ((TakesScreenshot) augmentedDriver).getScreenshotAs(OutputType.FILE);
      String           fileName = source.getName();
      Date             now      = new Date();
      SimpleDateFormat sdf      = new SimpleDateFormat("MM_dd_yyyy");
      String           dirName  = IMG_FOLDER_NAME + "_" + sdf.format(now);
      File             dirFile  = new File(WebUIManager.getInstance().getLogsPath() + File.separator + dirName);

      if (!dirFile.exists())
      {
        dirFile.mkdir();
      }

      fileUrl = dirName + "/" + source.getName();

      String path = WebUIManager.getInstance().getLogsPath() + File.separator + dirName + File.separator + fileName;

      FileUtils.copyFile(source, new File(path));
      Thread.sleep(3000);
    }
    catch (Exception e)
    {
      fileUrl = "Failed to capture screenshot: " + e.getMessage();
    }

    return fileUrl;
  }

  private static void generateReports(SuiteResult suiteResult)
  {
    String reportPath   = WebUIManager.getInstance().getLogsPath();
    File   logsPathFile = new File(reportPath);

    // load data.json if exist
    if (!logsPathFile.exists())
    {
      logsPathFile.mkdir();
    }

    // copy all index templates in place
    copyTempatesFiles(logsPathFile);

    // find data path
    String dataFolderPath = logsPathFile.getAbsolutePath() + File.separator + DATA_FOLDER_NAME;
    File   dataFolder     = new File(dataFolderPath);

    if (!dataFolder.exists())
    {
      dataFolder.mkdir();
    }

    Date                   now            = new Date();
    SimpleDateFormat       sdf            = new SimpleDateFormat("MM_dd_yyyy_'at'_HH_mm");
    String                 reportName     = WebUIManager.getInstance().getProjectName() + "_" + sdf.format(now);
    String                 reportFileName = dataFolder.getAbsolutePath() + File.separator + reportName + ".js";
    File                   reportJsonFile = new File(reportFileName);
    ObjectMapper           mapper         = new ObjectMapper();
    String                 jsonString     = null;
    File                   dataJsonFile   = new File(dataFolder.getAbsolutePath() + File.separator + DATA_FOLDER_NAME + ".js");
    List<SuiteResultLight> dataList       = new ArrayList<>();
    String                 dataJsonString = "";
    SuiteResultLight       suiteLight     = new SuiteResultLight();

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
        BufferedReader  stdin  = new BufferedReader(new InputStreamReader(fileIn));
        StringBuffer    buffer = new StringBuffer();
        String          line   = "";

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
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

    dataList.add(suiteLight);

    try
    {
      jsonString     = REPORT_JS_PRE_JSON + mapper.writeValueAsString(suiteResult);
      dataJsonString = DATA_JS_PRE_JSON + mapper.writeValueAsString(dataList);
    }
    catch (Exception e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    // serialize SuiteResult
    try
    {
      FileOutputStream outForReport       = new FileOutputStream(reportJsonFile);
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
      // TODO Auto-generated catch block
      e.printStackTrace();
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
    String jsLibPath       = logsPathFile.getAbsolutePath() + File.separator + JS_LIB_FOLDER;
    File   jsLibFolderFile = new File(jsLibPath);

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
    FileOutputStream outForReport = null;

    try
    {
      URL    aFileUrl      = Resources.getResource(resourceUrlPath);
      String aFileInString = Resources.toString(aFileUrl, Charsets.UTF_8);
      File   aFileInreport = new File(reportFilePath);

      outForReport = new FileOutputStream(aFileInreport);
      outForReport.write(aFileInString.getBytes());
      outForReport.flush();
    }
    catch (IOException ie)
    {
      ie.printStackTrace();
    }
    finally
    {
      if (outForReport != null)
      {
        try
        {
          outForReport.close();
        }
        catch (IOException e)
        {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }
  }
}
