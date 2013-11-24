package com.jazzautomation.action;

import com.jazzautomation.page.DomElementExpectation;

import org.codehaus.jackson.annotate.JsonIgnore;

import java.util.List;

public class ComponentAction
{
  private String                                  componentName;
  private HtmlAction                              action;
  private String                                  actionValue = null;  // only apply to ENTER/SELECT as action
  @JsonIgnore private boolean                     optional;
  @JsonIgnore private List<DomElementExpectation> expects;

  public void setAction(String actionName)
  {
    if (actionName.equalsIgnoreCase(HtmlAction.ENTER.getActionName()))
    {
      action = HtmlAction.ENTER;
    }
    else if (actionName.equalsIgnoreCase(HtmlAction.SELECT.getActionName()))
    {
      action = HtmlAction.SELECT;
    }
    else if (actionName.equalsIgnoreCase(HtmlAction.CLICK.getActionName()))
    {
      action = HtmlAction.CLICK;
    }
    else if (actionName.equalsIgnoreCase(HtmlAction.REFRESH.getActionName()))
    {
      action = HtmlAction.REFRESH;
    }
    else if (actionName.equalsIgnoreCase(HtmlAction.HOVER.getActionName()))
    {
      action = HtmlAction.HOVER;
    }
    else if (actionName.equalsIgnoreCase(HtmlAction.WAIT.getActionName()))
    {
      action = HtmlAction.WAIT;
    }
  }

  @Override public boolean equals(Object o)
  {
    if (!(o instanceof ComponentAction))
    {
      return false;
    }
    else
    {
      return ((ComponentAction) o).getComponentName().equals(componentName)
               && ((ComponentAction) o).getAction().getActionName().equals(action.getActionName());
    }
  }

  public String serialize()
  {
    return componentName + '.' + action.getActionName().toUpperCase();
  }

  public String toString()
  {
    StringBuilder returnStringBuffer = new StringBuilder();

    returnStringBuffer.append(componentName).append('.');

    if (action == HtmlAction.ENTER)
    {
      returnStringBuffer.append(HtmlAction.ENTER.getActionName().toUpperCase()).append('(').append(actionValue).append(')');
    }
    else if (action == HtmlAction.SELECT)
    {
      returnStringBuffer.append(HtmlAction.SELECT.getActionName().toUpperCase()).append('(').append(actionValue).append(')');
    }
    else
    {
      returnStringBuffer.append(action.getActionName().toUpperCase());
    }

    returnStringBuffer.append(DomElementExpectation.normalizeExpects(expects));

    return returnStringBuffer.toString();
  }

  public void setComponentName(String componentName)
  {
    this.componentName = componentName;
  }

  public void setOptional(boolean optional)
  {
    this.optional = optional;
  }

  public void setActionValue(String actionValue)
  {
    this.actionValue = actionValue;
  }

  public String getComponentName()
  {
    return componentName;
  }

  public HtmlAction getAction()
  {
    return action;
  }

  public String getActionValue()
  {
    return actionValue;
  }

  public boolean isOptional()
  {
    return optional;
  }
}
