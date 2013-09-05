package com.jazzautomation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.Proxy.ProxyType;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariDriver;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import com.jazzautomation.action.*;
import com.jazzautomation.page.*;
import static com.jazzautomation.util.Constants.*;

/**
 * UIManager holds all pages and their web components, page actions. It loads all configurations from settings.properties then go through all
 * registeredPages to load page object classes as well as their xml configurations.
 */
public class WebUIManager
{
  private static final String           SYSTEM_BROWSERS_SETTINGG  = "browsers";
  private static final String           SYSTEM_REPORTS_PATH       = "jazz.reports";
  private static final String           SYSTEM_CONFIGURATION_PATH = "jazz.configs";
  private static WebUIManager           instance                  = null;
  private static Log                    LOG                       = LogFactory.getLog(WebUIManager.class);
  private Map<String, Page>             pages                     = new HashMap<String, Page>();
  private Map<String, DomElement>       domElementPool            = new HashMap<String, DomElement>();
  private Map<String, List<PageAction>> pageActions               = new HashMap<String, List<PageAction>>();

  // configurations and reportsPath
  private String configurationsPath;
  private String reportsPath;

  // from jazz.properties
  private String        projectName        = "";
  private boolean       useRemoteWebDriver = false;
  private static String remoteWebDriverUrl = null;
  private static int    pagePace           = 10;
  private static int    actionPace         = 1;
  private static String featureNames;

  // from system properties
  private String browser        = null;
  private String platform       = null;
  private String browserVersion = null;
  Properties     settings       = new Properties();

  /**
   * Singleton instance of UIManager to hold information of pages and page actions.
   *
   * @return  The singleton UIManager
   *
   * @throws  IOException
   */
  public static WebUIManager getInstance()
  {
    if (instance == null)
    {
      instance = new WebUIManager();
    }

    return instance;
  }

  /*
   * private constructor.
   */
  private WebUIManager()
  {
    FileInputStream jazzSettings;

    try
    {
      long startTime = System.currentTimeMillis();

      configurationsPath = System.getProperty(SYSTEM_CONFIGURATION_PATH);
      reportsPath        = System.getProperty(SYSTEM_REPORTS_PATH);
      browser            = System.getProperty(SYSTEM_BROWSERS_SETTINGG);

      String useRemote   = System.getProperty("remote");

      if ((useRemote != null) && useRemote.equalsIgnoreCase("true"))
      {
        useRemoteWebDriver = true;
      }

      File configurationsFile = null;

      if ((null != configurationsPath) && (configurationsPath.trim().length() > 0))
      {
        configurationsFile = new File(configurationsPath);
      }
      else
      {
        configurationsFile = new File("configurations");
      }

      configurationsPath = configurationsFile.getAbsolutePath();

      File logsFile = null;

      if ((null != reportsPath) && (reportsPath.trim().length() > 0))
      {
        logsFile = new File(reportsPath);

        // recursively create file path if needed
        recursiveLyCreatePath(logsFile);
      }
      else
      {
        logsFile = new File("reports");
      }

      reportsPath = logsFile.getAbsolutePath();
      LOG.info("Initializing System ... ");

      // read settings files:
      LOG.info("\treading jazz.properties from " + configurationsPath + File.separator + "jazz.properties");
      LOG.info("\taccessing reports directory at " + reportsPath);
      jazzSettings = new FileInputStream(configurationsPath + File.separator + JAZZ + ".properties");
      settings.load(jazzSettings);

      // loading jazz.properties and populate all WebPages from its configurations.
      loadConfigurations();

      long endTime = System.currentTimeMillis();

      LOG.info("\nJazz Automation started successfully in " + (endTime - startTime) + " milliseconds.");
    }
    catch (FileNotFoundException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    catch (IOException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    // rb       = ResourceBundle.getBundle("settings");
  }

  // getter and setters
  public Map<String, List<PageAction>> getPageActions()
  {
    return pageActions;
  }

  public List<PageAction> getPageActionListFromPage(String pageName)
  {
    return pageActions.get(pageName);
  }

  public DomElement getDomElementFromPool(String identifier)
  {
    return domElementPool.get(identifier);
  }

  public String getConfigurationsPath()
  {
    return configurationsPath;
  }

  public String getLogsPath()
  {
    return reportsPath;
  }

  public void setLogsPath(String logsPath)
  {
    this.reportsPath = logsPath;
  }

  public String getRemoteWebDriverUrl()
  {
    return remoteWebDriverUrl;
  }

  public boolean isUseRemoteWebDriver()
  {
    return useRemoteWebDriver;
  }

  public void setRemoteWebDriverUrl(String remoteWebDriverUrl)
  {
    this.remoteWebDriverUrl = remoteWebDriverUrl;
  }

  public String getProjectName()
  {
    return projectName;
  }

  public void setProjectName(String projectName)
  {
    this.projectName = projectName;
  }

  public static int getPagePace()
  {
    return pagePace;
  }

  public static void setPagePace(int pagePace)
  {
    WebUIManager.pagePace = pagePace;
  }

  public static int getActionPace()
  {
    return actionPace;
  }

  public static void setActionPace(int actionPace)
  {
    WebUIManager.actionPace = actionPace;
  }

  public static String getFeatureNames()
  {
    return featureNames;
  }

  public static void setFeatureNames(String featureNames)
  {
    WebUIManager.featureNames = featureNames;
  }

  public void setUseRemoteWebDriver(boolean useRemoteWebDriver)
  {
    this.useRemoteWebDriver = useRemoteWebDriver;
  }

  public String getBrowser()
  {
    return browser;
  }

  public void setBrowser(String browser)
  {
    this.browser = browser;
  }

  public String getBrowserVersion()
  {
    return browserVersion;
  }

  public void setBrowserVersion(String browserVersion)
  {
    this.browserVersion = browserVersion;
  }

  public String getPlatform()
  {
    return platform;
  }

  public void setPlatform(String platform)
  {
    this.platform = platform;
  }

  public Map<String, Page> getPages()
  {
    return pages;
  }

  /**
   * Given a page name, return WebPage object in the Map of pages.
   *
   * @param   pageName  of the WebPage.
   *
   * @return  WebPage.
   */
  public Page getPage(String pageName)
  {
    return pages.get(pageName);
  }

  /**
   * loading setting.properties and populate all pages from its configurations.
   *
   * @throws  FileNotFoundException
   */
  private void loadConfigurations() throws FileNotFoundException
  {
    boolean useXml = false;

    if (settings.getProperty(PROJECT_NAME) != null)
    {
      projectName = settings.getProperty(PROJECT_NAME);
    }

    if (settings.getProperty(USE_REMOTE) != null)
    {
      useRemoteWebDriver = new Boolean(settings.getProperty(USE_REMOTE));
    }

    if (useRemoteWebDriver)
    {
      if ((settings.getProperty(REMOTE_WEB_DRIVER_URL) != null) && (settings.getProperty(REMOTE_WEB_DRIVER_URL).trim().length() > 0))
      {
        remoteWebDriverUrl = settings.getProperty(REMOTE_WEB_DRIVER_URL);
      }
      else
      {
        System.err.println("When useRemote, you must specify property: remoteWebDriverUrl.");
        System.exit(0);
      }
    }

    if (settings.getProperty(SETTINGS_USE_XML) != null)
    {
      useXml = new Boolean(settings.getProperty(SETTINGS_USE_XML));
    }

    String pagesDirectoryName = settings.getProperty(PAGES_DIRECTORY_NAME);

    // by default, use pages as the folder name
    pagesDirectoryName = (pagesDirectoryName == null) ? "pages"
                                                      : pagesDirectoryName;

    String pagesFolderPath = configurationsPath + File.separator + pagesDirectoryName;

    if (settings.getProperty(PAGE_PACE) != null)
    {
      pagePace = Integer.parseInt(settings.getProperty(PAGE_PACE).trim());
    }

    if (settings.getProperty(ACTION_PACE) != null)
    {
      actionPace = Integer.parseInt(settings.getProperty(ACTION_PACE).trim());
    }

    if (settings.getProperty(FEATURE_NAMES_EXECUTION) != null)
    {
      featureNames = settings.getProperty(FEATURE_NAMES_EXECUTION);
    }

    if (settings.getProperty(BROWSER) != null)
    {
      browser = settings.getProperty(BROWSER).trim();
    }

    // loop through directory, if useXml, add all files with xml postfix to pages, otherwise add all json files
    File pageFolder = new File(pagesFolderPath);

    if (!pageFolder.isDirectory())
    {
      LOG.error("\t" + pageFolder.getAbsolutePath() + " must be a directory. ");
      throw new RuntimeException(pageFolder.getAbsolutePath() + " must be a directory. ");
    }

    List<String> fileNames = Arrays.asList(pageFolder.list());

    // sort into alphabetical order so that we can always have override features for web components
    Collections.sort(fileNames);
    LOG.info("\tprocessing files at " + configurationsPath + File.separator + "pages" + File.separator + ":");

    for (String fileName : fileNames)
    {
      Page            page   = null;
      FileInputStream fileIn = new FileInputStream(pagesFolderPath + File.separator + fileName);

      LOG.info("\tprocessing page configuration file: " + fileName + " with useXml = " + useXml);

      if (useXml)
      {
        if (!fileName.toLowerCase().endsWith(".xml"))
        {
          LOG.info("\tignore file: " + fileName);

          continue;
        }

        try
        {
          JAXBContext  jaxbContext      = JAXBContext.newInstance(Page.class);
          Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

          page = (Page) jaxbUnmarshaller.unmarshal(fileIn);
          LOG.info("\t\tloaded weboage " + page.getPageName() + " from  " + fileName);
        }
        catch (JAXBException e)
        {
          // TODO Auto-generated catch block
          e.printStackTrace();

          continue;
        }
      }
      else
      {
        if (!fileName.toLowerCase().endsWith(".json"))
        {
          continue;
        }

        try
        {
          ObjectMapper objectMapper = new ObjectMapper();

          page = objectMapper.readValue(fileIn, Page.class);
        }
        catch (JsonParseException e)
        {
          // TODO Auto-generated catch block
          e.printStackTrace();

          continue;
        }
        catch (JsonMappingException e)
        {
          // TODO Auto-generated catch block
          e.printStackTrace();

          continue;
        }
        catch (IOException e)
        {
          // TODO Auto-generated catch block
          e.printStackTrace();

          continue;
        }
      }

      // something worng with the page, let's ignore it.
      if (page == null)
      {
        LOG.warn("\t\tfailed to load web page from " + fileName + " continue to load next.");

        continue;
      }

      pages.put(page.getPageName(), page);

      // add to domElementPool
      Map<String, DomElement> domElementsInPage = page.getDomElements();

      for (String componentIdentifier : domElementsInPage.keySet())
      {
        DomElement domElement = domElementsInPage.get(componentIdentifier);

        domElement.setIdentifier(componentIdentifier);

        // only add the first one in so that other page can override it.
        if (domElementPool.get(componentIdentifier) == null)
        {
          domElement.setPageInfo(page.getPageName());
          domElementPool.put(componentIdentifier, domElement);
        }
      }

      LOG.info("\t\tsuccessfully adding page entry of " + page.getPageName() + " with domElements: " + page.getDomElements());
    }
  }

  // building navigation graphs - future development
  private void buildNavigationGraph()
  {
    // always starts from the first page object.
    String           startingPathName        = settings.getProperty("startingPageName").trim();
    List<PageAction> pageActionsForFirstPage = pageActions.get(startingPathName);

    for (PageAction pageAction : pageActionsForFirstPage)
    {
      List<String> path = new ArrayList<String>();

      // path.add(startingPathName);
      String pageName     = pageAction.getSourcePageName();
      String nextPageName = pageAction.getTargetPageName();
      String actionString = PageAction.serializeList(pageAction.getActionChains().get(0));
      String key          = pageName + ":" + actionString;

      path.add(key);
    }
  }

  private boolean shouldPathStop(List<String> aPath, String key)
  {
    for (String pageName : aPath)
    {
      if (pageName.equals(key))
      {
        return true;
      }
    }

    return false;
  }

  private ArrayList<String> clonePath(List<String> path)
  {
    ArrayList<String> newPath = new ArrayList<String>();

    for (String pageName : path)
    {
      newPath.add(pageName);
    }

    return newPath;
  }

  // chrome driver
  public WebDriver getChromeDriver() throws MalformedURLException
  {
    DesiredCapabilities capabilities = DesiredCapabilities.chrome();

    capabilities.setCapability("chrome.switches", Arrays.asList(settings.getProperty("chrome.chrome.switches")));
    capabilities.setCapability("platform", applyPlatform());

    WebDriver driver;

    if (useRemoteWebDriver)
    {
      driver = new RemoteWebDriver(new URL(remoteWebDriverUrl), capabilities);
    }
    else
    {
      if (System.getProperty("webdriver.chrome.driver") == null)
      {
        System.setProperty("webdriver.chrome.driver", settings.getProperty("chrome.webdriver.chrome.driver"));
      }

      driver = new ChromeDriver(capabilities);
    }

    return driver;
  }

  // firefox
  public WebDriver getFirefoxDriver() throws MalformedURLException
  {
    WebDriver driver;

    if (useRemoteWebDriver)
    {
      DesiredCapabilities capabilities = DesiredCapabilities.firefox();

      capabilities.setCapability("platform", applyPlatform());
      capabilities.setCapability("version", applyVerion());
      driver = new RemoteWebDriver(new URL(remoteWebDriverUrl), capabilities);
    }
    else
    {
      FirefoxProfile ff = new FirefoxProfile();

      if (settings.getProperty("firefox.network.proxy.type").equalsIgnoreCase("AUTODETECT"))
      {
        ff.setPreference("network.proxy.type", ProxyType.AUTODETECT.ordinal());
      }
      else if (settings.getProperty("firefox.network.proxy.type").equalsIgnoreCase("DIRECT"))
      {
        ff.setPreference("network.proxy.type", ProxyType.DIRECT.ordinal());
      }

      driver = new FirefoxDriver(ff);
      driver.manage().window().maximize();
    }

    return driver;
  }

  // ie driver
  public WebDriver getIEDriver() throws MalformedURLException
  {
    WebDriver driver;

    if (useRemoteWebDriver)
    {
      DesiredCapabilities capabilities = DesiredCapabilities.internetExplorer();

      capabilities.setCapability("platform", applyPlatform());
      capabilities.setCapability("version", applyVerion());
      driver = new RemoteWebDriver(new URL(remoteWebDriverUrl), capabilities);
    }
    else
    {
      DesiredCapabilities capabilitiesInternet = new DesiredCapabilities();

      capabilitiesInternet.setCapability("ignoreProtectedModeSettings", settings.getProperty("ie.ignoreProtectedModeSettings"));
      capabilitiesInternet.setCapability("platform", applyPlatform());
      capabilitiesInternet.setCapability(CapabilityType.VERSION, applyVerion());
      driver = new InternetExplorerDriver(capabilitiesInternet);
    }

    driver.manage().window().maximize();

    return driver;
  }

  // safari
  public WebDriver getSafariDriver() throws MalformedURLException
  {
    if (useRemoteWebDriver)
    {
      DesiredCapabilities capabilities = DesiredCapabilities.safari();

      capabilities.setCapability("platform", applyPlatform());
      capabilities.setCapability("version", applyVerion());

      return new RemoteWebDriver(new URL(remoteWebDriverUrl), capabilities);
    }
    else
    {
      return new SafariDriver();
    }
  }

  // safari
  public WebDriver getIPadDriver() throws MalformedURLException
  {
    if (useRemoteWebDriver)
    {
      DesiredCapabilities capabilities = DesiredCapabilities.ipad();

      capabilities.setCapability("platform", applyPlatform());
      capabilities.setCapability("version", applyVerion());

      return new RemoteWebDriver(new URL(remoteWebDriverUrl), capabilities);
    }
    else
    {
      return new SafariDriver();
    }
  }

  public JavascriptExecutor getJQueryDriver(WebDriver webDriver)
  {
    return (JavascriptExecutor) webDriver;
  }

  public void loadJQuery(JavascriptExecutor jsDriver)
  {
    Object jquery = jsDriver.executeScript(" if ( typeof jQuery != 'undefined') { return 1;} else { return null; }");

    if (jquery == null)
    {
      URL    jqueryUrl  = Resources.getResource("jquery-1.8.0.min.js");
      String jqueryText = "";

      try
      {
        jqueryText = Resources.toString(jqueryUrl, Charsets.UTF_8);
      }
      catch (IOException e)
      {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

      LOG.info("\tEnable Jquery");
      jsDriver.executeScript(jqueryText);
    }
  }

  public String getCustomJS(String jsName)
  {
    URL    jsUrl  = Resources.getResource(jsName);
    String jsText = "";

    try
    {
      jsText = Resources.toString(jsUrl, Charsets.UTF_8);
    }
    catch (IOException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return jsText;
           // System.out.println("jsText = " + jsText);
           // jsDriver.executeScript(jsText);
  }

  private void recursiveLyCreatePath(File aDir)
  {
    if (!aDir.exists())
    {
      File parentDir = aDir.getParentFile();

      if (!parentDir.exists())
      {
        recursiveLyCreatePath(parentDir);
      }

      aDir.mkdir();
    }
  }

  private String applyPlatform()
  {
    if ((platform == null) || (platform.trim().length() == 0))
    {
      return settings.getProperty(PLATFORM);
    }
    else
    {
      return platform;
    }
  }

  private String applyVerion()
  {
    if ((browserVersion == null) || (browserVersion.trim().length() == 0))
    {
      return settings.getProperty(BROWSER_VERSION);
    }
    else
    {
      return browserVersion;
    }
  }
}
