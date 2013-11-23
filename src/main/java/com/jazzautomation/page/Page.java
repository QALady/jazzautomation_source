package com.jazzautomation.page;

import com.jazzautomation.WebUIManager;
import com.jazzautomation.action.ComponentAction;
import com.jazzautomation.action.HtmlAction;
import com.jazzautomation.action.HtmlActionStatus;
import com.jazzautomation.action.PageAction;
import com.jazzautomation.customaction.Action;
import com.jazzautomation.util.Constants;
import com.jazzautomation.util.Utils;
import com.jazzautomation.util.WebActionException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotVisibleException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the Java Page Object representing a html page object. After setWebDriver and setJsDriver, one must performing setup() to load page. To do
 * navigations from one this page to a new page, gotoPage needs to be called. Typical sequences are:
 */
@XmlRootElement
public class Page
{
  private static final int NUMBER_OF_RETRY = 3;
  private static Logger LOG = LoggerFactory.getLogger(Page.class);
  private String pageName;
  private String title;
  private String urlExtension;
  private Map<String, DomElement> domElements = new HashMap<>();

  // if the page need to submit a form at the end of action
  boolean doSubmit = false;

  // if the page need some pre-condition values such as username/password
  boolean doSetup = false;

  // if the page needs to switch windows from a parent page
  boolean switchWindows = false;
  boolean optional = false;

  // a key component must exist before a page can be operational.
  private String keyDomElementName = null;
  @XmlTransient
  private WebDriver webDriver;
  @XmlTransient
  private JavascriptExecutor jsDriver;
  @XmlTransient
  private float pagePace = 10.0f;  // default value 10 seconds
  @XmlTransient
  private float actionPace = 1.0f;   // default value 1 second
  @XmlTransient
  private String browser;

  /**
   * setup is needed if you need to have some value for your web components. For exmaple, a login page will need username and password before
   * performing web actions.
   *
   * @throws WebActionException
   */
  public void setup() throws WebActionException
  {
    LOG.info("Setting up page [" + pageName + "]");

    int timeout_seconds = (int) (NUMBER_OF_RETRY * pagePace);
    int sleepMilliSeconds = (int) (pagePace * 1000);
    final StringBuffer failedMessage = new StringBuffer();

    try
    {
      (new WebDriverWait(webDriver, timeout_seconds, sleepMilliSeconds)).until(new ExpectedCondition<Boolean>()
      {
        public Boolean apply(WebDriver d)
        {
          if (pagePace > 20)
          {
            if (!((JavascriptExecutor) d).executeScript("return document.readyState").equals("complete"))
            {
              return false;
            }
          }

          boolean doTitle = (title != null) && (title.trim().length() > 0);

          changeWindowsIfNecessary(d, doTitle);

          boolean urlAndTitleCorrect;

          if (doTitle)
          {
            urlAndTitleCorrect = (d.getCurrentUrl().indexOf(urlExtension) > 0) && d.getTitle().equals(title);
          }
          else
          {
            urlAndTitleCorrect = d.getCurrentUrl().indexOf(urlExtension) > 0;
          }

          boolean keyComponentExisted = true;

          if (keyDomElementName != null)
          {
            DomElement keyDomComponent = getDomElement(keyDomElementName);

            if (keyDomComponent == null)
            {
              LOG.info("There is no such domElement, [" + keyDomElementName + "] in your configuration.");
              failedMessage.append("no such dom element: " + keyDomElementName);

              return false;
            }

            try
            {
              populateDomElement(keyDomComponent);
            }
            catch (WebActionException e)
            {
              LOG.debug("Web exception", e);
              return false;
            }

            if (!keyDomComponent.isExisted() || !keyDomComponent.isVisible())
            {
              keyComponentExisted = false;
            }
          }

          return urlAndTitleCorrect && keyComponentExisted;
        }
      });
    }
    catch (Exception te)
    {
      te.printStackTrace();

      // adding why failed
      String errorMessage = "";

      if ((title != null) && (title.trim().length() > 0))
      {
        errorMessage += "Expected title is '" + title + "'; actual title: '" + webDriver.getTitle() + "'";
      }

      if ((urlExtension != null) && (urlExtension.trim().length() > 0))
      {
        if (errorMessage.length() > 0)
        {
          errorMessage += " -- ";
        }

        errorMessage += "Expect url is: '" + urlExtension + "'; actual urlExtension: '" + webDriver.getCurrentUrl() + "'";
      }

      if (keyDomElementName != null)
      {
        if (errorMessage.length() > 0)
        {
          errorMessage += " -- ";
        }

        DomElement keyDomComponent = getDomElement(keyDomElementName);

        errorMessage += "Expect " + keyDomElementName + " visible; actual: visible = " + keyDomComponent.isVisible() + " with (dom exists = ? "
            + keyDomComponent.isExisted() + ")";
      }

      LOG.info("timeout to load page: " + pageName + " with error: " + errorMessage);
      throw new WebActionException(errorMessage);
    }
  }

  private void changeWindowsIfNecessary(WebDriver webDriver1, boolean doTitle)
  {
    if (switchWindows && (!webDriver1.getCurrentUrl().contains(urlExtension)))
    {
      Set<String> windows = webDriver1.getWindowHandles();

      for (String window : windows)
      {
        webDriver1.switchTo().window(window);
        LOG.info("Changing windows: current window url " + webDriver1.getCurrentUrl());

        if (webDriver1.getCurrentUrl().indexOf(urlExtension) > 0)
        {
          if (doTitle)
          {
            if (webDriver1.getTitle().equals(title))
            {
              break;
            }
          }
          else
          {
            break;
          }
        }
      }
    }
  }


  /**
   * Taking webaction for the webcomponent, such as keyenter, click, etc.
   *
   * @param domElement
   * @param webAction
   * @return boolean
   */
  public HtmlActionStatus executeWebAction(final DomElement domElement, HtmlAction webAction, String actionValue) throws WebActionException
  {
    if (webAction.equals(HtmlAction.WAIT) || webAction.equals(HtmlAction.REFRESH))
    {
      LOG.info("Wait or refresh");
      performDomAction(domElement, webAction, actionValue);

      return HtmlActionStatus.GOOD;
    }

    LOG.info("Preparing " + webAction.getActionName() + " " + domElement.getIdentifier() + " (webAction)");
    paceLoadComponent(domElement);
    LOG.info("Loaded [" + domElement.getIdentifier() + "]");

    if ((null == domElement.getDomElement()) || !domElement.isExisted())
    {
      throw new WebActionException("No such web component exist: " + domElement.getIdentifier() + " with jquery = " + domElement.getJquery());
    }

    if (domElement.isExisted())
    {
      return performDomAction(domElement, webAction, actionValue);
    }
    else
    {
      LOG.error("Error: domElement for " + domElement.getIdentifier() + " is not visible - check your jquery");
      throw new WebActionException("No web component visible on UI " + domElement.getIdentifier());
    }
  }

  private HtmlActionStatus performDomAction(final DomElement domElement, HtmlAction webAction, String actionValue) throws WebActionException
  {
    try
    {
      switch (webAction)
      {
        case ENTER:
          try
          {
            domElement.getDomElement().clear();
          }
          catch (Exception e)
          {
            // do nothing for now.
          }

          CharSequence chars;

          if (actionValue == null)
          {
            chars = domElement.getValue().trim();
          }
          else
          {
            chars = actionValue;
          }

          LOG.info("Entering [" + domElement.getIdentifier() + "] and value is [" + chars + "]");
          if (browser.equals("ie") || browser.equals("safari"))
          {
            domElement.getDomElement().click();
          }

          domElement.getDomElement().sendKeys(chars);
          break;

        case CLICK:
          LOG.info("Clicking [" + domElement.getIdentifier() + "]");
          if (domElement.getDomElement().isEnabled())
          {
            performClickAction(domElement);
          }
          else
          {
            rePopulateDomElement(domElement);

            if (domElement.getDomElement().isEnabled())
            {
              performClickAction(domElement);
            }
            else
            {
              String errorMessage = "";

              if (!domElement.getDomElement().isDisplayed())
              {
                errorMessage = "Cannot click [" + domElement.getIdentifier() + "] as it is not visible.";
              }
              else
              {
                errorMessage = "Cannot click [" + domElement.getIdentifier() + "]. It is visible but still failed to click.";
              }

              WebActionException wae = new WebActionException(errorMessage);

              throw wae;
            }
          }

          break;

        case HOVER:
          LOG.info("Hovering " + domElement.getIdentifier());
          if (domElement.getDomElement().isEnabled())
          {
            performHoverAction(domElement);
          }
          else
          {
            // try to load domElement one more time before we quit
            rePopulateDomElement(domElement);

            if (domElement.getDomElement().isEnabled())
            {
              performHoverAction(domElement);
            }
            else
            {
              String errorMessage = "";

              if (!domElement.getDomElement().isDisplayed())
              {
                errorMessage = "Cannot hover [" + domElement.getIdentifier() + "] as it is not visible.";
              }
              else
              {
                errorMessage = "Cannot hover [" + domElement.getIdentifier() + "]. It is visible but still failed to hover.";
              }

              WebActionException wae = new WebActionException(errorMessage);

              throw wae;
            }
          }

          break;

        case SELECT:
          LOG.info("Selecting " + domElement.getIdentifier());
          performSelectAction(domElement, actionValue);
          break;

        case WAIT:
          LOG.info("Waiting [" + actionValue + "] seconds");
          try
          {
            Thread.sleep(new Long(actionValue) * 1000);
          }
          catch (Exception e)
          {
            e.printStackTrace();
          }

          break;

        case REFRESH:
          LOG.info("Refreshing browser from [" + pageName + "]");
          webDriver.navigate().refresh();
          break;
      }

      return HtmlActionStatus.GOOD;
    }
    catch (WebDriverException wde)
    {
      WebActionException wae = new WebActionException("Element is not actionable", wde);

      throw wae;
    }
  }

  private void performHoverAction(DomElement domElement)
  {
    String mouseOverScript = "if(document.createEvent){var evObj = document.createEvent('MouseEvents');"
        + "evObj.initEvent('mouseover', true, false); arguments[0].dispatchEvent(evObj);} "
        + "else if(document.createEventObject) { arguments[0].fireEvent('onmouseover');}";

    this.jsDriver.executeScript(mouseOverScript, domElement.getDomElement());
  }

  private void performSelectAction(DomElement domElement, String selectValue)
  {
    Select select = new Select(domElement.getDomElement());

    select.selectByVisibleText(selectValue);
  }

  private void performClickAction(DomElement domElement) throws WebActionException
  {
    // always give second chance.
    try
    {
      WebElement element = domElement.getDomElement();

      if (!element.isDisplayed())
      {
        jsDriver.executeScript("window.scrollTo(0,Math.max(document.documentElement.scrollHeight,document.body.scrollHeight,document.documentElement.clientHeight));");
        jsDriver.executeScript("arguments[0].scrollIntoView();", element);
      }

      doClick(element);
    }
    catch (Exception enve)
    {
      rePopulateDomElement(domElement);

      try
      {
        doClick(domElement.getDomElement());
      }
      catch (ElementNotVisibleException enve2)
      {
        if (!domElement.getDomElement().isDisplayed())
        {
          jsDriver.executeScript("window.scrollTo(0,Math.max(document.documentElement.scrollHeight,document.body.scrollHeight,document.documentElement.clientHeight));");
          jsDriver.executeScript("arguments[0].scrollIntoView();", domElement.getDomElement());
        }

        doClick(domElement.getDomElement());
      }
    }
  }

  private void doClick(WebElement element) throws WebActionException
  {
    try
    {
      element.click();
    }
    catch (WebDriverException wde)
    {
      throw new WebActionException("Error executing click event.", wde);
    }
  }

  /**
   * use jsDriver to execute jquery to get dom element.
   *
   * @param domElement
   * @throws WebActionException
   */
  protected void setDomElement(final DomElement domElement) throws WebActionException
  {
    if (domElement.getJquery() != null)
    {
      final String jqueryString = "return " + domElement.getJqueryGetDomElement() + ";";

      WebUIManager.getInstance().loadJQuery(jsDriver);
      LOG.info("Calling jquery script: \"" + jqueryString + "\"");

      try
      {
        (new WebDriverWait(webDriver, (long) actionPace * 5)).until(new ExpectedCondition<Boolean>()
        {
          public Boolean apply(WebDriver d)
          {
            domElement.setDomElement((WebElement) jsDriver.executeScript(jqueryString));
            LOG.info("Executed jquery: \"" + jqueryString + "\"");

            return true;
          }
        });
      }
      catch (Exception e)
      {
        throw new WebActionException("Failed to executing jquery of " + jqueryString, e);
      }
    }

    // all built-ins from selenium
    else if (domElement.getId() != null)
    {
      domElement.setDomElement(webDriver.findElement(By.id(domElement.getId())));
    }
    else if (domElement.getName() != null)
    {
      domElement.setDomElement(webDriver.findElement(By.name(domElement.getName())));
    }
    else if (domElement.getLinkText() != null)
    {
      domElement.setDomElement(webDriver.findElement(By.linkText(domElement.getLinkText())));
    }
    else if (domElement.getClassName() != null)
    {
      domElement.setDomElement(webDriver.findElement(By.className(domElement.getClassName())));
    }
    else if (domElement.getXpath() != null)
    {
      domElement.setDomElement(webDriver.findElement(By.xpath(domElement.getXpath())));
    }
    else if (domElement.getCssSelector() != null)
    {
      domElement.setDomElement(webDriver.findElement(By.cssSelector(domElement.getCssSelector())));
    }
    else if (domElement.getPartialLinkText() != null)
    {
      domElement.setDomElement(webDriver.findElement(By.partialLinkText(domElement.getPartialLinkText())));
    }
    else if (domElement.getTagName() != null)
    {
      domElement.setDomElement(webDriver.findElement(By.tagName(domElement.getTagName())));
    }

    LOG.info("On setDomElement [" + domElement.getIdentifier() + "]");
  }

  private HtmlActionStatus paceLoadComponent(final DomElement domElement) throws WebActionException
  {
    if (domElement.isOptional())
    {
      populateDomElement(domElement);
      LOG.info("On paceLoadDomElement (optional) " + domElement.getIdentifier());

      return HtmlActionStatus.GOOD;
    }
    else
    {
      return tryLoadDomElement(domElement, 0);
    }
  }

  private HtmlActionStatus tryLoadDomElement(final DomElement domElement, int numTry)
  {
    LOG.info("Loading domElement " + domElement.getIdentifier() + " with " + numTry + " number of tries");

    if (numTry > NUMBER_OF_RETRY)
    {
      LOG.error("Can not load web component [" + domElement.getIdentifier() + "] after " + String.valueOf(NUMBER_OF_RETRY + 1) + "] tries."
          + ". Details: dom element exist = " + domElement.isExisted() + "; dom element is visible " + domElement.isVisible());

      return HtmlActionStatus.SHOW_STOPPER;
    }

    try
    {
      int actionTimeoutSeconds = (int) (NUMBER_OF_RETRY * actionPace);
      int actionSleepMilliSeconds = (int) (actionPace * 1000);

      (new WebDriverWait(webDriver, actionTimeoutSeconds, actionSleepMilliSeconds)).until(new ExpectedCondition<Boolean>()
      {
        public Boolean apply(WebDriver d)
        {
          try
          {
            populateDomElement(domElement);
            LOG.info("Populated domElement " + domElement.getIdentifier() + " existed = " + domElement.isExisted() + "; isVisible = "
                + domElement.isVisible());
          }
          catch (WebActionException e)
          {
            return true;
          }

          return domElement.isExisted();
        }
      });

      return HtmlActionStatus.GOOD;
    }
    catch (Exception te)
    {
      LOG.trace("Retrying after exception.", te);
      return tryLoadDomElement(domElement, ++numTry);
    }
  }

  private void explicitWait(long duration)
  {
    try
    {
      Thread.sleep(duration);
    }
    catch (InterruptedException e)
    {
      e.printStackTrace();
    }
  }

  private void rePopulateDomElement(final DomElement domElement) throws WebActionException
  {
    explicitWait((int) (NUMBER_OF_RETRY * actionPace * 1000));
    populateDomElement(domElement);
  }

  private void populateDomElement(DomElement domElement) throws WebActionException
  {
    setDomElement(domElement);

    if (domElement.getDomElement() == null)
    {
      domElement.setExisted(false);
    }
    else
    {
      domElement.setExisted(true);

      if (domElement.getDomElement().isDisplayed())
      {
        domElement.setVisible(true);
      }
    }
  }

  private String getDomElementHtmlValue(DomElement domElement)
  {
    String value;

    if (domElement.getJquery() != null)
    {
      LOG.info("find html value jquery: " + "return " + domElement.getJqueryGetHtml());
      value = (String) jsDriver.executeScript("return " + domElement.getJqueryGetHtml());
    }
    else
    {
      value = domElement.getDomElement().getAttribute("innerHTML");
    }

    return value;
  }

  public boolean checkExpect(DomElementExpectation expect)
  {
    boolean expectMet = true;
    String expectVlaue = (expect.getValue() == null) ? ""
        : expect.getValue();
    String notString = expect.isNegative() ? " not "
        : " ";

    LOG.info("Checking expectation: " + expect.getComponentName() + notString + expect.getCondition() + " " + expectVlaue);

    String componentName = expect.getComponentName();
    DomElement domElement = getDomElement(componentName);

    if (domElement == null)
    {
      LOG.error("Web action failed on page [" + pageName + "]. Cannot find domElement: " + componentName);
      expectMet = false;
      expect.setMessage("Failed to find domElement: " + componentName);
    }

    String condition = expect.getCondition();

    try
    {
      populateDomElement(domElement);
    }
    catch (WebActionException e)
    {
      expect.setMessage(e.getMessage());

      return false;
    }

    if (condition.equalsIgnoreCase(HtmlActionConditionEnum.EXISTS.getValue()))
    {
      if (!domElement.isExisted() && !expect.isNegative())
      {
        expectMet = false;
        expect.setMessage("Expectation failed (expect exist): " + domElement.getIdentifier() + " doesn't exist.");
      }
      else if (domElement.isExisted() && expect.isNegative())
      {
        expectMet = false;
        expect.setMessage("Expectation failed (expect not exist): " + domElement.getIdentifier() + " exists.");
      }

      LOG.info("Expectation is met. The actual does" + notString + "exist");
    }
    else if (condition.equalsIgnoreCase(HtmlActionConditionEnum.VISIBLE.getValue()))
    {
      boolean isVisible = (domElement.getDomElement() != null) && domElement.getDomElement().isDisplayed() && domElement.getDomElement().isEnabled();

      if (!isVisible && !expect.isNegative())
      {
        expectMet = false;
        expect.setMessage("Expectation failed (expected visible), however [" + domElement.getIdentifier() + "] isn't visible.");
        LOG.info("Expectation failed. The actual is invisible.");
      }
      else
      {
        LOG.info("Expectation is met. The actual is visible.");
      }
    }
    else if (condition.equalsIgnoreCase(HtmlActionConditionEnum.INVISIBLE.getValue()))
    {
      boolean isVisible = (domElement.getDomElement() != null) && domElement.getDomElement().isDisplayed() && domElement.getDomElement().isEnabled();

      if (isVisible)
      {
        expectMet = false;
        expect.setMessage("Expectation failed (expected not visible), however [" + domElement.getIdentifier() + "] is visible.");
        LOG.info("Expectation failed. The actual is visible.");
      }
      else
      {
        LOG.info("Expectation is met. The actual is invisible.");
      }
    }
    else
    {
      String htmlValue = getDomElementHtmlValue(domElement);

      htmlValue = htmlValue.replace("&nbsp;", " ").trim();

      if (condition.equalsIgnoreCase(HtmlActionConditionEnum.EQUALS.getValue()))
      {
        // if string, we only care if the expected value was
        // contained.
        if (Utils.isANumber(expect.getValue()))
        {
          double expectValue = Double.parseDouble(expect.getValue());
          double actualValue = Double.parseDouble(htmlValue.replaceAll("[^0-9\\.]", ""));

          if (expectValue != actualValue)
          {
            expectMet = false;
            expect.setMessage("Expectation failed (expected equals): [" + domElement.getIdentifier() + "] expects " + expect.getValue() + "; actual = ["
                + htmlValue + "]");
          }
        }
        else
        {
          if(expect.isCustomAction())
          {
            try
            {
              // create instance of the custom actions and cast the class to our Action interface
              Action instanceOfAction = (Action)expect.getCustomActionClass().newInstance();
              // call the fire method
              String calculatedExpectation = instanceOfAction.fire();
              if (htmlValue.indexOf(calculatedExpectation) < 0)
              {
               expectMet = false;
               expect.setMessage("Expectation failed (expect equals): [" + domElement.getIdentifier() + "] expects value of [" + expect.getValue()
                  + "]; actual value is [" + htmlValue +']');
              }
            }
            catch(InstantiationException | IllegalAccessException e)
            {
              String actionClassString = expect.getValue().trim().substring(Constants.CUSTOM_ACTION_INDICATOR.length());
              LOG.error("Error creating custom action [" + actionClassString + "].", e);
            }
            catch(Exception e)
            {
              LOG.error("Error in custom action", e);
            }

          }
          else if (htmlValue.indexOf(expect.getValue().trim()) < 0)
          {
            expectMet = false;
            expect.setMessage("Expectation failed (expect equals): [" + domElement.getIdentifier() + "] expects value of [" + expect.getValue()
                + "]; actual value is [" + htmlValue +']');
          }
        }

        if (expectMet)
        {
          LOG.info("Expectation is met. The actual is " + htmlValue);
        }
        else
        {
          LOG.info("Expectation failed (expect equals): [" + domElement.getIdentifier() + "] expects [" + expect.getValue().trim() + "]; actual is ["
              + htmlValue + ']');
        }
      }
      else if (condition.equalsIgnoreCase(HtmlActionConditionEnum.NOT_EQUALS.getValue()))
      {
        if (expect.getValue().equalsIgnoreCase(htmlValue.trim()))
        {
          expectMet = false;
          expect.setMessage("Expectation failed (expect not equals): [" + domElement.getIdentifier() + "] expects [" + expect.getValue() + "]; actual is ["
              + htmlValue +']');
        }
      }
      else
      {
        double expectDoubleValue = Double.parseDouble(expect.getValue());
        String actualValue = htmlValue.replaceAll("[^0-9\\.]", "");
        double actualDoubleValue = Double.parseDouble(actualValue);

        if (condition.equalsIgnoreCase(HtmlActionConditionEnum.GREATER_EQUAL_THAN.getValue()))
        {
          if (expectDoubleValue > actualDoubleValue)
          {
            expectMet = false;
            expect.setMessage("Expectation failed (expect >=): " + domElement.getIdentifier() + " expects " + expectDoubleValue + "; actual ="
                + actualDoubleValue);
          }
        }
        else if (condition.equalsIgnoreCase(HtmlActionConditionEnum.GREATER_THAN.getValue()))
        {
          if (expectDoubleValue >= actualDoubleValue)
          {
            expectMet = false;
            expect.setMessage("Expectation failed (expect >): " + domElement.getIdentifier() + " expects " + expectDoubleValue + "; actual ="
                + actualDoubleValue);
          }
        }
        else if (condition.equalsIgnoreCase(HtmlActionConditionEnum.LESS_EQUAL_THAN.getValue()))
        {
          if (expectDoubleValue < actualDoubleValue)
          {
            expectMet = false;
            expect.setMessage("Expectation failed (expect <=): " + domElement.getIdentifier() + " expects " + expectDoubleValue + "; actual ="
                + actualDoubleValue);
          }
        }
        else if (condition.equalsIgnoreCase(HtmlActionConditionEnum.LESS_THAN.getValue()))
        {
          if (expectDoubleValue <= actualDoubleValue)
          {
            expectMet = false;
            expect.setMessage("Expectation failed (expect <): " + domElement.getIdentifier() + " expects " + expectDoubleValue + "; actual ="
                + actualDoubleValue);
          }
        }
      }
    }

    return expectMet;
  }

  public DomElement getDomElement(String componentName)
  {
    // try it with browser name
    DomElement domElement = domElements.get(componentName + "-" + browser.trim());

    // we'll just get it without browser name
    if (domElement == null)
    {
      domElement = domElements.get(componentName);
    }

    // finally we will try to get from pool
    if (domElement == null)
    {
      LOG.info("Cannot get domElement '" + componentName + "' on page " + pageName + "; try to find from the pool.");
      domElement = WebUIManager.getInstance().getDomElementFromPool(componentName + "-" + browser.trim());

      if (domElement == null)
      {
        domElement = WebUIManager.getInstance().getDomElementFromPool(componentName);
      }

      LOG.info("Found domElement '" + componentName + "' configuration on page " + domElement.getPageInfo() + ". Use it for this page.");
    }

    return domElement;
  }

  // getters and setters
  public JavascriptExecutor getJsDriver()
  {
    return jsDriver;
  }

  public WebDriver getWebDriver()
  {
    return webDriver;
  }

  @XmlTransient
  public void setWebDriver(WebDriver webDriver)
  {
    this.webDriver = webDriver;
  }

  @XmlTransient
  public void setJsDriver(JavascriptExecutor jsDriver)
  {
    this.jsDriver = jsDriver;
  }

  public String getTitle()
  {
    return title;
  }

  public void setTitle(String title)
  {
    this.title = title;
  }

  public String getPageName()
  {
    return pageName;
  }

  public void setPageName(String pageName)
  {
    this.pageName = pageName;
  }

  public String getUrlExtension()
  {
    return urlExtension;
  }

  public void setUrlExtension(String urlExtension)
  {
    this.urlExtension = urlExtension;
  }

  public Map<String, DomElement> getDomElements()
  {
    return domElements;
  }

  public void setDomElements(Map<String, DomElement> domElements)
  {
    this.domElements = domElements;
  }

  public boolean isOptional()
  {
    return optional;
  }

  public void setOptional(boolean optional)
  {
    this.optional = optional;
  }

  public String getKeyDomElementName()
  {
    return keyDomElementName;
  }

  public void setKeyDomElementName(String KeyDomElementName)
  {
    this.keyDomElementName = KeyDomElementName;
  }

  public float getPagePace()
  {
    return pagePace;
  }

  public void setPagePace(float pagePace)
  {
    this.pagePace = pagePace;
  }

  public float getActionPace()
  {
    return actionPace;
  }

  public void setActionPace(float actionPace)
  {
    this.actionPace = actionPace;
  }

  public String getBrowser()
  {
    return browser;
  }

  public void setBrowser(String browser)
  {
    this.browser = browser;
  }
}
