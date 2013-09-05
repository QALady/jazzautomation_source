package com.jazzautomation.page;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.openqa.selenium.WebElement;
import com.jazzautomation.action.ConditionAction;

/** The class represents a DOM element on a html page. */
@XmlRootElement
public class DomElement
{
  protected String identifier;

  // we use the following way to find a component. other than jquery, everything else is using Selenium built-in functions
  protected String jquery          = null;
  protected String id              = null;
  protected String name            = null;
  protected String linkText        = null;
  protected String className       = null;
  protected String xpath           = null;
  protected String cssSelector     = null;
  protected String partialLinkText = null;
  protected String tagName         = null;

  // end of how to find a component;
  protected int index = 0;

  // protected String                 jqueryGetValue;
  protected String                 jqueryGetHtml;
  protected String                 jqueryGetDomElement;
  protected boolean                optional;
  protected boolean                existed;
  protected boolean                visible;
  protected String                 value;
  protected String                 browser            = null;
  protected HtmlCondition          webCondition       = null;
  protected ConditionAction        webConditionAction = null;
  @XmlTransient
  private WebElement               domElement;
  @XmlTransient
  private String                   pageInfo;

  public boolean isExisted()
  {
    return existed;
  }

  public void setExisted(boolean existed)
  {
    this.existed = existed;
  }

  public boolean isVisible()
  {
    return visible;
  }

  public void setVisible(boolean visible)
  {
    this.visible = visible;
  }

  public String getJquery()
  {
    return jquery;
  }

  public void setJquery(String jquery)
  {
    this.jquery = jquery;
  }

  public String getIdentifier()
  {
    return identifier;
  }

  public void setIdentifier(String identifier)
  {
    this.identifier = identifier;
  }

  public String getValue()
  {
    return value;
  }

  public void setValue(String value)
  {
    this.value = value;
  }

  public WebElement getDomElement()
  {
    return domElement;
  }

  public String getJqueryGetHtml()
  {
    if (jquery.trim().endsWith("]"))
    {
      return jquery + ".innerHTML;";
    }
    else
    {
      return jquery + "[" + index + "].innerHTML;";
    }
  }

  public String getJqueryGetDomElement()
  {
    if (jquery.trim().endsWith("]"))
    {
      jqueryGetDomElement = jquery;
    }
    else
    {
      jqueryGetDomElement = jquery + "[" + index + "]";
    }

    return jqueryGetDomElement;
  }

  public boolean isOptional()
  {
    return optional;
  }

  public void setOptional(boolean optional)
  {
    this.optional = optional;
  }

  public HtmlCondition getWebCondition()
  {
    return webCondition;
  }

  public void setWebCondition(HtmlCondition webCondition)
  {
    this.webCondition = webCondition;
  }

  public ConditionAction getWebConditionAction()
  {
    return webConditionAction;
  }

  public void setWebConditionAction(ConditionAction webConditionAction)
  {
    this.webConditionAction = webConditionAction;
  }

  @XmlTransient
  public void setDomElement(WebElement domElement)
  {
    this.domElement = domElement;
  }

  public String getPageInfo()
  {
    return pageInfo;
  }

  public int getIndex()
  {
    return index;
  }

  public String getId()
  {
    return id;
  }

  public void setId(String id)
  {
    this.id = id;
  }

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getLinkText()
  {
    return linkText;
  }

  public void setLinkText(String linkText)
  {
    this.linkText = linkText;
  }

  public String getClassName()
  {
    return className;
  }

  public void setClassName(String className)
  {
    this.className = className;
  }

  public String getXpath()
  {
    return xpath;
  }

  public void setXpath(String xpath)
  {
    this.xpath = xpath;
  }

  public String getCssSelector()
  {
    return cssSelector;
  }

  public void setCssSelector(String cssSelector)
  {
    this.cssSelector = cssSelector;
  }

  public String getPartialLinkText()
  {
    return partialLinkText;
  }

  public void setPartialLinkText(String partialLinkText)
  {
    this.partialLinkText = partialLinkText;
  }

  public String getBrowser()
  {
    return browser;
  }

  public void setBrowser(String browser)
  {
    this.browser = browser;
  }

  public String getTagName()
  {
    return tagName;
  }

  public void setTagName(String tagName)
  {
    this.tagName = tagName;
  }

  public void setIndex(int index)
  {
    this.index = index;
  }

  public void setPageInfo(String pageInfo)
  {
    this.pageInfo = pageInfo;
  }

  public String toString()
  {
    return "webcomponent: " + this.identifier + " with jquery: " + this.jquery;
  }
}
