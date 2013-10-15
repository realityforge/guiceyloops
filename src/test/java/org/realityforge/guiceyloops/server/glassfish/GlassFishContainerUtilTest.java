package org.realityforge.guiceyloops.server.glassfish;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class GlassFishContainerUtilTest
{
  private File _wardir;

  @AfterMethod
  public void cleanupDir()
    throws IOException
  {
    if ( null != _wardir )
    {
      deleteWarDir();
      _wardir = null;
    }
  }

  @Test
  public void getWarFile()
    throws Exception
  {
    final File warDir = getWarDir();
    final File warFile = createWar( warDir, "myfile" );
    assertTrue( warFile.setLastModified( System.currentTimeMillis() - 8000 ) );
    final File warFile2 = createWar( warDir, "myfile2" );
    assertTrue( warFile2.setLastModified( System.currentTimeMillis() ) );

    try
    {
      System.setProperty( "war.dir", warDir.getAbsolutePath() );
      assertEquals( GlassFishContainerUtil.getWarFile(), warFile2 );

      System.setProperty( "war.filename", warFile.getAbsolutePath() );
      assertEquals( GlassFishContainerUtil.getWarFile(), warFile );
    }
    finally
    {
      System.getProperties().remove( "war.filename" );
      System.getProperties().remove( "war.dir" );
    }
  }

  private File createWar( final File warDir, final String warName )
    throws IOException
  {
    final File warFile = new File( warDir, warName + ".war" );
    final ZipOutputStream zipFile = new ZipOutputStream( new FileOutputStream( warFile ) );
    zipFile.putNextEntry( new ZipEntry( "Ignored.txt" ) );
    zipFile.close();
    return warFile;
  }

  private File getWarDir()
    throws IOException
  {
    if ( null == _wardir )
    {
      _wardir = File.createTempFile( "wardir", ".d" );
      delete( _wardir );
      assertTrue( _wardir.mkdirs() );
    }
    return _wardir;
  }

  private void deleteWarDir()
  {
    if ( _wardir.exists() && _wardir.isDirectory() )
    {
      final File[] files = _wardir.listFiles();
      if ( null != files )
      {
        for ( final File file : files )
        {
          delete( file );
        }
      }
    }
    delete( _wardir );
  }

  private void delete( final File file )
  {
    if ( !file.delete() )
    {
      file.deleteOnExit();
    }
  }
}
