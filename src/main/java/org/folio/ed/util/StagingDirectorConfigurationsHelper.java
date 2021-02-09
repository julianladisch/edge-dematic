package org.folio.ed.util;

import java.util.concurrent.TimeUnit;

public class StagingDirectorConfigurationsHelper {
  private static final String PORT_SEPARATOR = ":";

  private StagingDirectorConfigurationsHelper(){}

  public static String resolveAddress(String url) {
    return url.split(PORT_SEPARATOR)[0];
  }

  public static Integer resolvePort(String url) {
    return Integer.parseInt(url.split(PORT_SEPARATOR)[1]);
  }

  public static long resolvePollingTimeFrame(int accessionDelay, String timeUnit) {
    switch (timeUnit.toLowerCase()) {
      case "minutes" : return accessionDelay * TimeUnit.MINUTES.toMillis(1);
      case "hours" : return accessionDelay * TimeUnit.HOURS.toMillis(1);
      case "days" : return accessionDelay * TimeUnit.DAYS.toMillis(1);
      case "weeks" : return (long) accessionDelay * TimeUnit.DAYS.toMillis(7);
      case "months": return (long) accessionDelay * TimeUnit.DAYS.toMillis(30);
      default: return 10_000L;
    }
  }
}
