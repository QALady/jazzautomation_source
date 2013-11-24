package com.jazzautomation.ui.javafx;

import com.jazzautomation.ui.Os;
import com.jazzautomation.ui.Settings;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;

import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import javafx.stage.Stage;

import java.net.URL;

import java.util.ResourceBundle;

/**
 * JavaFX front-end - will implement later.
 *
 * <p>Created by douglas_bullard on 10/2/13.</p>
 */
public class MainController implements Initializable
{
  @FXML private Button        quitButton;
  @FXML private Button        selectProjectConfigFileButton;
  @FXML private TextField     currentDirField;
  @FXML private CheckBox      useHttpProxyCheckbox;
  @FXML private CheckBox      useHttpProxyAuthenticationCheckbox;
  @FXML private TextField     proxyServerNameField;
  @FXML private TextField     proxyServerPortField;
  @FXML private TextField     proxyUserNameField;
  @FXML private PasswordField proxyPasswordField;
  @FXML private GridPane      proxyServerPane;
  @FXML private HBox          proxyBox;
  @FXML private VBox          userBox;
  private Os                  os          = Os.getOs();
  private Settings            preferences;
  // ------------------------ INTERFACE METHODS ------------------------

  // --------------------- Interface Initializable ---------------------
  @Override public void initialize(URL url, ResourceBundle resourceBundle)
  {
    System.out.println("com.jazzautomation.ui.MainController.initialize");
    preferences = new Settings();
    initializeUiFromSettings();
    setDefaultButton();
  }

  // -------------------------- OTHER METHODS --------------------------
  private void initializeUiFromSettings()
  {
    if (preferences != null)
    {
      setCheckboxFromSettings(useHttpProxyCheckbox, preferences.shouldUseProxy());
      setCheckboxFromSettings(useHttpProxyAuthenticationCheckbox, preferences.shouldUseProxyAuthentication());
      proxyServerNameField.setText(preferences.getProxyServerName());  // these override the helpful text if not empty or null
      proxyServerPortField.setText(preferences.getProxyServerPort() + "");
      proxyUserNameField.setText(preferences.getProxyUserName());
    }
  }

  /** Helper method to deal with null values upon initialization. */
  private void setCheckboxFromSettings(CheckBox checkbox, boolean value)
  {
    if (checkbox != null)
    {
      checkbox.setSelected(value);
    }
  }

  public void quitClickedAction()
  {
    saveSettings();
    System.exit(0);
  }

  @FXML private void saveSettings() {}

  private void setDefaultButton()
  {
    quitButton.setDefaultButton(true);
  }

  public void start(Stage stage) throws Exception
  {
    // if (areAllNotNull(preferences, watchFilesCheckbox, deleteDotFilesCheckbox, groupByFilesCheckbox, shouldIncludeImportedFilesCheckbox,
    // useHttpProxyAuthenticationCheckbox, useHttpProxyCheckbox, proxyServerNameField, proxyServerPortField, proxyUserNameField,
    // proxyPasswordField, tabPane, justUseCompileConfigCheckbox, showGradleTaskDependenciesCheckbox))
    {
      preferences.setUseHttpProxy(useHttpProxyCheckbox.isSelected());
      preferences.setUseProxyAuthentication(useHttpProxyAuthenticationCheckbox.isSelected());
      preferences.setProxyServerName(proxyServerNameField.getText());

      String text = proxyServerPortField.getText();

      try
      {
        preferences.setProxyServerPort(Integer.parseInt(text));
      }
      catch (NumberFormatException e)
      {
        System.out.println("GradleVisualizerUiController.saveSettings - couldn't parse a port number out of \"" + text + '"');
      }

      preferences.setProxyUserName(proxyUserNameField.getText());

      // preferences.setProxyPassword(proxyPasswordField.getText());
    }
  }

  public void useHttpAuthenticationBoxClicked()
  {
    System.out.println("GradleVisualizerUiController.useHttpAuthenticationBoxClicked");
    userBox.setVisible(useHttpProxyAuthenticationCheckbox.isSelected());
    saveSettings();
  }

  public void useHttpProxyBoxClicked()
  {
    System.out.println("GradleVisualizerUiController.useHttpProxyBoxClicked");

    boolean selected = useHttpProxyCheckbox.isSelected();

    proxyBox.setVisible(selected);
    useHttpProxyAuthenticationCheckbox.setVisible(selected);

    if (!selected)
    {
      setCheckboxFromSettings(useHttpProxyAuthenticationCheckbox, false);
      userBox.setVisible(false);
    }

    saveSettings();
  }
}
