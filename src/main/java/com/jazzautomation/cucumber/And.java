package com.jazzautomation.cucumber;

import com.jazzautomation.action.ComponentAction;

import java.util.ArrayList;
import java.util.List;

public class And extends CucumberBase
{
  private List<ComponentAction> actions  = new ArrayList<>();
  private boolean               optional;

  public void addActions(ComponentAction action)
  {
    actions.add(action);
  }

  // --------------------- GETTER / SETTER METHODS ---------------------
  public List<ComponentAction> getActions()
  {
    return actions;
  }

  public boolean isOptional()
  {
    return optional;
  }

  public void setOptional(boolean optional)
  {
    this.optional = optional;
  }
}
