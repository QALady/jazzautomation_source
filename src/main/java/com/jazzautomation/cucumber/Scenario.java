package com.jazzautomation.cucumber;

import com.jazzautomation.cucumber.parser.FeatureParser;
import com.jazzautomation.cucumber.parser.IllegalCucumberFormatException;

import java.util.ArrayList;
import java.util.List;

import static com.jazzautomation.cucumber.CucumberConstants.*;

public class Scenario extends CucumberBase
{
  private Given     given;
  private List<And> ands     = new ArrayList<>();
  private Then      then;
  private boolean   optional;

  public Scenario()
  {
    setLeadingWords(SCENARIO);
    setEndWords(SCENARIO);
  }

  public void process() throws IllegalCucumberFormatException
  {
    List<String> lines               = FeatureParser.scanIntoLines(getText());
    String[]     descriptionEndWords = { GIVEN };
    int          index               = FeatureParser.setUpDescription(this, lines, SCENARIO, COLON, descriptionEndWords);

    optional = FeatureParser.isTaskOptional(getDescription());

    // handle Given, And and Then
    while (index < lines.size())
    {
      String line = lines.get(index);

      if (FeatureParser.isStartWithAWordAfterLineNumber(line, GIVEN))
      {
        given = new Given();
        index = FeatureParser.setUpText(given, lines, index);
        given.process();
      }
      else if (FeatureParser.isStartWithAWordAfterLineNumber(line, AND))
      {
        And and = new And();

        index = FeatureParser.setUpText(and, lines, index);
        and.process();
        addAnd(and);
      }
      else if (FeatureParser.isStartWithAWordAfterLineNumber(line, THEN))
      {
        then  = new Then();
        index = FeatureParser.setUpText(then, lines, index);
        then.process();
      }
    }
  }

  public void addAnd(And and)
  {
    ands.add(and);
  }

  // --------------------- GETTER / SETTER METHODS ---------------------
  public List<And> getAnds()
  {
    return ands;
  }

  public Given getGiven()
  {
    return given;
  }

  public void setGiven(Given given)
  {
    this.given = given;
  }

  public Then getThen()
  {
    return then;
  }

  public void setThen(Then then)
  {
    this.then = then;
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
