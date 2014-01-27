package com.jazzautomation;

import com.google.common.base.Optional;

import static com.jazzautomation.WebUIManager.SYSTEM_BROWSERS_SETTING;

import com.jazzautomation.action.ComponentAction;
import com.jazzautomation.action.HtmlAction;

import com.jazzautomation.cucumber.And;
import com.jazzautomation.cucumber.Background;
import com.jazzautomation.cucumber.Feature;
import com.jazzautomation.cucumber.Scenario;
import com.jazzautomation.cucumber.Then;

import com.jazzautomation.page.DomElementExpectation;
import com.jazzautomation.page.Page;

import com.jazzautomation.report.ActionResult;
import com.jazzautomation.report.ExpectationResult;
import com.jazzautomation.report.FeatureResult;
import com.jazzautomation.report.ScenarioResult;
import com.jazzautomation.report.SuiteResult;

import com.jazzautomation.ui.Browsers;
import static com.jazzautomation.ui.Browsers.Chrome;
import static com.jazzautomation.ui.Browsers.IE;
import static com.jazzautomation.ui.Browsers.NOT_SPECIFIED;
import static com.jazzautomation.ui.Browsers.Safari;
import com.jazzautomation.ui.Settings;

import static com.jazzautomation.util.Constants.IMG_FOLDER_NAME;
import com.jazzautomation.util.WebActionException;

import org.apache.commons.io.FileUtils;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.Augmenter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;

import java.io.File;

import java.net.MalformedURLException;

import java.util.List;
import java.util.Map;
import java.util.Set;

/** Suite processor class that will process test suites and run all features. */
public class SuiteProcessor
{
  private static final Logger            LOG                 = LoggerFactory.getLogger(SuiteProcessor.class);
  private static final int               SCREEN_CAPTURE_WAIT = 3000;
  private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("MM_dd_yyyy");

  private SuiteProcessor() {}

  /**
   * Processes the provided suite.
   *
   * @param  suite   the suite to be executed
   * @param  driver  the web driver; optional
   */
  public static void process(Suite suite, WebDriver driver)
  {
    SuiteResult result = runSuite(suite.getFeatures(), driver);

    suite.setResult(result);
  }

  private static SuiteResult runSuite(List<Feature> features, WebDriver driver)
  {
    LOG.info("** Begin Suite **");

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
    LOG.info("** End Suite **");

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

    LOG.info("Background settings = [" + backgroundSettings + ']');

    if (!backgroundSettings.isEmpty())
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
        featureResult.setMessage("Error: remoteWebDriver Url is incorrect (STOPPED): " + WebUIManager.getRemoteWebDriverUrl()
                                   + " Please check your settings.properties ");
        featureResult.setSuccess(false);
        resetSettings(null, true);
      }
    }

    // go to the set
    String startingSiteUrl = backgroundSettings.get("url").trim();

    LOG.info("Navigating to site: " + startingSiteUrl);
    assert driver != null;
    driver.get(startingSiteUrl);
    LOG.info("Go to feature [" + feature.getDescription() + "] with total [" + feature.getScenarios().size() + "] scenarios");

    boolean            isFirstPage = true;
    JavascriptExecutor jsDriver    = null;

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

    featureResult.setDuration((featureTimeEnded - featureTimeStarted) / 1000);
    featureResult.calculateSuccessRate();
    driver.quit();
  }

  private static void runScenario(Scenario scenario, FeatureResult featureResult, WebDriver driver, JavascriptExecutor jsDriver)
  {
    long           scenarioTimeStarted = System.currentTimeMillis();
    ScenarioResult scenarioResult      = new ScenarioResult();

    featureResult.addScenarioResult(scenarioResult);
    scenarioResult.setScenario(scenario);
    LOG.info("start working on scenario [" + scenario.getText() + ']');

    Page page = scenario.getGiven().getPage();

    pageSetup(driver, jsDriver, page);

    // loading page
    if (loadPage(scenario, driver, scenarioTimeStarted, scenarioResult, page))
    {
      return;
    }

    // take actions
    takeActions(scenario, scenarioResult, page);

    // verify expectations
    verifyExpectations(scenario, driver, jsDriver, scenarioResult, page);
    scenarioResult.calculateSuccessRate();

    long scenarioTimeEnded = System.currentTimeMillis();

    scenarioResult.setDuration((scenarioTimeEnded - scenarioTimeStarted) / 1000.0);
  }

  private static boolean loadPage(Scenario scenario, WebDriver driver, long scenarioTimeStarted, ScenarioResult scenarioResult, Page page)
  {
    if (!loadPage(scenario, scenarioResult, page))
    {
      scenarioResult.calculateSuccessRate();

      long scenarioTimeEnded = System.currentTimeMillis();

      scenarioResult.setDuration((scenarioTimeEnded - scenarioTimeStarted) / 1000);

      if (scenario.isOptional())
      {
        LOG.info("Not able to load page [" + page.getPageName() + "] for scenario [" + scenario.getText() + ']');
        scenarioResult.setScreenShotPath(captureScreen(driver));
      }
      else
      {
        scenarioResult.setSuccess(false);
        LOG.info("Optional: Not able to load page [" + page.getPageName() + "] for scenario [" + scenario.getText() + ']');
        scenarioResult.setScreenShotPath(captureScreen(driver));
      }

      return true;
    }

    return false;
  }

  private static void verifyExpectations(Scenario scenario, WebDriver driver, JavascriptExecutor jsDriver, ScenarioResult scenarioResult, Page page)
  {
    Then then = scenario.getThen();

    if (then != null)
    {
      if (then.getPageExpected() != null)
      {
        pageSetup(driver, jsDriver, then.getPageExpected());
        verifyPageExpectation(scenarioResult, then);
      }
      else
      {
        for (DomElementExpectation expect : then.getExpects())
        {
          verifyComponentExpectation(scenarioResult, page, expect);
        }
      }
    }
  }

  private static void takeActions(Scenario scenario, ScenarioResult scenarioResult, Page page)
  {
    for (And and : scenario.getAnds())
    {
      LOG.info("Working on 'and' [" + and.getText() + ']');

      for (ComponentAction componentAction : and.getActions())
      {
        executeAction(scenarioResult, page, and, componentAction);
      }
    }
  }

  /**
   * What does return true mean? False? //CODEREVIEW - need docs
   *
   * @param   scenario
   * @param   scenarioResult
   * @param   page
   *
   * @return
   */
  private static boolean loadPage(Scenario scenario, ScenarioResult scenarioResult, Page page)
  {
    try
    {
      page.setup();

      return true;
    }
    catch (Exception exception)
    {
      if (scenario.isOptional())
      {
        scenarioResult.setMessage("Skipped page for :" + page.getPageName() + " [" + exception.getMessage() + "].");
        LOG.info("Skipped page for :" + page.getPageName() + " - optional.");
      }
      else
      {
        scenarioResult.setSuccess(false);
        scenarioResult.setSuccessRate(0.0);
        scenarioResult.setMessage("Failed to load page: " + page.getPageName() + " [" + exception.getMessage() + "].");
        LOG.info("Failed to load page for :" + page.getPageName() + " for " + scenario.getText() + " Please check your feature settings");
      }

      return false;
    }
  }

  private static void pageSetup(WebDriver driver, JavascriptExecutor jsDriver, Page page)
  {
    page.setWebDriver(driver);
    page.setJsDriver(jsDriver);

    WebUIManager webUiManager = WebUIManager.getInstance();

    WebUIManager.loadJQuery(jsDriver);
    LOG.info("Navigating to page " + page.getPageName());
    page.setPageLoadTimeout(WebUIManager.getPageLoadTimeout());
    page.setActionPace(WebUIManager.getActionPace());
    page.setBrowser(webUiManager.getBrowser());
  }

  private static void verifyPageExpectation(ScenarioResult scenarioResult, Then then)
  {
    ExpectationResult expectResult = new ExpectationResult();

    scenarioResult.addExpectResults(expectResult);

    try
    {
      then.getPageExpected().setup();
      expectResult.setSuccess(true);
    }
    catch (WebActionException e)
    {
      expectResult.setSuccess(false);
      expectResult.setMessage("Expected to see page:" + then.getPageExpected().getPageName() + " Failed - " + e.getMessage());
      scenarioResult.setScreenShotPath(captureScreen(then.getPageExpected().getWebDriver()));
    }
  }

  private static void verifyComponentExpectation(ScenarioResult scenarioResult, Page page, DomElementExpectation expect)
  {
    ExpectationResult expectResult = new ExpectationResult();

    scenarioResult.addExpectResults(expectResult);
    expectResult.setComponentExpect(expect);

    if (page.checkExpectation(expect))
    {
      expectResult.setSuccess(true);
    }
    else
    {
      expectResult.setSuccess(false);
      expectResult.setMessage(expect.getMessage());
      LOG.info("Failed to meet expect - capture screen ");
      scenarioResult.setScreenShotPath(captureScreen(page.getWebDriver()));
    }
  }

  private static void executeAction(ScenarioResult scenarioResult, Page page, And and, ComponentAction componentAction)
  {
    ActionResult actionResult = new ActionResult();

    actionResult.setAnd(and);
    actionResult.setAction(componentAction);
    scenarioResult.addActionResult(actionResult);

    try
    {
      if (componentAction.getAction() == HtmlAction.WAIT)
      {
        page.executeWebAction(null, componentAction.getAction(), componentAction.getActionValue());
      }
      else
      {
        page.executeWebAction(page.getDomElement(componentAction.getComponentName()), componentAction.getAction(), componentAction.getActionValue());
      }

      actionResult.setSuccess(true);
    }
    catch (WebActionException wae)
    {
      if (componentAction.isOptional())
      {
        actionResult.setSuccess(true);
        actionResult.setMessage("Skipped action :" + componentAction.getAction() + ' ' + componentAction.getComponentName() + " - optional");
        LOG.info("Skipped action :" + componentAction.getAction() + ' ' + componentAction.getComponentName() + " - optional");
      }
      else
      {
        actionResult.setMessage("Failed to take action :" + componentAction.getAction() + ' ' + componentAction.getComponentName() + " - "
                                  + wae.getMessage());
        actionResult.setSuccess(false);
        scenarioResult.setScreenShotPath(captureScreen(page.getWebDriver()));
        LOG.info("Failed to take action :" + componentAction.getAction() + ' ' + componentAction.getComponentName());
      }
    }
  }

  private static void resetSettings(Map<String, String> backgroundSettings, boolean resetToNull)
  {
    WebUIManager uiManager = WebUIManager.getInstance();

    if (resetToNull)
    {
      uiManager.setPlatform(null);

      String browserProperty = Settings.getNotNullSystemProperty(SYSTEM_BROWSERS_SETTING);

      // noinspection UnnecessarilyQualifiedStaticallyImportedElement
      Optional<Browsers> possible = Browsers.findValueOf(browserProperty);
      Browsers           browser  = possible.isPresent() ? possible.get()
                                                         : NOT_SPECIFIED;

      uiManager.setBrowser(browser, true);
      uiManager.setBrowserVersion(null);
    }
    else
    {
      Set<String> keys = backgroundSettings.keySet();

      for (String key : keys)
      {
        if (key.toLowerCase().equals("platform"))
        {
          uiManager.setPlatform(backgroundSettings.get(key));
        }
        else if (key.toLowerCase().equals("browser"))
        {
          LOG.info("browser is " + backgroundSettings.get(key));

          Optional<Browsers> possible = Browsers.findValueOf(backgroundSettings.get(key));

          uiManager.setBrowser(possible.get(), false);
        }
        else if (key.toLowerCase().equals("browserVersion"))
        {
          uiManager.setBrowserVersion(backgroundSettings.get(key));
        }
      }
    }
  }

  private static WebDriver setWebDriver(Browsers browser) throws MalformedURLException
  {
    WebUIManager webUIManager = WebUIManager.getInstance();

    if (LOG.isDebugEnabled())
    {
      LOG.debug("browser name = '" + browser.getLowercaseName() + '\'');
    }

    WebDriver driver;

    if (browser == Chrome)
    {
      driver = webUIManager.getChromeDriver();  // todo why isn't this a property of the browser enum?
    }
    else if (browser == IE)
    {
      driver = webUIManager.getIEDriver();
    }
    else if (browser == Safari)
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
      Thread.sleep(3500);

      WebDriver augmentedDriver = driver;

      if (WebUIManager.getInstance().isUseRemoteWebDriver())
      {
        augmentedDriver = new Augmenter().augment(driver);
      }

      //
      File   source   = ((TakesScreenshot) augmentedDriver).getScreenshotAs(OutputType.FILE);
      String fileName = source.getName();
      String dirName  = IMG_FOLDER_NAME + '_' + DATE_TIME_FORMATTER.format(LocalDate.now());
      File   dirFile  = new File(WebUIManager.getInstance().getLogsPath() + File.separator + dirName);

      if (!dirFile.exists())
      {
        dirFile.mkdir();
      }

      fileUrl = dirName + '/' + source.getName();

      String path = WebUIManager.getInstance().getLogsPath() + File.separator + dirName + File.separator + fileName;

      FileUtils.copyFile(source, new File(path));
      Thread.sleep(SCREEN_CAPTURE_WAIT);
    }
    catch (Exception e)
    {
      fileUrl = "Failed to capture screen shot: " + e.getMessage();
    }

    return fileUrl;
  }
}
