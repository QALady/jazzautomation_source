package com.jazzautomation.cucumber;

import static com.jazzautomation.cucumber.CucumberConstants.BACKGROUND;
import static com.jazzautomation.cucumber.CucumberConstants.COLON;
import static com.jazzautomation.cucumber.CucumberConstants.GIVEN;
import static com.jazzautomation.cucumber.CucumberConstants.SCENARIO;
import com.jazzautomation.cucumber.parser.FeatureParser;

import java.util.List;

public class Background extends CucumberBase
{
  private Given given;

  public Background()
  {
    setLeadingWords(BACKGROUND);
    setEndWords(SCENARIO);
  }

  public void process() throws IllegalCucumberFormatException
  {
    List<String> lines               = FeatureParser.scanIntoLines(getText());
    String[]     descriptionEndWords = { GIVEN };
    int          index               = FeatureParser.setUpDescription(this, lines, SCENARIO, COLON, descriptionEndWords);
    String       line                = lines.get(index);

    // everything here is for "given"
    if (FeatureParser.isStartWithAWordAfterLineNumber(line, GIVEN))
    {
      given = new Given();
      given.setText(FeatureParser.normalizeToString(lines, index));
      given.setForBackground(true);
      given.process();
    }
  }

  // --------------------- GETTER / SETTER METHODS ---------------------
  public Given getGiven()
  {
    return given;
  }

  public void setGiven(Given given)
  {
    this.given = given;
  }
}
