package com.jazzautomation.ui;

import static com.jazzautomation.WebUIManager.SYSTEM_BROWSERS_SETTING;
import static com.jazzautomation.WebUIManager.SYSTEM_CONFIGURATION_PATH;
import static com.jazzautomation.WebUIManager.SYSTEM_REPORTS_PATH;

import static com.jazzautomation.ui.Browsers.Firefox;

import static com.jazzautomation.util.Constants.FEATURE_NAMES_EXECUTION;
import static com.jazzautomation.util.Constants.SETTINGS_USE_XML;

import org.apache.commons.lang3.StringUtils;

import java.util.prefs.Preferences;

/** Repository for user UI settings. */
public class Settings
{
  public static final String  LAST_VISITED_DIR             = "LAST_VISITED_DIR";
  public static final String  SETTINGS                     = "settings";
  public static final String  CONFIGURATIONS_PATH          = "configurationsPath";
  public static final String  REPORTS_PATH                 = "reportsPath";
  public static final String  BROWSER                      = "browser";
  public static final String  FEATURES                     = "features";
  public static final String  LOGS_PATH                    = "logsPath";
  public static final int     DEFAULT_PROXY_PORT           = 8080;
  private static final String USE_HTTP_PROXY               = "useHttpProxy";
  private static final String PROXY_SERVER_NAME            = "proxyServerName";
  private static final String PROXY_SERVER_PORT            = "proxyServerPort";
  private static final String USE_PROXY_AUTHENTICATION     = "useProxyAuthentication";
  private static final String PROXY_USER_NAME              = "proxyUserName";
  private static final String PROXY_USER_PASSWORD          = "proxyPassword";
  private String              settings;
  private String              features;
  private String              configurationsPath;
  private String              logsPath;
  private String              reportsPath;
  private Browsers            browser;
  private final Preferences   preferences;
  private boolean             shouldUseProxy;
  private boolean             shouldUseProxyAuthentication;
  private String              proxyServerName;
  private int                 proxyServerPort;
  private String              proxyUserName;

  public Settings()
  {
    // set up the persistent settings with Java util Preferences.
    preferences = Preferences.userNodeForPackage(Settings.class);
    retrieve();
  }

  /** Retrieve from disk. Use the default given if there is no value */
  public void retrieve()
  {
    // logsPath           = preferences.get(LOGS_PATH, getNotNullSystemProperty(SYSTEM_LOGS_PATH));
    settings           = preferences.get(SETTINGS, getNotNullSystemProperty(SETTINGS_USE_XML));
    features           = preferences.get(FEATURES, getNotNullSystemProperty(FEATURE_NAMES_EXECUTION));
    configurationsPath = preferences.get(CONFIGURATIONS_PATH, getNotNullSystemProperty(SYSTEM_CONFIGURATION_PATH));

    String browserProperty = getNotNullSystemProperty(SYSTEM_BROWSERS_SETTING);

    if (StringUtils.isEmpty(browserProperty))
    {
      browserProperty = preferences.get(BROWSER, Firefox.name());
    }

    browser                      = Browsers.valueOf(browserProperty);
    reportsPath                  = preferences.get(REPORTS_PATH, getNotNullSystemProperty(SYSTEM_REPORTS_PATH));
    shouldUseProxy               = preferences.getBoolean(USE_HTTP_PROXY, false);
    proxyServerName              = preferences.get(PROXY_SERVER_NAME, "");
    proxyServerPort              = preferences.getInt(PROXY_SERVER_PORT, DEFAULT_PROXY_PORT);
    shouldUseProxyAuthentication = preferences.getBoolean(USE_PROXY_AUTHENTICATION, false);
    proxyUserName                = preferences.get(PROXY_USER_NAME, "");
    logsPath                     = System.getProperty("user.home") + "/jazzautomation.log";  // the assumption here is that this is immutable

    // proxyPassword          = preferences.get(PROXY_USER_PASSWORD, "");
  }

  /** get the property from System if it exists, else return empty string. */
  public static String getNotNullSystemProperty(String propertyName)
  {
    String property = System.getProperty(propertyName);

    if (property == null)
    {
      property = "";
    }

    return property;
  }

  public void setBrowser(Browsers browser)
  {
    this.browser = browser;
    save();
  }

  /** save all settings to disk. */
  public void save()
  {
    setValueIfNotNull(SETTINGS, settings);
    setValueIfNotNull(FEATURES, features);
    setValueIfNotNull(CONFIGURATIONS_PATH, configurationsPath);
    setValueIfNotNull(LOGS_PATH, logsPath);
    setValueIfNotNull(REPORTS_PATH, reportsPath);
    setValueIfNotNull(BROWSER, browser.name());
    setValueIfNotNull(SETTINGS, settings);
    preferences.putBoolean(USE_HTTP_PROXY, shouldUseProxy);
    preferences.put(PROXY_SERVER_NAME, proxyServerName);
    preferences.putInt(PROXY_SERVER_PORT, proxyServerPort);
    preferences.putBoolean(USE_PROXY_AUTHENTICATION, shouldUseProxyAuthentication);
    preferences.put(PROXY_USER_NAME, proxyUserName);
    setSystemProperties();  // set the system settings from the stored/modified preferences

    // preferences.put(PROXY_USER_PASSWORD, proxyPassword);
  }

  /** Util method - if the value isn't null, set it into preferences, else ignore it. */
  private void setValueIfNotNull(String key, String value)
  {
    if (value != null)
    {
      preferences.put(key, value);
    }
  }

  public void setConfigurationsPath(String configurationsPath)
  {
    this.configurationsPath = configurationsPath;
    save();
  }

  public void setFeatures(String features)
  {
    this.features = features;
    save();
  }

  public void setReportsPath(String reportsPath)
  {
    this.reportsPath = reportsPath;
    save();
  }

  public void setSettings(String settings)
  {
    this.settings = settings;
    save();
  }

  /** Set the properties into System properties for use elsewhere in the UI. */
  public void setSystemProperties()
  {
    System.setProperty(SETTINGS_USE_XML, settings);
    System.setProperty(FEATURE_NAMES_EXECUTION, features);
    System.setProperty(SYSTEM_CONFIGURATION_PATH, configurationsPath);
    System.setProperty(SYSTEM_REPORTS_PATH, reportsPath);
    System.setProperty(SYSTEM_BROWSERS_SETTING, browser.name());

    String driverName = browser.getDriver().getDriverName();

    if (driverName != null)
    {
      System.setProperty(driverName, driverName);
    }
  }

  public void setUseHttpProxy(boolean useHttpProxy)
  {
    shouldUseProxy = useHttpProxy;
  }

  public void setUseProxyAuthentication(boolean useProxyAuthentication)
  {
    shouldUseProxyAuthentication = useProxyAuthentication;
  }

  public boolean shouldUseProxy()
  {
    return shouldUseProxy;
  }

  public boolean shouldUseProxyAuthentication()
  {
    return shouldUseProxyAuthentication;
  }

  // --------------------- GETTER / SETTER METHODS ---------------------
  public Browsers getBrowser()
  {
    return browser;
  }

  public String getConfigurationsPath()
  {
    return configurationsPath;
  }

  public String getFeatures()
  {
    return features;
  }

  public String getLogsPath()
  {
    return logsPath;
  }

  public void setLogsPath(String logsPath)
  {
    this.logsPath = logsPath;
  }

  public String getProxyServerName()
  {
    return proxyServerName;
  }

  public void setProxyServerName(String proxyServerName)
  {
    this.proxyServerName = proxyServerName;
  }

  public int getProxyServerPort()
  {
    return proxyServerPort;
  }

  public void setProxyServerPort(int proxyServerPort)
  {
    this.proxyServerPort = proxyServerPort;
  }

  public String getProxyUserName()
  {
    return proxyUserName;
  }

  public void setProxyUserName(String proxyUserName)
  {
    this.proxyUserName = proxyUserName;
  }

  public String getReportsPath()
  {
    return reportsPath;
  }

  public String getSettings()
  {
    return settings;
  }
}
