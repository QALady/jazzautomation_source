package com.jazzautomation.action;

import java.util.ArrayList;
import java.util.List;

import com.jazzautomation.page.*;

import static com.jazzautomation.util.Constants.*;

public class PageAction
{
  String                      sourcePageName;
  String                      targetPageName;
  boolean                     optional     = false;
  List<List<ComponentAction>> actionChains = new ArrayList<>();
  List<DomElementExpectation>    expects;

  public boolean isOptional()
  {
    return optional;
  }

  public void setOptional(boolean optional)
  {
    this.optional = optional;
  }

  public String getSourcePageName()
  {
    return sourcePageName;
  }

  public void setSourcePageName(String sourcePageName)
  {
    this.sourcePageName = sourcePageName;
  }

  public String getTargetPageName()
  {
    return targetPageName;
  }

  public void setTargetPageName(String targetPageName)
  {
    this.targetPageName = targetPageName;
  }

  public List<DomElementExpectation> getExpects()
  {
    return expects;
  }

  public void setExpects(List<DomElementExpectation> expects)
  {
    this.expects = expects;
  }

  public List<List<ComponentAction>> getActionChains()
  {
    return actionChains;
  }

  public void setActionChains(List<List<ComponentAction>> actionChains)
  {
    this.actionChains = actionChains;
  }

  public void addComponentAction(String componentActionString)
  {
    String[]        stringSplit = componentActionString.split(DOT);
    ComponentAction compAction  = new ComponentAction();

    compAction.setComponentName(stringSplit[0].trim());
    compAction.setAction(stringSplit[1].toLowerCase());

    if (actionChains.size() == 0)
    {
      List<ComponentAction> componentActionList0 = new ArrayList<>();

      componentActionList0.add(compAction);
      actionChains.add(componentActionList0);
    }
    else
    {
      actionChains.get(0).add(compAction);
    }
  }

  public String serialize()
  {
    StringBuffer serializedText = new StringBuffer();

    serializedText.append(sourcePageName + "");
    serializedText.append(DomElementExpectation.normalizeExpects(expects));
    serializedText.append(":");

    for (int i = 0; i < actionChains.size(); i++)
    {
      List<ComponentAction> listCompActions = actionChains.get(i);

      for (int j = 0; j < listCompActions.size(); j++)
      {
        serializedText.append(listCompActions.get(j).toString());

        if (j != (listCompActions.size() - 1))
        {
          serializedText.append("&&");
        }
      }

      if (i == (actionChains.size() - 1))
      {
        serializedText.append(";");
      }
      else
      {
        serializedText.append("||");
      }
    }

    return serializedText.toString();
  }

  public static List<ComponentAction> deserializeList(String componentActionString)
  {
    List<ComponentAction> componentActions = new ArrayList<>();

    // split actions
    String[] actionsStringSplit = componentActionString.split(DOUBLE_AND);

    for (String actionString : actionsStringSplit)
    {
      ComponentAction aComponentAction = new ComponentAction();

      // parsing action
      parseActionString(actionString, aComponentAction);
      componentActions.add(aComponentAction);
    }

    return componentActions;
  }

  public static String serializeList(List<ComponentAction> componentActions)
  {
    String returnString = "";

    for (int i = 0; i < componentActions.size(); i++)
    {
      returnString += componentActions.get(i).serialize();

      if (i != (componentActions.size() - 1))
      {
        returnString += DOUBLE_AND;
      }
    }

    return returnString;
  }

  private static void parseActionString(String actionString, ComponentAction componentAction)
  {
    String[] splitByDot  = actionString.split(DOT);
    String   startString = splitByDot[0].trim();

    componentAction.setComponentName(startString.trim());
    componentAction.setAction(splitByDot[1].trim());
  }

  public String toString()
  {
    return "PageAction: targetPageName: " + targetPageName + " actionChains: " + actionChains + " with expects: " + expects;
  }
}
