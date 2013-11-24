package com.jazzautomation.ui;

import com.jazzautomation.AutomationDriver;

import java.io.File;

import javax.swing.*;

/**
 * Runner for the driver - has to be done in the background so the UI is still responsive.
 *
 * <p>Yeah, it's threaded - let the JVM do the heavy lifting.</p>
 */
public class UiBackgroundTaskManager extends SwingWorker<Object, Object>
{
  private final ProgressUpdater progressUpdater;
  private final Settings        settings;
  private MainUi                mainUi;

  public UiBackgroundTaskManager(ProgressUpdater progressUpdater, Settings settings, MainUi mainUi)
  {
    this.progressUpdater = progressUpdater;
    this.settings        = settings;
    this.mainUi          = mainUi;
  }

  @Override protected Object doInBackground() throws Exception
  {
    AutomationDriver.beginTestSuite();
    progressUpdater.stop();
    mainUi.setNormalCursor();
    Os.getOs().openFile(new File(settings.getConfigurationsPath() + "/reports/index.html"));

    return null;
  }
}
