package org.folio.ed.domain.dto;

import lombok.Data;

import java.util.List;

@Data
public class Configurations {
  private List<Configuration> configurations;
  private int totalRecords;
}
