package com.jazzautomation.customaction;

/**
 * Created with IntelliJ IDEA.
 * User: dedrick
 * Date: 11/24/13
 * Time: 6:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class InitializationException extends RuntimeException
{
  public InitializationException(String message)
  {
    super(message);
  }

  public InitializationException(String message, Throwable cause)
  {
    super(message, cause);
  }
}
