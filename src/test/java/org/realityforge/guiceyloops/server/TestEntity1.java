package org.realityforge.guiceyloops.server;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table( name = "tblTestEntity1", schema = "Test")
public class TestEntity1
{
  @Id
  Integer pk;
}
