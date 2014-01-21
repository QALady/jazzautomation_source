package com.jazzautomation.cucumber;

import com.jazzautomation.action.ComponentAction;
import com.jazzautomation.action.HtmlAction;

import static com.jazzautomation.cucumber.CucumberConstants.AND;
import static com.jazzautomation.cucumber.CucumberConstants.TABLE_COLUMN_SEPARATOR;
import static com.jazzautomation.cucumber.CucumberConstants.THEN;
import com.jazzautomation.cucumber.parser.FeatureParser;
import com.jazzautomation.cucumber.parser.IllegalCucumberFormatException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class And extends CucumberBase
{
  private static final Logger   LOG      = LoggerFactory.getLogger(And.class);
  private List<ComponentAction> actions  = new ArrayList<>();
  private boolean               optional;

  public And()
  {
    setLeadingWords(AND);
    setEndWords(AND, THEN);
  }

  public void process() throws IllegalCucumberFormatException
  {
    List<String>  lines      = FeatureParser.scanIntoLines(getText());
    StringBuilder descBuffer = new StringBuilder();

    descBuffer.append(FeatureParser.filterWords(lines.get(0), AND, ""));

    String[] descriptionEndWords = { TABLE_COLUMN_SEPARATOR };
    int      index               = FeatureParser.retrieveDescription(lines, descBuffer, descriptionEndWords);

    setDescription(descBuffer.toString());
    optional = FeatureParser.isTaskOptional(getDescription());

    // see what is the action for - click, hover, refresh, select, wait
    String[]     words           = getDescription().replaceAll("\n", " ").trim().split(" ");
    HtmlAction[] legalWebActions = HtmlAction.values();
    HtmlAction   action          = null;

    for (String word : words)
    {
      for (HtmlAction webAction : legalWebActions)
      {
        if (webAction.toString().equalsIgnoreCase(word.trim()))
        {
          action = webAction;
        }
      }
    }

    if (action == null)
    {
      LOG.info("And description is [" + getDescription() + ']');
      throw new IllegalCucumberFormatException("Illegal And statement - no valid action can be found in your And statement:\n" + getText()
                                                 + "\nPlease check your configuration.");
    }

    ComponentAction componentAction = new ComponentAction();

    componentAction.setAction(action.toString());
    componentAction.setOptional(optional);

    // for click/hover - expect a webcomponent
    if ((action == HtmlAction.HOVER) || (action == HtmlAction.CLICK))
    {
      FeatureParser.parseHoverAndClick(this, words, componentAction);
    }
    else if (action == HtmlAction.WAIT)
    {
      FeatureParser.parseWait(this, words, componentAction);
    }
    else if ((action == HtmlAction.ENTER) || (action == HtmlAction.SELECT))
    {
      // select and enter actions can be multiples
      // the rest of line are key-value pairs
      String line = lines.get(index);

      if (FeatureParser.isStartWithAWordAfterLineNumber(line, TABLE_COLUMN_SEPARATOR))
      {  // everything is for setting map

        String              mapInString = FeatureParser.normalizeToString(lines, index);
        Map<String, String> inputMap    = FeatureParser.processMap(mapInString);

        for (String key : inputMap.keySet())
        {
          if (!FeatureParser.canFindWebComponent(key.trim()))
          {
            throw new IllegalCucumberFormatException(key + " is not a valid name at " + getText() + ". Please check your configurations.");
          }

          ComponentAction aComponentAction = new ComponentAction();

          aComponentAction.setAction(action.toString());
          aComponentAction.setComponentName(key.trim());
          aComponentAction.setActionValue(inputMap.get(key).trim());
          addActions(aComponentAction);
        }
      }
    }

    // refresh, back and forward
    else
    {
      addActions(componentAction);
    }
  }

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
