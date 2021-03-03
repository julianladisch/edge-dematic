package org.folio.ed.domain.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAlias;

import lombok.Data;

@Data
public class ResultList<E> {
  @JsonAlias("total_records")
  private Integer totalRecords;
  @JsonAlias({ "users", "accessions", "retrievals", "configurations" })
  private List<E> result;
}
