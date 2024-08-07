package com.company.base2.repository.model;

import com.company.base2.PojaGenerated;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@PojaGenerated
@Entity
@Getter
@Setter
public class Dummy {
  @Id private String id;
}
