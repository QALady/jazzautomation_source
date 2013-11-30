package com.jazzautomation.ui;

import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListener;
import org.apache.commons.io.input.TailerListenerAdapter;
import org.apache.commons.lang3.StringUtils;

import java.io.File;

import java.util.ArrayDeque;
import java.util.Queue;

import javax.swing.*;

/**
 * Runner for the log tailer - has to be done in the background so the UI is still responsive.
 *
 * <p>Yeah, it's threaded - let the JVM do the heavy lifting.</p>
 */
public class ProgressTailer extends SwingWorker<Object, Object>
{
  public static final int PIXELS_PER_ROW = 18;
  public static final int DELAY_MILLIS   = 250;
  private JTextArea       outputTextArea;
  private String          logsPath;
  private Tailer          tailer;
  private int             maxSize;
  private Queue<String>   queue          = new ArrayDeque<>(25);  // initial size

  public ProgressTailer(JTextArea outputTextArea, String logsPath)
  {
    this.outputTextArea = outputTextArea;
    this.logsPath       = logsPath;
  }

  /**
   * We're in a loop, constantly getting the log. We remove any lines previously there before we started, then paste what's left into the text area.
   */
  @Override protected Object doInBackground() throws Exception
  {
    File           logFile  = new File(logsPath);
    TailerListener listener = new MyTailerListener();

    tailer = Tailer.create(logFile, listener, DELAY_MILLIS, true);

    return null;
  }

  public void stop()
  {
    tailer.stop();
  }

  class MyTailerListener extends TailerListenerAdapter
  {
    public void handle(String line)
    {
      maxSize = outputTextArea.getSize().height / PIXELS_PER_ROW;  // UI size could have changed

      // For now, never pop the queue - tail the entire new part of the log

      // while (queue.size() >= maxSize)
      // {
      // System.out.println("queue.size() [" + queue.size() + ']' + " >= maxSize[" + maxSize + ']');
      // queue.remove();
      // }
      queue.add(line);
      populateTextAreaFromQueue();
    }

    private void populateTextAreaFromQueue()
    {
      StringBuilder sb = new StringBuilder(queue.size());

      for (String line : queue)
      {
        sb.append(line).append('\n');
      }

      String text = sb.toString();

      text = StringUtils.removeEnd(text, "\n");
      outputTextArea.setText(text);
    }
  }
}
