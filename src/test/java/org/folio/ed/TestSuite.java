package org.folio.ed;

import org.folio.ed.integration.EmsIntegrationTest;
import org.folio.ed.integration.StagingDirectorIntegrationTest;
import org.folio.ed.security.EdgeSecurityFilterTest;
import org.folio.ed.security.SecurityManagerServiceTest;
import org.folio.ed.security.TenantAwareAWSParamStoreTest;
import org.folio.ed.service.StagingDirectorIntegrationServiceTest;
import org.folio.ed.util.StagingDirectorStatusHelperTest;
import org.junit.jupiter.api.Nested;

public class TestSuite {
  @Nested
  class SecurityManagerServiceTestNested extends SecurityManagerServiceTest {
  }

  @Nested
  class StagingDirectorIntegrationTestNested extends StagingDirectorIntegrationTest {
  }

  @Nested
  class EmsIntegrationTestNested extends EmsIntegrationTest {
  }

  @Nested
  class StagingDirectorStatusHelperTestNested extends StagingDirectorStatusHelperTest {
  }

  @Nested
  class TenantAwareAWSParamStoreTestNested extends TenantAwareAWSParamStoreTest {
  }

  @Nested
  class StagingDirectorIntegrationServiceTestNested extends StagingDirectorIntegrationServiceTest {
  }

  @Nested
  class EdgeSecurityFilterTestNested extends EdgeSecurityFilterTest {
  }
}
