package com.jazzautomation.cucumber;

import com.jazzautomation.WebUIManager;

import static com.jazzautomation.cucumber.CucumberConstants.ON;
import static com.jazzautomation.cucumber.CucumberConstants.TABLE_COLUMN_SEPARATOR;
import static com.jazzautomation.cucumber.CucumberConstants.THEN;
import com.jazzautomation.cucumber.parser.FeatureParser;
import com.jazzautomation.cucumber.parser.IllegalCucumberFormatException;

import com.jazzautomation.page.DomElementExpectation;
import com.jazzautomation.page.HtmlActionConditionEnum;
import com.jazzautomation.page.Page;

import com.jazzautomation.util.Constants;

import org.codehaus.jackson.annotate.JsonIgnore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Then extends CucumberBase
{
  private static final Logger         LOG          = LoggerFactory.getLogger(Then.class);
  @JsonIgnore
  private Page                        pageExpected;
  private boolean                     forExpects;
  private List<DomElementExpectation> expects      = new ArrayList<>();

  public Then(String... endWords)
  {
    setLeadingWords(THEN);
    setEndWords(endWords);
  }

  public void process() throws IllegalCucumberFormatException
  {
    List<String>  lines      = FeatureParser.scanIntoLines(getText());
    StringBuilder descBuffer = new StringBuilder();

    descBuffer.append(FeatureParser.filterWords(lines.get(0), THEN, ""));

    String[] descriptionEndWords = { TABLE_COLUMN_SEPARATOR };
    int      index               = FeatureParser.retrieveDescription(lines, descBuffer, descriptionEndWords);

    setDescription(descBuffer.toString());

    String[]     words        = getDescription().replaceAll("\n", " ").trim().split(" ");
    List<String> specialWords = FeatureParser.retrieveSpecialWords(words);

    // these words mean a Page
    if (specialWords.isEmpty())
    {
      int     i     = 0;
      boolean hasOn = false;

      // find any thing after ON
      for (; i < words.length; i++)
      {
        if (words[i].trim().toLowerCase().endsWith(ON))
        {
          hasOn = true;

          break;
        }
      }

      if (hasOn)
      {
        specialWords.add(words[i + 1]);
      }
    }

    Set<String> pageKeys   = WebUIManager.getInstance().getPages().keySet();
    String      webPageKey = null;

    for (String word : specialWords)
    {
      if (pageKeys.contains(word.trim()))
      {
        webPageKey = word.trim();

        break;
      }
    }

    if (webPageKey != null)
    {
      pageExpected = WebUIManager.getInstance().getPage(webPageKey);
    }

    if (index < lines.size())
    {
      // do something - select can be multiple and enter can be multiple
      // the rest of line are key-value pairs
      String line = lines.get(index);

      if (FeatureParser.isStartWithAWordAfterLineNumber(line, TABLE_COLUMN_SEPARATOR))
      {  // everything is for setting map

        String              mapInString = FeatureParser.normalizeToString(lines, index);
        Map<String, String> expectMap   = FeatureParser.processMap(mapInString);

        for (String key : expectMap.keySet())
        {
          processExpect(expectMap, key);
        }
      }
    }
    else if (webPageKey == null)
    {
      throw new IllegalCucumberFormatException("Can not find a valid page for Then statement " + getText());
    }
  }

  private void processExpect(Map<String, String> expectMap, String key) throws IllegalCucumberFormatException
  {
    if (!FeatureParser.canFindWebComponent(key.trim()))
    {
      throw new IllegalCucumberFormatException("Component with name [" + key.trim() + "] is not a valid with text [" + getText()
                                                 + "]. Please check your configurations.");
    }

    DomElementExpectation expect = new DomElementExpectation();

    expect.setComponentName(key.trim());

    if (expectMap.get(key).trim().equalsIgnoreCase(HtmlActionConditionEnum.VISIBLE.getValue()))
    {
      expect.setCondition(HtmlActionConditionEnum.VISIBLE.getValue());
    }
    else if (expectMap.get(key).trim().equalsIgnoreCase(HtmlActionConditionEnum.INVISIBLE.getValue()))
    {
      expect.setCondition(HtmlActionConditionEnum.INVISIBLE.getValue());
    }
    else
    {
      expect.setCondition(HtmlActionConditionEnum.EQUALS.getValue());
      expect.setValue(expectMap.get(key).trim());

      String expectationString = expect.getValue().trim();

      if (expectationString.startsWith(Constants.CUSTOM_ACTION_INDICATOR))
      {
        String actionClass = expectationString.substring(Constants.CUSTOM_ACTION_INDICATOR.length());

        try
        {
          Class clazz = Class.forName(actionClass);

          expect.setCustomActionClass(clazz);
          expect.setCustomAction(true);
        }
        catch (Exception e)
        {
          LOG.error("Error creating custom action", e);
          throw new IllegalCucumberFormatException("Error creating custom action.", e);
        }
      }
    }

    addExpect(expect);
  }

  public void addExpect(DomElementExpectation expect)
  {
    expects.add(expect);
  }

  // --------------------- GETTER / SETTER METHODS ---------------------
  public List<DomElementExpectation> getExpects()
  {
    return expects;
  }

  public Page getPageExpected()
  {
    return pageExpected;
  }

  public void setPageExpected(Page pageExpected)
  {
    this.pageExpected = pageExpected;
  }

  public boolean isForExpects()
  {
    return forExpects;
  }

  public void setForExpects(boolean forExpects)
  {
    this.forExpects = forExpects;
  }
}
