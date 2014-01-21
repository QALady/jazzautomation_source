package com.jazzautomation.ui;

import com.jazzautomation.AutomationDriver;
import com.jazzautomation.WebUIManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import javax.swing.SwingWorker;

/**
 * Runner for the driver - has to be done in the background so the UI is still responsive.
 *
 * <p>Yeah, it's threaded - let the JVM do the heavy lifting.</p>
 */
public class UiBackgroundTaskManager extends SwingWorker<Object, Object>
{
  private final ProgressTailer progressUpdater;
  private final Settings       settings;
  private MainUi               mainUi;
  private static final Logger  LOG = LoggerFactory.getLogger(UiBackgroundTaskManager.class);

  public UiBackgroundTaskManager(ProgressTailer progressUpdater, Settings settings, MainUi mainUi)
  {
    this.progressUpdater = progressUpdater;
    this.settings        = settings;
    this.mainUi          = mainUi;
  }

  @Override
  protected Object doInBackground() throws Exception
  {
    try
    {
      WebUIManager.reinitialize();

      Browsers browser = settings.getBrowser();

      if (browser != Browsers.NOT_SPECIFIED)
      {
        WebUIManager.getInstance().setBrowser(browser, true);  // override anything read in from feature files
      }

      AutomationDriver.beginTestSuite();
    }
    catch (Exception e)
    {
      LOG.error(e.getMessage(), e);
    }

    progressUpdater.stop();
    mainUi.setNormalCursor();
    Os.getOs().openFile(new File(settings.getConfigurationsPath() + "/reports/index.html"));

    return null;
  }
}
