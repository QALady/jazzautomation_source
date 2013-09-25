package com.jazzautomation.cucumber;

import java.util.ArrayList;
import java.util.List;
import org.codehaus.jackson.annotate.JsonIgnore;

import com.jazzautomation.action.ComponentAction;

public class And extends CucumberBase
{
  private List<ComponentAction> actions  = new ArrayList<>();
  private boolean               optional;

  public List<ComponentAction> getActions()
  {
    return actions;
  }

  public void addActions(ComponentAction action)
  {
    actions.add(action);
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
