package com.jazzautomation.cucumber;

import com.jazzautomation.page.DomElementExpectation;
import java.util.ArrayList;
import java.util.List;
import org.codehaus.jackson.annotate.JsonIgnore;

import com.jazzautomation.page.Page;

public class Then extends CucumberBase
{
  @JsonIgnore
  private Page                  pageExpected;
  private boolean                  forExpects;
  private List<DomElementExpectation> expects = new ArrayList<>();

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

  public List<DomElementExpectation> getExpects()
  {
    return expects;
  }

  public void addExpect(DomElementExpectation expect)
  {
    expects.add(expect);
  }
}
