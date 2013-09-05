package com.jazzautomation.report;

import com.jazzautomation.page.DomElementExpect;

public class ExpectResult extends ResultBase
{
  private DomElementExpect componentExpect;

  public DomElementExpect getComponentExpect()
  {
    return componentExpect;
  }

  public void setComponentExpect(DomElementExpect componentExpect)
  {
    this.componentExpect = componentExpect;
  }
}
