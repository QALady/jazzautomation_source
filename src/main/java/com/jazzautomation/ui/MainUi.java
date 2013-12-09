package com.jazzautomation.ui;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.util.StatusPrinter;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;

import com.jazzautomation.WebUIManager;

import org.apache.commons.lang3.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import static java.awt.Cursor.getPredefinedCursor;
import static java.awt.Toolkit.getDefaultToolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.File;

import javax.swing.*;
import static javax.swing.JFileChooser.APPROVE_OPTION;
import static javax.swing.JFileChooser.FILES_ONLY;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;
// import org.slf4j.Logger;

/** Created by douglas_bullard on 10/2/13. */
@SuppressWarnings("MethodOnlyUsedFromInnerClass")
public class MainUi extends JFrame
{
  public static final String  TITLE_TEXT             = "Jazz Automation v";
  public static final String  VERSION                = "1.0";
  private static final Logger LOG                    = LoggerFactory.getLogger(MainUi.class);
  public static final int     DEFAULT_WIDTH          = 1000;
  public static final int     DEFAULT_HEIGHT         = 700;
  private Settings            settings;
  private JButton             quitButton;
  private JCheckBox           useProxyCheckBox;
  private JTextArea           outputTextArea;
  private Cursor              normalCursor           = getPredefinedCursor(Cursor.DEFAULT_CURSOR);
  private Cursor              waitCursor             = getPredefinedCursor(Cursor.WAIT_CURSOR);
  private Toolkit             toolkit                = getDefaultToolkit();
  private Dimension           screenSize             = toolkit.getScreenSize();
  private JPanel              mainPanel;
  private JTextField          featuresTextField;
  private JComboBox<Browsers> browserComboBox;
  private JButton             goButton;
  private JLabel              configurationPathLabel;
  private JButton             featuresSelectButton;
  private JLabel              reportsPathLabel;
  private JLabel              logPathLabel;
  private JScrollPane         outputScrollPane;

  /** Sets the look and feel. */
  @SuppressWarnings("UseOfSystemOutOrSystemErr")
  public static LookAndFeel setLookAndFeel(String feelName, Component component)
  {
    LookAndFeel currentLAF = UIManager.getLookAndFeel();

    try
    {
      UIManager.setLookAndFeel(feelName);
      SwingUtilities.updateComponentTreeUI(component);
    }
    catch (Exception e)
    {
      System.out.println("Error setting native LAF: " + feelName + e.getMessage());
    }

    return currentLAF;
  }

  public void setNormalCursor()
  {
    setCursor(normalCursor);
  }

  public MainUi()
  {
    LOG.error("Setting log stuff now!");

    LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();

    StatusPrinter.print(lc);
    settings = new Settings();
    initializeComponents();
    addListeners();
    setInitialSettings();
    JFrame.setDefaultLookAndFeelDecorated(false);
    setTitle(TITLE_TEXT + VERSION);
    setVisible(true);
  }

  /** Configure the UI components after creation. */
  private void initializeComponents()
  {
    setTitle("Jazz Automation v1.0");
    setContentPane(mainPanel);
    setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel", this);

    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

    // initial size of 1000 x 700, or full screen, whichever is smaller
    int maxWidth  = (int) Math.min(DEFAULT_WIDTH, screenSize.getWidth());
    int maxHeight = (int) Math.min(DEFAULT_HEIGHT, screenSize.getHeight());

    setSize(new Dimension(maxWidth, maxHeight));
    browserComboBox.setModel(new DefaultComboBoxModel<>(Browsers.values()));
    centerApp(this);
    setVisible(true);
  }

  /** Center the app in the window. */
  public static void centerApp(MainUi ui)
  {
    if (ui != null)
    {
      Toolkit   defaultToolkit = getDefaultToolkit();
      Dimension screenSize     = defaultToolkit.getScreenSize();
      int       x              = (int) ((screenSize.getWidth() - ui.getWidth()) / 2);
      int       y              = (int) ((screenSize.getHeight() - ui.getHeight()) / 2);

      ui.setBounds(x, y, ui.getWidth(), ui.getHeight());
    }
  }

  /** add any action listenders to the components. */
  private void addListeners()
  {
    quitButton.addActionListener(new ActionListener()
      {
        @Override public void actionPerformed(ActionEvent e)
        {
          handleQuitButtonAction();
        }
      });
    useProxyCheckBox.addActionListener(new ActionListener()
      {
        @Override public void actionPerformed(ActionEvent e)
        {
          // do something...
        }
      });
    goButton.addActionListener(new ActionListener()
      {
        @Override public void actionPerformed(ActionEvent e)
        {
          handleGoButtonAction();
        }
      });
    featuresSelectButton.addActionListener(new ActionListener()
      {
        @Override public void actionPerformed(ActionEvent e)
        {
          handleSelectFeaturesButtonAction();
        }
      });
  }

  /** Save any settings and then quit. */
  private void handleQuitButtonAction()
  {
    saveSettings();
    System.exit(0);
  }

  private void saveSettings()
  {
    settings.setBrowser((Browsers) browserComboBox.getSelectedItem());
    settings.setFeatures(featuresTextField.getText());
    settings.setConfigurationsPath(configurationPathLabel.getText());
    settings.setReportsPath(reportsPathLabel.getText());
    settings.setLogsPath(logPathLabel.getText());
    settings.save();
  }

  /** They've selected GO - run the driver, then show the report when finished. */
  private void handleGoButtonAction()
  {
    saveSettings();
    settings.setSystemProperties();  // set the system settings from the stored/modified preferences

    // ProgressUpdater progressUpdater = new ProgressUpdater(outputTextArea, settings.getLogsPath());
    ProgressTailer progressUpdater = new ProgressTailer(outputTextArea, settings.getLogsPath());

    // do something when the "Go" button is clicked
    try
    {
      setCursor(waitCursor);
      WebUIManager.getInstance().setBrowser(settings.getBrowser().name());

      UiBackgroundTaskManager uiBackgroundTaskManager = new UiBackgroundTaskManager(progressUpdater, settings, this);

      uiBackgroundTaskManager.execute();  // fire off the driver
      progressUpdater.execute();          // run the updater in the background'
    }
    catch (Exception e1)
    {
      LOG.error("Unexpected exception", e1);

      String message = e1.getMessage();

      showMessageDialog(this, message, "Error running Jazz Automation", ERROR_MESSAGE);
    }
  }

  /** After they've selected the features, set the paths in the UI and save. */
  private void handleSelectFeaturesButtonAction()
  {
    getFilePaths(settings.getConfigurationsPath(), configurationPathLabel, featuresTextField);
    saveSettings();
  }

  /** Get the list of features and use it to set the text fields. */
  private void getFilePaths(String path, JLabel dirLabel, JTextField textField)
  {
    // show a file requester dialog and get path from that
    JFileChooser fileChooser = new JFileChooser();

    if (path != null)
    {
      fileChooser.setCurrentDirectory(new File(path, "features"));
    }

    fileChooser.setFileSelectionMode(FILES_ONLY);
    fileChooser.setMultiSelectionEnabled(true);

    int returnVal = fileChooser.showOpenDialog(this);

    if (returnVal == APPROVE_OPTION)
    {
      File[] files = fileChooser.getSelectedFiles();

      if (files.length > 0)
      {
        File          parentFile    = files[0].getParentFile().getParentFile();
        String        dir           = parentFile.getAbsolutePath();
        StringBuilder stringBuilder = new StringBuilder();

        for (File file : files)
        {
          String fileName = StringUtils.substringBefore(file.getName(), ".");  // just get the filename without the extension

          stringBuilder.append(fileName).append(',');
        }

        String results = stringBuilder.toString();

        results = StringUtils.removeEnd(results, ",");
        dirLabel.setText(dir);

        File reportsDir = new File(parentFile, "reports");

        reportsDir.mkdirs();
        reportsPathLabel.setText(reportsDir.getAbsolutePath());
        textField.setText(results);
        settings.setConfigurationsPath(configurationPathLabel.getText());
      }
    }
  }

  /** set any checkboxes, etc., from the settings. */
  private void setInitialSettings()
  {
    featuresTextField.setText(settings.getFeatures());
    configurationPathLabel.setText(settings.getConfigurationsPath());
    reportsPathLabel.setText(settings.getReportsPath());
    browserComboBox.setSelectedItem(settings.getBrowser());
    logPathLabel.setText(settings.getLogsPath());
  }
  // --------------------------- main() method ---------------------------

  /** @param  args  the command line arguments */
  public static void main(String... args)
  {
    try
    {
      System.out.println("com.jazzautomation.AutomationDriver.main...............................");

      MainUi ui = new MainUi();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  {
    // GUI initializer generated by IntelliJ IDEA GUI Designer
    // >>> IMPORTANT!! <<<
    // DO NOT EDIT OR ADD ANY CODE HERE!
    $$$setupUI$$$();
  }

  /**
   * Method generated by IntelliJ IDEA GUI Designer >>> IMPORTANT!! <<< DO NOT edit this method OR call it in your code!
   *
   * @noinspection  ALL
   */
  private void $$$setupUI$$$()
  {
    mainPanel = new JPanel();
    mainPanel.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));

    final JLabel label1 = new JLabel();

    label1.setFont(new Font("Arial", Font.BOLD, 36));
    label1.setText("Jazz Automation");
    mainPanel.add(label1,
                  new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED,
                                      GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

    final JPanel panel1 = new JPanel();

    panel1.setLayout(new GridLayoutManager(6, 5, new Insets(0, 0, 0, 0), -1, -1));
    mainPanel.add(panel1,
                  new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                      GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                      GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
    featuresTextField = new JTextField();
    panel1.add(featuresTextField,
               new GridConstraints(1, 2, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW,
                                   GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));

    final JLabel label2 = new JLabel();

    label2.setText("Reports Path:");
    panel1.add(label2,
               new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED,
                                   GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

    final JLabel label3 = new JLabel();

    label3.setText("Browser:");
    panel1.add(label3,
               new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED,
                                   GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
    browserComboBox = new JComboBox();
    panel1.add(browserComboBox,
               new GridConstraints(5, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_FIXED,
                                   GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

    final JLabel label4 = new JLabel();

    label4.setText("Log path:");
    panel1.add(label4,
               new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED,
                                   GridConstraints.SIZEPOLICY_FIXED, null, null, null, 1, false));
    configurationPathLabel = new JLabel();
    configurationPathLabel.setText("");
    panel1.add(configurationPathLabel,
               new GridConstraints(2, 1, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_WANT_GROW,
                                   GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    featuresSelectButton = new JButton();
    featuresSelectButton.setText("Select features:");
    panel1.add(featuresSelectButton,
               new GridConstraints(1, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                                   GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
                                   null, null, null, 0, false));
    reportsPathLabel = new JLabel();
    reportsPathLabel.setText("");
    panel1.add(reportsPathLabel,
               new GridConstraints(3, 1, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_WANT_GROW,
                                   GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    logPathLabel = new JLabel();
    logPathLabel.setText("Label");
    panel1.add(logPathLabel,
               new GridConstraints(4, 1, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_WANT_GROW,
                                   GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));

    final Spacer spacer1 = new Spacer();

    panel1.add(spacer1,
               new GridConstraints(5, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW,
                                   1, null, null, null, 0, false));

    final JLabel label5 = new JLabel();

    label5.setText("Configuration Dir:");
    panel1.add(label5,
               new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED,
                                   GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    goButton = new JButton();
    goButton.setText("Go");
    panel1.add(goButton,
               new GridConstraints(1, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                                   GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
                                   null, null, null, 0, false));
    quitButton = new JButton();
    quitButton.setText("Quit");
    panel1.add(quitButton,
               new GridConstraints(5, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL,
                                   GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
                                   null, null, null, 0, false));
    useProxyCheckBox = new JCheckBox();
    useProxyCheckBox.setEnabled(false);
    useProxyCheckBox.setText("Use Proxy");
    panel1.add(useProxyCheckBox,
               new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
                                   GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED,
                                   null, null, null, 0, false));
    outputScrollPane = new JScrollPane();
    outputScrollPane.setVerticalScrollBarPolicy(22);
    mainPanel.add(outputScrollPane,
                  new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
                                      GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW,
                                      GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null,
                                      new Dimension(1000, -1), 0, false));
    outputTextArea = new JTextArea();
    outputTextArea.setEditable(false);
    outputTextArea.setFont(new Font("Courier New", Font.PLAIN, outputTextArea.getFont().getSize()));
    outputScrollPane.setViewportView(outputTextArea);
  }

  /** @noinspection  ALL */
  public JComponent $$$getRootComponent$$$()
  {
    return mainPanel;
  }
}
