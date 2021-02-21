package org.folio.ed.domain.entity;

import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import org.springframework.stereotype.Component;

@Data
@Component
@NoArgsConstructor
@AllArgsConstructor
@Builder
@With
public class ConnectionSystemParameters {

  private String username;

  private String password;

  @JsonIgnore
  private String okapiToken;

  @JsonIgnore
  private String tenantId;
}
