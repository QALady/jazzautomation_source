package com.jazzautomation.report;

import com.jazzautomation.page.DomElementExpectation;

public class ExpectationResult extends ResultBase
{
  private DomElementExpectation componentExpect;

  public DomElementExpectation getComponentExpect()
  {
    return componentExpect;
  }

  public void setComponentExpect(DomElementExpectation componentExpect)
  {
    this.componentExpect = componentExpect;
  }
}
