package com.jazzautomation.util;

public class Constants
{
  // jazz.properties
  public static final String JAZZ                    = "jazz";
  public static final String PROJECT_NAME            = "projectName";
  public static final String USE_REMOTE              = "useRemote";
  public static final String REMOTE_WEB_DRIVER_URL   = "remoteWebDriverUrl";
  public static final String SETTINGS_USE_XML        = "useXml";
  public static final String PAGES_DIRECTORY_NAME    = "pagesDirectoryName";
  public static final String PAGE_LOAD_TIMEOUT       = "pageLoadTimeout";
  public static final String ACTION_PACE             = "actionPace";
  public static final String FEATURE_NAMES_EXECUTION = "features";
  public static final String CUSTOM_CLASSPATH        = "customClasspath";

  // jazz.properties optional:
  public static final String BROWSER         = "browser";
  public static final String PLATFORM        = "platform";
  public static final String BROWSER_VERSION = "browserVersion";

  // defaults
  public static final String DEFAULT_BROWSER = "firefox";

  // others
  public static final String   DASH                    = "-";
  public static final String   COMMA                   = ",";
  public static final String   COLON                   = ":";
  public static final String   LESS_THAN               = "<";
  public static final String   GREATER_THAN            = ">";
  public static final String   DOT                     = "\\.";
  public static final String   SLASH                   = "/";
  public static final String   BACK_SLASH              = "\\";
  public static final String   DOUBLE_SLASH            = "//";
  public static final String   DOUBLE_PIPE             = "\\|\\|";
  public static final String   DOUBLE_AND              = "&&";
  public static final String   SEMICOLON               = ";";
  public static final String   LEFT_SQUARE_BRACKET     = "[";
  public static final String   RIGHT_SQUARE_BRACKET    = "]";
  public static final String   LEFT_PARENTHESIS        = "\\(";
  public static final String   RIGHT_PARENTHESIS       = "\\)";
  public static final String   EXCLAMATION_MARK        = "!";
  public static final String   DATA_FOLDER_NAME        = "data";
  public static final String   IMG_FOLDER_NAME         = "img";
  public static final String   REPORT_JS_PRE_JSON      = "result =";
  public static final String   DATA_JS_PRE_JSON        = "data =";
  public static final String   TEMPLATES_FOLDER_NAME   = "templates";
  public static final String[] INDEX_FILES             = { "index.html", "jazz.css", "jazz.js" };
  public static final String   JS_LIB_FOLDER           = "jslib";
  public static final String[] JS_LIB_FILES            = { "backbone.js", "backbone-relational.js", "jquery.js", "underscore.js" };
  public static final String[] SUPPORTED_BROWSERS      = { "ie", "firefox", "chrome", "safari" };
  public static final String   CUSTOM_ACTION_INDICATOR = "$action=";

  private Constants() {}
}
