package sap.prd.cmintegration.cli;

import java.io.IOException;
import java.io.OutputStreamWriter;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ CommandsTest.class, CreateTransportTest.class, GetChangeStatusTest.class,
    GetChangeTransportDescriptionTest.class, GetChangeTransportModifiableTest.class, GetChangeTransportOwnerTest.class,
    GetChangeTransportsTest.class, ReleaseTransportTest.class, UploadFileToTransportTest.class })
public class LoggingTest
{

  private final static Logger root = Logger.getRootLogger();

  @BeforeClass
  public static void setupClass() throws IOException
  {
    root.setLevel(Level.ALL);
    ConsoleAppender appender = new ConsoleAppender();
    OutputStreamWriter writer = new OutputStreamWriter(System.out);
    appender.setWriter(writer);
    PatternLayout layout = new PatternLayout();
    layout.setConversionPattern("%d %c %C %-5p [%t]: %m%n");
    appender.setLayout(layout);
    root.addAppender(appender);
  }

}
