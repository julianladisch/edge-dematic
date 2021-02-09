
package org.folio.ed.domain.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
public class Contributor {
  private String name;
}
