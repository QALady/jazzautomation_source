package com.jazzautomation.report;

import com.jazzautomation.action.ComponentAction;
import com.jazzautomation.cucumber.And;

public class ActionResult extends ResultBase
{
  private And             and;
  private ComponentAction action;

  public And getAnd()
  {
    return and;
  }

  public void setAnd(And and)
  {
    this.and = and;
  }

  public ComponentAction getAction()
  {
    return action;
  }

  public void setAction(ComponentAction componentAction)
  {
    this.action = componentAction;
  }
}
