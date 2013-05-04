package org.realityforge.guiceyloops.server;

import java.util.ArrayList;

class TestEntityManagerTestingModule
  extends EntityManagerTestingModule
{
  @Override
  protected void configure()
  {
    super.configure();
    final ArrayList<String> tables = new ArrayList<String>();
    collectTableName( tables, TestEntity1.class );
    collectTableName( tables, TestEntity2.class );
    requestCleaningOfTables( tables.toArray( new String[ tables.size() ] ) );
  }

  @Override
  protected String getPersistenceUnitName()
  {
    return "TestUnit" ;
  }
}
