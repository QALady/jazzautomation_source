package com.jazzautomation.cucumber;

import java.util.ArrayList;
import java.util.List;

public class Scenario extends CucumberBase
{
  private Given     given;
  private List<And> ands     = new ArrayList<>();
  private Then      then;
  private boolean   optional;

  public Given getGiven()
  {
    return given;
  }

  public void setGiven(Given given)
  {
    this.given = given;
  }

  public List<And> getAnds()
  {
    return ands;
  }

  public void addAnd(And and)
  {
    ands.add(and);
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
