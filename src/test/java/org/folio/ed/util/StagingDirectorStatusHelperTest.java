package org.folio.ed.util;

import static org.folio.ed.util.StagingDirectorConfigurationsHelper.resolvePollingTimeFrame;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class StagingDirectorStatusHelperTest {
  @Test
  void testTimeFrameResolver() {
    assertEquals(60_000, resolvePollingTimeFrame(1, "minutes"));
    assertEquals(3_600_000, resolvePollingTimeFrame(1, "hours"));
    assertEquals(86_400_000, resolvePollingTimeFrame(1, "days"));
    assertEquals(604_800_000, resolvePollingTimeFrame(1, "weeks"));
    assertEquals(2_592_000_000L, resolvePollingTimeFrame(1, "months"));
    assertEquals(10_000, resolvePollingTimeFrame(1, "default"));
  }
}
