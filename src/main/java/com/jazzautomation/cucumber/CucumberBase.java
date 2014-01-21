package com.jazzautomation.cucumber;

import com.jazzautomation.cucumber.parser.IllegalCucumberFormatException;

import org.codehaus.jackson.annotate.JsonIgnore;

public abstract class CucumberBase
{
  private String               description;
  @JsonIgnore
  private String               text;
  @JsonIgnore
  private String[]             endWords    = {};
  @JsonIgnore
  private String               leadingWord;

  protected CucumberBase() {}

  public String[] getEndWords()
  {
    return endWords;
  }

  void setEndWords(String... endWords)
  {
    this.endWords = endWords;
  }

  public String getDescription()
  {
    return description;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }

  public String getText()
  {
    return text;
  }

  public void setText(String text)
  {
    this.text = text;
  }

  public String toString()
  {
    return text;
  }

  public String getLeadingWord()
  {
    return leadingWord;
  }

  public void setLeadingWords(String leadingWord)
  {
    this.leadingWord = leadingWord;
  }

  public abstract void process() throws IllegalCucumberFormatException;
}
