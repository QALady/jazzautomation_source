package com.jazzautomation.cucumber.parser;

import com.jazzautomation.WebUIManager;

import com.jazzautomation.action.ComponentAction;
import com.jazzautomation.action.HtmlAction;

import com.jazzautomation.cucumber.And;
import com.jazzautomation.cucumber.Background;
import com.jazzautomation.cucumber.CucumberBase;
import static com.jazzautomation.cucumber.CucumberConstants.COLON;
import static com.jazzautomation.cucumber.CucumberConstants.ESCAPE_CHAR;
import static com.jazzautomation.cucumber.CucumberConstants.FEATURE;
import static com.jazzautomation.cucumber.CucumberConstants.LINE_END_MARK;
import static com.jazzautomation.cucumber.CucumberConstants.LINE_START_MARK;
import static com.jazzautomation.cucumber.CucumberConstants.OPTIONAL;
import static com.jazzautomation.cucumber.CucumberConstants.TABLE_COLUMN_SEPARATOR;
import static com.jazzautomation.cucumber.CucumberConstants.TABLE_COLUMN_SEPARATOR_CHAR;
import com.jazzautomation.cucumber.Feature;
import com.jazzautomation.cucumber.Scenario;

import com.jazzautomation.page.DomElement;

import static com.jazzautomation.util.Constants.SUPPORTED_BROWSERS;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringAfterLast;
import static org.apache.commons.lang3.StringUtils.substringBeforeLast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A singleton class to parse a Cucumber file at a time. The only public method is parse - take FileInputStream to parse through and return a Feature
 * object. In case of ill-formats, throws IllegalCucumberFormatException.
 */
@SuppressWarnings({ "AssignmentToMethodParameter", "MethodMayBeStatic" })
public class FeatureParser
{
  private static final String  COMMENT_MARKER = "#";
  private static Logger        LOG            = LoggerFactory.getLogger(FeatureParser.class);
  private static FeatureParser instance;

  /** Standard singleton implementation. */
  public static FeatureParser getInstance()
  {
    if (instance == null)
    {
      instance = new FeatureParser();
    }

    return instance;
  }

  public static boolean isTaskOptional(String description)
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

  public static Map<String, String> processMap(String text) throws IllegalCucumberFormatException
  {
    Map<String, String> keyValuePair = new LinkedHashMap<>();
    List<String>        lines        = scanIntoLines(text);

    // every line is a key value pair
    for (String line : lines)
    {
      line = filterLineNumber(line);

      if (isTableStructureValid(line))
      {
        String innerString = substringBeforeLast(line, TABLE_COLUMN_SEPARATOR);
        String value       = substringAfterLast(innerString, TABLE_COLUMN_SEPARATOR);
        String key         = substringBeforeLast(innerString, TABLE_COLUMN_SEPARATOR);

        key = substringAfter(key, TABLE_COLUMN_SEPARATOR);
        keyValuePair.put(key.trim(), value.trim());
      }
      else
      {
        if (isBlank(line))
        {
          continue;
        }

        if (line.trim().startsWith(COMMENT_MARKER))
        {
          LOG.info("Skipping line [" + line.trim() + ']');

          continue;
        }

        throw new IllegalCucumberFormatException("The following line could not be processed; it was invalid. Line = [" + line + ']');
      }
    }

    return keyValuePair;
  }

  public static List<String> scanIntoLines(String line)
  {
    List<String> list = new ArrayList<>();

    list.addAll(Arrays.asList(line.split("\n")));

    return list;
  }

  private static String filterLineNumber(String text)
  {
    if (text.startsWith(LINE_START_MARK))
    {
      return substringAfter(text, LINE_END_MARK);
    }
    else
    {
      return text;
    }
  }

  // jsheridan CODEREVIEW - None of these have any documentation
  private static boolean isTableStructureValid(String text)
  {
    // we should have three individual table separators
    int count = 0;

    for (int i = 0; i < text.length(); i++)
    {
      if (text.charAt(i) == TABLE_COLUMN_SEPARATOR_CHAR)
      {
        if (i == 0)
        {
          count++;
        }
        else if (text.charAt(i - 1) != ESCAPE_CHAR)
        {
          count++;
        }
      }
    }

    return count == 3;
  }

  public static void parseHoverAndClick(And and, String[] words, ComponentAction componentAction) throws IllegalCucumberFormatException
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

  public static List<String> retrieveSpecialWords(String... words)
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

  public static boolean canFindWebComponent(String componentName)
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

  public static void parseWait(And and, String[] words, ComponentAction componentAction)
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

  public static int setUpDescription(CucumberBase cucumber, List<String> lines, String leadingWord, String followingWord, String... endWords)
  {
    StringBuilder descBuffer = new StringBuilder();

    descBuffer.append(filterWords(lines.get(0), leadingWord, followingWord)).append('\n');

    int index = retrieveDescription(lines, descBuffer, endWords);

    cucumber.setDescription(descBuffer.toString());

    return index;
  }

  public static int retrieveDescription(List<String> lines, StringBuilder descBuffer, String... descriptionEndWords)
  {
    int index = 1;

    while (index < lines.size())
    {
      String aline = filterLineNumber(lines.get(index));

      if (isAtEnd(aline, descriptionEndWords))
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

  public static String normalizeToString(List<String> lines, int index)
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

  public static String filterWords(String text, String leadingWord, String followingWord)
  {
    String filteredString = filterLineNumber(text).trim();

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

  /** private constructor. */
  private FeatureParser() {}

  /**
   * parse a file return a Feature object. Throw IllegalCucumberFormatException and stop if there is formatting errors
   *
   * @param  originalLines
   */
  public Feature parse(List<String> originalLines) throws IllegalCucumberFormatException
  {
    Feature       feature            = new Feature();
    StringBuilder originalTextBuffer = new StringBuilder();
    List<String>  stringsForFile     = processRawLines(originalLines, originalTextBuffer, 1);

    feature.setOriginalText(originalTextBuffer.toString());

    int index = setUpDescription(feature, stringsForFile);

    index = setUpBackground(feature, stringsForFile, index);
    setUpScenarios(feature, stringsForFile, index);
    LOG.info("Successfully parsed feature: " + feature.getOriginalText());

    return feature;
  }

  private List<String> processRawLines(List<String> originalLines, StringBuilder originalTextBuffer, int lineNum)
  {
    List<String> stringsForFile = new ArrayList<>();

    for (String line : originalLines)
    {
      String trimmedLine = line.trim();

      originalTextBuffer.append(line).append('\n');

      String formattedLine = LINE_START_MARK + (lineNum++) + LINE_END_MARK + trimmedLine;

      if (isNotEmpty(trimmedLine) && trimmedLine.startsWith(COMMENT_MARKER))
      {  // skip adding comment lines to the executable feature.
        stringsForFile.add("\n");
      }

      stringsForFile.add(formattedLine);
    }

    // add an extra line at the end of file.
    stringsForFile.add("\n");

    return stringsForFile;
  }

  private int setUpDescription(Feature feature, List<String> stringsForFile)
  {
    // setup feature description - background is optional
    int           index      = setUpText(feature, stringsForFile, 0);
    List<String>  lines      = scanIntoLines(feature.getText());
    StringBuilder descBuffer = new StringBuilder();

    descBuffer.append(filterWords(lines.get(0), FEATURE, COLON)).append('\n');

    String[] descriptionEndWords = {};

    retrieveDescription(lines, descBuffer, descriptionEndWords);
    feature.setDescription(descBuffer.toString());

    return index;
  }

  private int setUpBackground(Feature feature, List<String> stringsForFile, int index) throws IllegalCucumberFormatException
  {
    // setup background
    Background background = new Background();

    index = setUpText(background, stringsForFile, index);

    if (background.getText() != null)
    {
      background.process();
      feature.setBackground(background);
    }

    return index;
  }

  private void setUpScenarios(Feature feature, List<String> stringsForFile, int index) throws IllegalCucumberFormatException
  {
    // loop through scenarios and add to list
    // setup scenarios
    while (index < stringsForFile.size())
    {
      Scenario scenario = new Scenario();

      index = setUpText(scenario, stringsForFile, index);
      scenario.process();
      feature.addScenario(scenario);
    }
  }

  // jsheridan CODEREVIEW - unit test would be nice.  What does this do?
  public static int setUpText(CucumberBase cucumberObject, List<String> stringsForFile, int index)
  {
    String   currentLine = stringsForFile.get(index);
    String   leadingWord = cucumberObject.getLeadingWord();
    String[] endWords    = cucumberObject.getEndWords();

    if (isStartWithAWordAfterLineNumber(currentLine, leadingWord))
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

        if (isAtEnd(nextLine, endWords))
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

  public static boolean isStartWithAWordAfterLineNumber(String text, String word)
  {
    if (text.startsWith(LINE_START_MARK))
    {
      String filteredString = filterLineNumber(text);

      return filteredString.trim().toLowerCase().startsWith(word.toLowerCase());
    }
    else
    {
      return text.trim().toLowerCase().startsWith(word.toLowerCase());
    }
  }

  private static boolean isAtEnd(String text, String... endWords)
  {
    for (String word : endWords)
    {
      if (isStartWithAWordAfterLineNumber(text, word))
      {
        return true;
      }
    }

    return false;
  }
}
