package com.jazzautomation.ui;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.io.IOException;

import javax.swing.*;

/**
 * Runner for the log tailer - has to be done in the background so the UI is still responsive.
 *
 * <p>Yeah, it's threaded - let the JVM do the heavy lifting.</p>
 */
public class ProgressUpdater extends SwingWorker<Object, Object>
{
  public static final int PIXELS_PER_ROW = 15;
  private JTextArea       outputTextArea;
  private String          logsPath;
  private boolean         keepRunning    = true;

  public ProgressUpdater(JTextArea outputTextArea, String logsPath)
  {
    this.outputTextArea = outputTextArea;
    this.logsPath       = logsPath;
  }

  /**
   * We're in a loop, constantly getting the log. We remove any lines previously there before we started, then paste what's left into the text area.
   *
   * <p>Not terribly performant, but it'll do for now.</p>
   */
  @Override protected Object doInBackground() throws Exception
  {
    File     logFile        = new File(logsPath);
    Object[] originalLines  = FileUtils.readLines(logFile).toArray();
    int      originalLength = originalLines.length;

    while (keepRunning)
    {
      try
      {
        if (logFile.exists())
        {
          Object[]      strings  = FileUtils.readLines(logFile).toArray();
          Object[]      newLines = ArrayUtils.subarray(strings, originalLength, strings.length - 1);
          StringBuilder sb       = new StringBuilder();

          for (Object newLine : newLines)
          {
            sb.append(newLine).append('\n');
          }

          String text = sb.toString();

          outputTextArea.setText(text);
        }
      }
      catch (IOException e)
      {
        e.printStackTrace();  // might have an issue, at least log it
      }

      Thread.sleep(500);
    }

    return null;
  }

  public void stop()
  {
    keepRunning = false;
  }
}
