package com.jazzautomation.action;

import java.util.List;
import org.codehaus.jackson.annotate.JsonIgnore;
import com.jazzautomation.page.DomElementExpect;

public class ComponentAction
{
  String                               componentName;
  HtmlAction                            action;
  String                               actionValue = null;  // only apply to ENTER/SELECT as action
  @JsonIgnore
  boolean                              optional;
  @JsonIgnore
  List<DomElementExpect>             expects;

  public String getComponentName()
  {
    return componentName;
  }

  public void setComponentName(String componentName)
  {
    this.componentName = componentName;
  }

  public HtmlAction getAction()
  {
    return action;
  }

  public String getActionValue()
  {
    return actionValue;
  }

  public void setActionValue(String actionValue)
  {
    this.actionValue = actionValue;
  }

  public List<DomElementExpect> getExpects()
  {
    return expects;
  }

  public boolean isOptional()
  {
    return optional;
  }

  public void setOptional(boolean optional)
  {
    this.optional = optional;
  }

  public void setExpects(List<DomElementExpect> expects)
  {
    this.expects = expects;
  }

  public void setAction(String actionName)
  {
    if (actionName.equalsIgnoreCase(HtmlAction.ENTER.getActionName()))
    {
      this.action = HtmlAction.ENTER;
    }
    else if (actionName.equalsIgnoreCase(HtmlAction.SELECT.getActionName()))
    {
      this.action = HtmlAction.SELECT;
    }
    else if (actionName.equalsIgnoreCase(HtmlAction.CLICK.getActionName()))
    {
      this.action = HtmlAction.CLICK;
    }
    else if (actionName.equalsIgnoreCase(HtmlAction.REFRESH.getActionName()))
    {
      this.action = HtmlAction.REFRESH;
    }
    else if (actionName.equalsIgnoreCase(HtmlAction.HOVER.getActionName()))
    {
      this.action = HtmlAction.HOVER;
    }
    else if (actionName.equalsIgnoreCase(HtmlAction.WAIT.getActionName()))
    {
      this.action = HtmlAction.WAIT;
    }
  }

  @Override
  public boolean equals(Object o)
  {
    if (!(o instanceof ComponentAction))
    {
      return false;
    }
    else
    {
      return ((ComponentAction) o).getComponentName().equals(this.componentName)
               && ((ComponentAction) o).getAction().getActionName().equals(this.action.getActionName());
    }
  }

  public String serialize()
  {
    return componentName + "." + action.getActionName().toUpperCase();
  }

  public String toString()
  {
    StringBuffer returnStringBuffer = new StringBuffer();

    returnStringBuffer.append(componentName + ".");

    if (action.equals(HtmlAction.ENTER))
    {
      returnStringBuffer.append(HtmlAction.ENTER.getActionName().toUpperCase() + "(" + actionValue + ")");
    }
    else if (action.equals(HtmlAction.SELECT))
    {
      returnStringBuffer.append(HtmlAction.SELECT.getActionName().toUpperCase() + "(" + actionValue + ")");
    }
    else
    {
      returnStringBuffer.append(action.getActionName().toUpperCase());
    }

    returnStringBuffer.append(DomElementExpect.normalizeExpects(expects));

    return returnStringBuffer.toString();
  }
}
