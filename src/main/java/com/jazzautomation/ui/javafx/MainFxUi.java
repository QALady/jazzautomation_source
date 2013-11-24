package com.jazzautomation.ui.javafx;

import javafx.application.Application;

import static javafx.fxml.FXMLLoader.load;

import javafx.scene.Scene;

import javafx.scene.layout.AnchorPane;

import javafx.stage.Stage;

import java.net.URL;

import static java.util.logging.Level.SEVERE;
import static java.util.logging.Logger.getLogger;

/** Created by douglas_bullard on 11/24/13. */
public class MainFxUi extends Application
{
  /** @param  args  the command line arguments */
  public static void main(String... args)
  {
    try
    {
      launch(MainFxUi.class, (String[]) null);
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  @Override public void start(Stage primaryStage)
  {
    try
    {  // UI related stuff

      URL        resource = MainFxUi.class.getResource("MainController.fxml");
      AnchorPane page     = load(resource);
      Scene      scene    = new Scene(page);

      primaryStage.setScene(scene);
      primaryStage.setTitle("Jazz Automation");
      primaryStage.show();
    }
    catch (Exception ex)
    {
      getLogger(MainFxUi.class.getName()).log(SEVERE, null, ex);
    }
  }
}
