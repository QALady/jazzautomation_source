package com.jazzautomation.cucumber.parser;

import com.jazzautomation.WebUIManager;

import com.jazzautomation.action.ComponentAction;
import com.jazzautomation.action.HtmlAction;

import com.jazzautomation.cucumber.And;
import com.jazzautomation.cucumber.Background;
import com.jazzautomation.cucumber.CucumberBase;
import static com.jazzautomation.cucumber.CucumberConstants.AND;
import static com.jazzautomation.cucumber.CucumberConstants.BACKGROUND;
import static com.jazzautomation.cucumber.CucumberConstants.COLON;
import static com.jazzautomation.cucumber.CucumberConstants.ESCAPE_CHAR;
import static com.jazzautomation.cucumber.CucumberConstants.FEATURE;
import static com.jazzautomation.cucumber.CucumberConstants.GIVEN;
import static com.jazzautomation.cucumber.CucumberConstants.LINE_END_MARK;
import static com.jazzautomation.cucumber.CucumberConstants.LINE_START_MARK;
import static com.jazzautomation.cucumber.CucumberConstants.ON;
import static com.jazzautomation.cucumber.CucumberConstants.OPTIONAL;
import static com.jazzautomation.cucumber.CucumberConstants.SCENARIO;
import static com.jazzautomation.cucumber.CucumberConstants.TABLE_COLUMN_SEPERATOR;
import static com.jazzautomation.cucumber.CucumberConstants.TABLE_COLUMN_SEPERATOR_CHAR;
import static com.jazzautomation.cucumber.CucumberConstants.THEN;
import com.jazzautomation.cucumber.Feature;
import com.jazzautomation.cucumber.Given;
import com.jazzautomation.cucumber.Scenario;
import com.jazzautomation.cucumber.Then;

import com.jazzautomation.page.DomElement;
import com.jazzautomation.page.DomElementExpectation;
import com.jazzautomation.page.HtmlActionConditionEnum;
import com.jazzautomation.page.Page;

import com.jazzautomation.util.Constants;
import static com.jazzautomation.util.Constants.SUPPORTED_BROWSERS;

import org.apache.commons.lang3.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/**
 * A singleton class to parse a Cucumber file at a time. The only public method is parse - take FileInputStream to parse through and return a Feature
 * object. In case of ill-formats, throws IllegalCucumberFormatException.
 */
public class FeatureParser
{
  private static final String  COMMENT_MARKER = "#";
  private static Logger        LOG            = LoggerFactory.getLogger(FeatureParser.class);
  private static FeatureParser instance       = null;

  /** Standard singleton implementation. */
  public static FeatureParser getInstance()
  {
    if (instance == null)
    {
      instance = new FeatureParser();
    }

    return instance;
  }

  /** private constructor. */
  private FeatureParser() {}

  /**
   * parse a file return a Feature object. Throw IllegalCucumberFormatException and stop if there is formatting errors
   *
   * @param  originalLines
   */
  public Feature parse(List<String> originalLines) throws IllegalCucumberFormatException
  {
    // jsheridan CODEREVIEW - long method break up for clarity
    Feature       feature            = new Feature();
    List<String>  stringsForFile     = new ArrayList<>();
    StringBuilder originalTextBuffer = new StringBuilder();
    int           lineNum            = 1;

    for (String line : originalLines)
    {
      // if (StringUtils.isEmpty(line))
      // {
      // continue;
      // }
      String trimmedLine = line.trim();

      originalTextBuffer.append(line).append('\n');

      String formattedLine = LINE_START_MARK + (lineNum++) + LINE_END_MARK + trimmedLine;

      if (StringUtils.isNotEmpty(trimmedLine) && trimmedLine.startsWith(COMMENT_MARKER))
      {  // skip adding comment lines to the executable feature.
        stringsForFile.add("\n");

        continue;
      }

      stringsForFile.add(formattedLine);
    }

    // add an extra line at the end of file.
    stringsForFile.add("\n");
    feature.setOriginalText(originalTextBuffer.toString());

    // setup feature description - background is optional
    String[] featureEndWords = { BACKGROUND, SCENARIO };
    int      index           = 0;

    index = setupText(feature, stringsForFile, index, FEATURE, featureEndWords);

    List<String>  lines      = scanIntoLine(feature.getText());
    StringBuilder descBuffer = new StringBuilder();

    descBuffer.append(filterWords(lines.get(0), FEATURE, COLON)).append('\n');

    String[] descriptionEndWords = {};

    retrieveDescription(lines, descBuffer, descriptionEndWords);
    feature.setDescription(descBuffer.toString());

    // setup background
    String[]   backgroundEndWords = { SCENARIO };
    Background background         = new Background();

    index = setupText(background, stringsForFile, index, BACKGROUND, backgroundEndWords);

    if (background.getText() != null)
    {
      processBackground(background);
      feature.setBackground(background);
    }

    // loop through scenarios and add to list
    // setup scenarios
    String[] scenarioEndWords = { SCENARIO };

    while (index < stringsForFile.size())
    {
      Scenario scenario = new Scenario();

      index = setupText(scenario, stringsForFile, index, SCENARIO, scenarioEndWords);
      processScenario(scenario);
      feature.addScenario(scenario);
    }

    LOG.info("Successfully parsed feature: " + feature.getOriginalText());

    return feature;
  }

  // jsheridan CODEREVIEW - unit test would be nice
  private int setupText(CucumberBase cucumberObject, List<String> stringsForFile, int index, String leadingWord, String[] endWords)
  {
    String currentLine = stringsForFile.get(index);

    if (startWithAWordAfterLineNumber(currentLine, leadingWord))
    {
      StringBuilder text = new StringBuilder();

      text.append(currentLine).append('\n');

      while (index < stringsForFile.size())
      {
        if (index == (stringsForFile.size() - 1))
        {
          break;
        }

        index++;

        String nextLine = stringsForFile.get(index);

        if (atEnd(nextLine, endWords))
        {
          break;
        }
        else
        {  // take the line and move the index
          text.append(nextLine).append('\n');
        }
      }

      if (index == (stringsForFile.size() - 1))
      {
        index = stringsForFile.size();
      }

      cucumberObject.setText(text.toString());
    }

    return index;
  }

  private void processScenario(Scenario scenario) throws IllegalCucumberFormatException
  {
    List<String> lines               = scanIntoLine(scenario.getText());
    String[]     descriptionEndWords = { GIVEN };
    int          index               = setupDescription(scenario, lines, SCENARIO, COLON, descriptionEndWords);

    scenario.setOptional(isTaskOptional(scenario.getDescription()));

    // handle Given, And and Then
    while (index < lines.size())
    {
      String line = lines.get(index);

      if (startWithAWordAfterLineNumber(line, GIVEN))
      {
        Given    given         = new Given();
        String[] givenEndWords = { AND, THEN };

        index = setupText(given, lines, index, GIVEN, givenEndWords);
        processGiven(given);
        scenario.setGiven(given);
      }
      else if (startWithAWordAfterLineNumber(line, AND))
      {
        And      and         = new And();
        String[] andEndWords = { AND, THEN };

        index = setupText(and, lines, index, AND, andEndWords);
        processAnd(and);
        scenario.addAnd(and);
      }
      else if (startWithAWordAfterLineNumber(line, THEN))
      {
        Then     then         = new Then();
        String[] thenEndWords = {};

        index = setupText(then, lines, index, THEN, thenEndWords);
        processThen(then);
        scenario.setThen(then);
      }
    }
  }

  private boolean isTaskOptional(String description)
  {
    boolean isOptional = false;

    if (description.trim().toLowerCase().startsWith("("))
    {  // it maybe an optional scenario

      String possibleStringForOption = description.substring(description.indexOf('(') + 1);

      LOG.info("Is this optional: " + possibleStringForOption);

      if (possibleStringForOption.trim().toLowerCase().startsWith(OPTIONAL))
      {
        isOptional = true;

        if (LOG.isDebugEnabled())
        {
          LOG.debug("Returning optional flag [" + isOptional + ']');
        }
      }
    }

    return isOptional;
  }

  private void processBackground(Background background) throws IllegalCucumberFormatException
  {
    List<String> lines               = scanIntoLine(background.getText());
    String[]     descriptionEndWords = { GIVEN };
    int          index               = setupDescription(background, lines, SCENARIO, COLON, descriptionEndWords);
    String       line                = lines.get(index);

    // everything here is for "given"
    if (startWithAWordAfterLineNumber(line, GIVEN))
    {
      Given given = new Given();

      given.setText(normalizeToString(lines, index));
      given.setForBackground(true);
      processGiven(given);
      background.setGiven(given);
    }
  }

  // jsheridan CODEREVIEW - break up into smaller methods for clarity
  private void processGiven(Given given) throws IllegalCucumberFormatException
  {
    List<String>  lines      = scanIntoLine(given.getText());
    StringBuilder descBuffer = new StringBuilder();

    descBuffer.append(filterWords(lines.get(0), GIVEN, "")).append('\n');

    String[] descriptionEndWords = { TABLE_COLUMN_SEPERATOR };
    int      index               = retrieveDescription(lines, descBuffer, descriptionEndWords);

    given.setDescription(descBuffer.toString());

    // handle given for Background
    if (given.isForBackground())
    {
      String line = lines.get(index);

      if (startWithAWordAfterLineNumber(line, TABLE_COLUMN_SEPERATOR))
      {  // everything is for setting map

        String mapInString = normalizeToString(lines, index);

        given.setSettings(processMap(mapInString));
      }
    }
    else
    {    // should find a webPage

      String[]     words        = given.getDescription().replaceAll("\n", " ").trim().split(" ");
      List<String> specialWords = retrieveSpecialWords(words);

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
        Page page = WebUIManager.getInstance().getPage(webPageKey);

        given.setPage(page);
      }
      else
      {
        throw new IllegalCucumberFormatException("Can not find a valid web page for Given statement:\n" + given.getText()
                                                   + "\nPlease check your configuration file.");
      }
    }
  }

  private void processAnd(And and) throws IllegalCucumberFormatException
  {
    List<String>  lines      = scanIntoLine(and.getText());
    StringBuilder descBuffer = new StringBuilder();

    descBuffer.append(filterWords(lines.get(0), AND, ""));

    String[] descriptionEndWords = { TABLE_COLUMN_SEPERATOR };
    int      index               = retrieveDescription(lines, descBuffer, descriptionEndWords);

    and.setDescription(descBuffer.toString());
    and.setOptional(isTaskOptional(and.getDescription()));

    // see what is the action for - click, hover, refresh, select, wait
    String[]     words           = and.getDescription().replaceAll("\n", " ").trim().split(" ");
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
      LOG.info("And description is [" + and.getDescription() + ']');
      throw new IllegalCucumberFormatException("Illegal And statement - no valid action can be found in your And statement:\n" + and.getText()
                                                 + "\nPlease check your configuration.");
    }

    ComponentAction componentAction = new ComponentAction();

    componentAction.setAction(action.toString());
    componentAction.setOptional(and.isOptional());

    // for click/hover - expect a webcomponent
    if ((action == HtmlAction.HOVER) || (action == HtmlAction.CLICK))
    {
      parseHoverAndClick(and, words, componentAction);
    }
    else if (action == HtmlAction.WAIT)
    {
      parseWait(and, words, componentAction);
    }
    else if ((action == HtmlAction.ENTER) || (action == HtmlAction.SELECT))
    {
      // select and enter actions can be multiples
      // the rest of line are key-value pairs
      String line = lines.get(index);

      if (startWithAWordAfterLineNumber(line, TABLE_COLUMN_SEPERATOR))
      {  // everything is for setting map

        String              mapInString = normalizeToString(lines, index);
        Map<String, String> inputMap    = processMap(mapInString);

        for (String key : inputMap.keySet())
        {
          if (!canFindWebComponent(key.trim()))
          {
            throw new IllegalCucumberFormatException(key + " is not a valid name at " + and.getText() + ". Please check your configurations.");
          }

          ComponentAction aComponentAction = new ComponentAction();

          aComponentAction.setAction(action.toString());
          aComponentAction.setComponentName(key.trim());
          aComponentAction.setActionValue(inputMap.get(key).trim());
          and.addActions(aComponentAction);
        }
      }
    }

    // refresh, back and forward
    else
    {
      and.addActions(componentAction);
    }
  }

  // jsheridan CODEREVIEW - very long method - break up
  private void processThen(Then then) throws IllegalCucumberFormatException
  {
    List<String>  lines      = scanIntoLine(then.getText());
    StringBuilder descBuffer = new StringBuilder();

    descBuffer.append(filterWords(lines.get(0), THEN, ""));

    String[] descriptionEndWords = { TABLE_COLUMN_SEPERATOR };
    int      index               = retrieveDescription(lines, descBuffer, descriptionEndWords);

    then.setDescription(descBuffer.toString());

    String[]     words        = then.getDescription().replaceAll("\n", " ").trim().split(" ");
    List<String> specialWords = retrieveSpecialWords(words);

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
      Page page = WebUIManager.getInstance().getPage(webPageKey);

      then.setPageExpected(page);
    }

    if (index < lines.size())
    {
      // do something - select can be multiple and enter can be multiple
      // the rest of line are key-value pairs
      String line = lines.get(index);

      if (startWithAWordAfterLineNumber(line, TABLE_COLUMN_SEPERATOR))
      {  // everything is for setting map

        String              mapInString = normalizeToString(lines, index);
        Map<String, String> expectMap   = processMap(mapInString);

        for (String key : expectMap.keySet())
        {
          if (!canFindWebComponent(key.trim()))
          {
            throw new IllegalCucumberFormatException("Component with name [" + key.trim() + "] is not a valid with text [" + then.getText()
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

          then.addExpect(expect);
        }
      }
    }

    //
    else if (webPageKey == null)
    {
      throw new IllegalCucumberFormatException("Can not find a valid page for Then statement " + then.getText());
    }
  }

  private Map<String, String> processMap(String string) throws IllegalCucumberFormatException
  {
    Map<String, String> keyValuePair = new LinkedHashMap<>();
    List<String>        lines        = scanIntoLine(string);

    // every line is a key value pair
    for (String line : lines)
    {
      line = filterLineNumber(line);

      if (isTableStructureValid(line))
      {
        int    firstSeparatorIndex = line.indexOf(TABLE_COLUMN_SEPERATOR);
        int    lastSeparatorIndex  = line.lastIndexOf(TABLE_COLUMN_SEPERATOR);
        String innerString         = line.substring(firstSeparatorIndex + 1, lastSeparatorIndex);
        int    separatorIndex      = innerString.indexOf(TABLE_COLUMN_SEPERATOR);

        keyValuePair.put(innerString.substring(0, separatorIndex).trim(), innerString.substring(separatorIndex + 1));
      }
      else
      {
        if (StringUtils.isBlank(line))
        {
          continue;
        }
        else if (line.trim().startsWith(COMMENT_MARKER))
        {
          LOG.info("Skipping line [" + line.trim() + ']');

          continue;
        }

        throw new IllegalCucumberFormatException("The following line could not be processed; it was invalid. Line = [" + line + ']');
      }
    }

    return keyValuePair;
  }

  private void parseHoverAndClick(And and, String[] words, ComponentAction componentAction) throws IllegalCucumberFormatException
  {
    List<String> specialWords = retrieveSpecialWords(words);

    if (specialWords.isEmpty())
    {
      int i = 0;

      // find any thing after hover/click
      for (; i < words.length; i++)
      {
        if (words[i].trim().toUpperCase().endsWith(HtmlAction.HOVER.toString())
              || words[i].trim().toUpperCase().endsWith(HtmlAction.CLICK.toString()))
        {
          break;
        }
      }

      if ((i + 1) < words.length)
      {
        specialWords.add(words[i + 1]);
      }
    }

    boolean hasWebComponent = false;
    String  componentName   = "";

    for (String word : specialWords)
    {
      if (canFindWebComponent(word))
      {
        hasWebComponent = true;
        componentName   = word;

        break;
      }
    }

    if (!hasWebComponent)
    {
      throw new IllegalCucumberFormatException("Illegal 'And' statement - cannot find a valid dom component in your And statement [" + and.getText()
                                                 + "]. Please check your configuration.");
    }

    componentAction.setComponentName(componentName);
    and.addActions(componentAction);
  }

  private void parseWait(And and, String[] words, ComponentAction componentAction)
  {
    // find the "wait" word
    int index = 0;

    for (; index < words.length; index++)
    {
      if (words[index].trim().toUpperCase().equals(HtmlAction.WAIT.toString()))
      {
        break;
      }
    }

    // can be 5s. 5 s, 5.0seconds, 5.0 seconds
    if (index < (words.length - 1))
    {
      String waitValue = words[index + 1].trim().toLowerCase();
      int    sIndex    = waitValue.indexOf('s');

      if (sIndex != -1)
      {
        waitValue = waitValue.substring(0, sIndex);
      }

      componentAction.setActionValue(waitValue);
    }

    and.addActions(componentAction);
  }

  private int setupDescription(CucumberBase cucumber, List<String> lines, String leadingWord, String followingWord, String[] endWords)
  {
    StringBuilder descBuffer = new StringBuilder();

    descBuffer.append(filterWords(lines.get(0), leadingWord, followingWord)).append('\n');

    int index = retrieveDescription(lines, descBuffer, endWords);

    cucumber.setDescription(descBuffer.toString());

    return index;
  }

  private boolean isTableStructureValid(String string)
  {
    // we should have three individual table separators
    int count = 0;

    for (int i = 0; i < string.length(); i++)
    {
      if (string.charAt(i) == TABLE_COLUMN_SEPERATOR_CHAR)
      {
        if (i == 0)
        {
          count++;
        }
        else if (string.charAt(i - 1) != ESCAPE_CHAR)
        {
          count++;
        }
      }
    }

    return count == 3;
  }

  private String normalizeToString(List<String> lines, int index)
  {
    StringBuilder stringBuilder = new StringBuilder();

    while (index < lines.size())
    {
      String line = lines.get(index);

      stringBuilder.append(line).append('\n');
      index++;
    }

    return stringBuilder.toString();
  }

  private int retrieveDescription(List<String> lines, StringBuilder descBuffer, String[] descriptionEndWords)
  {
    int index = 1;

    while (index < lines.size())
    {
      String aline = filterLineNumber(lines.get(index));

      if (atEnd(aline, descriptionEndWords))
      {
        break;
      }
      else
      {
        descBuffer.append(aline).append('\n');
        index++;
      }
    }

    return index;
  }

  private String filterWords(String string, String leadingWord, String followingWord)
  {
    String filteredString = filterLineNumber(string).trim();

    if (filteredString.toLowerCase().startsWith(leadingWord))
    {
      filteredString = filteredString.substring(leadingWord.length()).trim();

      if (filteredString.toLowerCase().startsWith(followingWord))
      {
        filteredString = filteredString.substring(followingWord.length()).trim();
      }
    }

    return filteredString;
  }

  private boolean atEnd(String string, String[] endWords)
  {
    for (String word : endWords)
    {
      if (startWithAWordAfterLineNumber(string, word))
      {
        return true;
      }
    }

    return false;
  }

  private List<String> scanIntoLine(String string)
  {
    Scanner      scanner = new Scanner(string);
    List<String> list    = new ArrayList<>();

    while (scanner.hasNextLine())
    {
      list.add(scanner.nextLine());
    }

    return list;
  }

  private List<String> retrieveSpecialWords(String[] words)
  {
    List<String> specialWords = new ArrayList<>();

    for (String word : words)
    {
      if (word.trim().startsWith("\"") && word.trim().endsWith("\""))
      {
        specialWords.add(word.trim().substring(1, word.trim().length() - 1));
      }
    }

    return specialWords;
  }

  private boolean startWithAWordAfterLineNumber(String string, String word)
  {
    if (string.startsWith(LINE_START_MARK))
    {
      String filteredString = filterLineNumber(string);

      return filteredString.trim().toLowerCase().startsWith(word.toLowerCase());
    }
    else
    {
      return string.trim().toLowerCase().startsWith(word.toLowerCase());
    }
  }

  private String filterLineNumber(String string)
  {
    if (string.startsWith(LINE_START_MARK))
    {
      return string.substring(string.indexOf(LINE_END_MARK) + LINE_END_MARK.length());
    }
    else
    {
      return string;
    }
  }

  private boolean canFindWebComponent(String componentName)
  {
    DomElement webComponent = WebUIManager.getInstance().getDomElementFromPool(componentName.trim());

    if (webComponent == null)
    {  // if it is a special case: "-browser" option

      for (String browser : SUPPORTED_BROWSERS)
      {
        webComponent = WebUIManager.getInstance().getDomElementFromPool(componentName.trim() + '-' + browser);

        if (webComponent != null)
        {
          return true;
        }
      }
    }
    else
    {
      return true;
    }

    return false;
  }
}
