package org.realityforge.guiceyloops.server;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table( name = "vwTestEntity2", schema = "Test")
public class TestEntity2
{
  @Id
  Integer pk;
}
