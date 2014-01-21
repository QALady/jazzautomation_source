package com.jazzautomation.cucumber;

import com.jazzautomation.WebUIManager;

import static com.jazzautomation.cucumber.CucumberConstants.AND;
import static com.jazzautomation.cucumber.CucumberConstants.GIVEN;
import static com.jazzautomation.cucumber.CucumberConstants.ON;
import static com.jazzautomation.cucumber.CucumberConstants.TABLE_COLUMN_SEPARATOR;
import static com.jazzautomation.cucumber.CucumberConstants.THEN;
import com.jazzautomation.cucumber.parser.FeatureParser;
import com.jazzautomation.cucumber.parser.IllegalCucumberFormatException;

import com.jazzautomation.page.Page;

import org.codehaus.jackson.annotate.JsonIgnore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Given extends CucumberBase
{
  @JsonIgnore
  private Page                page;
  private boolean             forBackground;
  private Map<String, String> settings = new HashMap<>();

  public Given()
  {
    setLeadingWords(GIVEN);
    setEndWords(AND, THEN);
  }

  public void process() throws IllegalCucumberFormatException
  {
    List<String>  lines      = FeatureParser.scanIntoLines(getText());
    StringBuilder descBuffer = new StringBuilder();

    descBuffer.append(FeatureParser.filterWords(lines.get(0), GIVEN, "")).append('\n');

    String[] descriptionEndWords = { TABLE_COLUMN_SEPARATOR };
    int      index               = FeatureParser.retrieveDescription(lines, descBuffer, descriptionEndWords);

    setDescription(descBuffer.toString());

    // handle given for Background
    if (forBackground)
    {
      processBackground(lines, index);
    }
    else
    {
      processOthers();
    }
  }

  private void processBackground(List<String> lines, int index) throws IllegalCucumberFormatException
  {
    String line = lines.get(index);

    if (FeatureParser.isStartWithAWordAfterLineNumber(line, TABLE_COLUMN_SEPARATOR))
    {                                                    // everything is for setting map

      String mapInString = FeatureParser.normalizeToString(lines, index);

      settings = FeatureParser.processMap(mapInString);  // todo overrirde browser settings here
    }
  }

  private void processOthers() throws IllegalCucumberFormatException
  {
    // should find a webPage
    String[]     words        = getDescription().replaceAll("\n", " ").trim().split(" ");
    List<String> specialWords = FeatureParser.retrieveSpecialWords(words);

    if (specialWords.isEmpty())
    {
      int i = 0;

      // find any thing after ON
      for (; i < words.length; i++)
      {
        if (words[i].trim().toLowerCase().endsWith(ON))
        {
          break;
        }
      }

      specialWords.add(words[i + 1]);
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
      page = WebUIManager.getInstance().getPage(webPageKey);
    }
    else
    {
      throw new IllegalCucumberFormatException("Can not find a valid web page for Given statement:\n" + getText()
                                                 + "\nPlease check your configuration file.");
    }
  }

  // --------------------- GETTER / SETTER METHODS ---------------------
  public Page getPage()
  {
    return page;
  }

  public void setPage(Page page)
  {
    this.page = page;
  }

  public Map<String, String> getSettings()
  {
    return settings;
  }

  public void setSettings(Map<String, String> settings)
  {
    this.settings = settings;
  }

  public boolean isForBackground()
  {
    return forBackground;
  }

  public void setForBackground(boolean forBackground)
  {
    this.forBackground = forBackground;
  }
}
