package com.jazzautomation;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import com.jazzautomation.report.SuiteResult;
import com.jazzautomation.report.SuiteResultLight;

import static com.jazzautomation.util.Constants.*;

import org.codehaus.jackson.map.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import java.net.URL;

import java.util.ArrayList;
import java.util.List;

/** Responsible for generating the Jazz Automation report. */
@SuppressWarnings({ "StaticMethodOnlyUsedInOneClass", "TypeMayBeWeakened" })
public class ReportGenerator
{
  private static final Logger LOG          = LoggerFactory.getLogger(ReportGenerator.class);
  public static final String  DATE_FORMAT  = "MM_dd_yyyy_'at'_HH_mm";
  private static final String JS_EXTENSION = ".js";
  private static final String UNDER_SCORE  = "_";

  private ReportGenerator() {}

  /**
   * Generates the JazzAutomation report for the provided <code>SuiteResult.</code>
   *
   * @param  suiteResult  the suite results.
   */
  public static void generateReport(SuiteResult suiteResult)
  {
    String reportPath   = WebUIManager.getInstance().getLogsPath();
    File   logsPathFile = new File(reportPath);

    // load data.json if exist
    if (!logsPathFile.exists())
    {
      logsPathFile.mkdir();
    }

    // copy all index templates in place
    copyTemplateFiles(logsPathFile);

    // find data path
    String dataFolderPath = logsPathFile.getAbsolutePath() + File.separator + DATA_FOLDER_NAME;
    File   dataFolder     = new File(dataFolderPath);

    if (!dataFolder.exists())
    {
      dataFolder.mkdir();
    }

    LocalDateTime          now                  = LocalDateTime.now();
    ZonedDateTime          gmtTime              = ZonedDateTime.now(ZoneId.of("GMT"));
    DateTimeFormatter      dateTimeFormatter    = DateTimeFormatter.ofPattern(DATE_FORMAT);
    DateTimeFormatter      gmtDateTimeFormatter = DateTimeFormatter.ofPattern("d MMM yyyy HH:mm:ss 'GMT'");
    String                 reportName           = WebUIManager.getInstance().getProjectName() + UNDER_SCORE + dateTimeFormatter.format(now);
    String                 reportFileName       = dataFolder.getAbsolutePath() + File.separator + reportName + JS_EXTENSION;
    File                   reportJsonFile       = new File(reportFileName);
    ObjectMapper           mapper               = new ObjectMapper();
    String                 jsonString           = null;
    File                   dataJsonFile         = new File(dataFolder.getAbsolutePath() + File.separator + DATA_FOLDER_NAME + JS_EXTENSION);
    List<SuiteResultLight> dataList             = new ArrayList<>();
    String                 dataJsonString       = "";
    SuiteResultLight       suiteLight           = new SuiteResultLight();

    suiteLight.setName(reportName);
    suiteLight.setProject(WebUIManager.getInstance().getProjectName());
    suiteLight.setTimestamp(gmtDateTimeFormatter.format(gmtTime));
    suiteLight.setDuration(suiteResult.getDuration());
    suiteLight.setSuccessRate(suiteResult.getSuccessRate());

    if (dataJsonFile.exists())
    {
      dataList = getSuiteResultFromJsonFile(mapper, dataJsonFile, dataList);
    }

    dataList.add(suiteLight);

    String dataJsonString = "";
    String jsonString     = null;

    try
    {
      jsonString     = REPORT_JS_PRE_JSON + mapper.writeValueAsString(suiteResult);
      dataJsonString = DATA_JS_PRE_JSON + mapper.writeValueAsString(dataList);
    }
    catch (Exception e)
    {
      LOG.error("Error converting suite results or reading the report data.", e);
    }

    // serialize SuiteResult
    try(FileOutputStream reportOutputStream = new FileOutputStream(reportJsonFile);
        FileOutputStream jsonDataOutputFile = new FileOutputStream(dataJsonFile);
       )
    {
      if (jsonString != null)
      {
        reportOutputStream.write(jsonString.getBytes());
        jsonDataOutputFile.write(dataJsonString.getBytes());
      }

      reportOutputStream.flush();
      jsonDataOutputFile.flush();
    }
    catch (Exception e)
    {
      LOG.error("Error serializing the report.", e);
    }
  }

  private static List<SuiteResultLight> getSuiteResultFromJsonFile(ObjectMapper mapper, File dataJsonFile, List<SuiteResultLight> dataList)
  {
    try(FileInputStream fileIn = new FileInputStream(dataJsonFile);
        BufferedReader stdin = new BufferedReader(new InputStreamReader(fileIn)))
    {
      StringBuilder buffer = new StringBuilder();
      String        line;

      while ((line = stdin.readLine()) != null)
      {
        buffer.append(line);
      }

      String stringData = buffer.toString();

      if (stringData.trim().startsWith(DATA_JS_PRE_JSON))
      {
        dataList = mapper.readValue(stringData.trim().substring(DATA_JS_PRE_JSON.length()), ArrayList.class);
      }
    }
    catch (Exception e)
    {
      LOG.error("Error generating report", e);
    }

    return dataList;
  }

  private static void copyTemplateFiles(File logsPathFile)
  {
    // copy all index files
    for (String aFileName : INDEX_FILES)
    {
      copyFile(aFileName, logsPathFile.getAbsolutePath() + File.separator + aFileName);
    }

    // create jslib folder
    String jsLibPath       = logsPathFile.getAbsolutePath() + File.separator + JS_LIB_FOLDER;
    File   jsLibFolderFile = new File(jsLibPath);

    if (!jsLibFolderFile.exists())
    {
      jsLibFolderFile.mkdir();
    }

    // create all jsLibs files
    for (String aJsLibFileName : JS_LIB_FILES)
    {
      copyFile(aJsLibFileName, jsLibFolderFile.getAbsolutePath() + File.separator + aJsLibFileName);
    }
  }

  private static void copyFile(String resourceUrlPath, String reportFilePath)
  {
    File reportFile = new File(reportFilePath);

    try(FileOutputStream outForReport = new FileOutputStream(reportFile))
    {
      URL    fileUrl = Resources.getResource(resourceUrlPath);
      String string  = Resources.toString(fileUrl, Charsets.UTF_8);

      outForReport.write(string.getBytes());
      outForReport.flush();
    }
    catch (IOException ie)
    {
      LOG.error("Error copying report.", ie);
    }
  }
}
