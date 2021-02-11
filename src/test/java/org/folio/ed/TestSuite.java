package org.folio.ed;

import org.folio.ed.integration.StagingDirectorTest;
import org.folio.ed.service.SecurityManagerServiceTest;
import org.folio.ed.util.StagingDirectorStatusHelperTest;
import org.junit.jupiter.api.Nested;

public class TestSuite {
  @Nested
  class SecurityManagerServiceTestNested extends SecurityManagerServiceTest {
  }

  @Nested
  class StagingDirectorTestNested extends StagingDirectorTest {
  }

  @Nested
  class StagingDirectorStatusHelperTestNested extends StagingDirectorStatusHelperTest {
  }
}
