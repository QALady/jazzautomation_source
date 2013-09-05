package com.jazzautomation.cucumber;

public class Background extends CucumberBase
{
  private Given given;

  public Given getGiven()
  {
    return given;
  }

  public void setGiven(Given given)
  {
    this.given = given;
  }
}
