package org.realityforge.guiceyloops.server.glassfish;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class GlassFishContainerUtil
{
  private GlassFishContainerUtil()
  {
  }

  @Nonnull
  public static File getWarFile()
  {
    return getWarFile( null );
  }

  @Nonnull
  public static File getWarFile( @Nullable final String prefix )
  {
    final String p = prefix == null ? "" : prefix + ".";
    final String filename = System.getProperties().getProperty( p + "war.filename", null );
    if ( null != filename )
    {
      final File file = new File( filename );
      if ( !file.exists() || file.isDirectory() )
      {
        final String message =
          "The system property '" + p + "war.filename' with value '" + filename +
          "' does not exist or is a directory.";
        throw new IllegalStateException( message );
      }
      return file;
    }
    else
    {
      final String warDir = System.getProperties().getProperty( p + "war.dir", null );
      if ( null != warDir )
      {
        final File dir = new File( warDir );
        if ( !dir.exists() || !dir.isDirectory() )
        {
          final String message =
            "The system property '" + p + "war.dir' with value '" + warDir +
            "' does not exist or is not a directory.";
          throw new IllegalStateException( message );
        }
        final File[] candidates = dir.listFiles( new FilenameFilter()
        {
          @Override
          public boolean accept( final File dir, final String name )
          {
            return name.endsWith( ".war" );
          }
        } );
        if ( 0 == candidates.length )
        {
          final String message =
            "No .war files found in '" + p + "war.dir' with value '" + warDir + "'.";
          throw new IllegalStateException( message );
        }

        Arrays.sort( candidates, new Comparator<File>()
        {
          @Override
          public int compare( final File o1, final File o2 )
          {
            return (int) ( o2.lastModified() - o1.lastModified() );
          }
        } );
        return candidates[ 0 ];
      }
      else
      {
        final String message =
          "The system properties '" + p + "war.filename' and '" + p + "war.dir' are not specified and " +
          " the tools is unable to determine location of war to deploy.";
        throw new IllegalStateException( message );

      }
    }
  }

  public static int getRandomPort()
  {
    return new Random().nextInt( 3000 ) + 9000;
  }

  @Nonnull
  public static URL[] getEmbeddedGlassFishClasspath( @Nonnull final GlassFishVersion version )
    throws Exception
  {
    return getEmbeddedGlassFishClasspath( version.getSpecs() );
  }

  @Nonnull
  public static URL[] getEmbeddedGlassFishClasspath()
    throws Exception
  {
    return getEmbeddedGlassFishClasspath( new String[ 0 ] );
  }

  @Nonnull
  public static URL[] getEmbeddedGlassFishClasspath( @Nonnull final String[] defaultDependencies )
    throws Exception
  {
    final ArrayList<URL> elements = new ArrayList<>();
    final String classpath = System.getProperties().getProperty( "embedded.glassfish.classpath", null );
    final String artifactsPath = System.getProperties().getProperty( "embedded.glassfish.artifacts", null );
    if ( null != classpath )
    {
      for ( final String filename : classpath.split( File.pathSeparator ) )
      {
        final File file = new File( filename );
        if ( !file.exists() )
        {
          final String message =
            "System property 'embedded.glassfish.classpath' specified but " +
            "contains filename '" + filename + "' that does not exist.";
          throw new IllegalStateException( message );
        }
        elements.add( file.toURI().toURL() );
      }
    }
    else if ( null != artifactsPath )
    {
      final String m2Repository = getMavenRepository();
      for ( final String spec : artifactsPath.split( "," ) )
      {
        final File file = specToFile( m2Repository, spec );
        if ( !file.exists() )
        {
          final String message =
            "System property 'embedded.glassfish.artifacts' specified but contains a spec '" + spec +
            ", that can not be located in the local maven repository at '" + file + "'.";
          throw new IllegalStateException( message );
        }
        elements.add( file.toURI().toURL() );
      }
    }
    else if ( 0 == defaultDependencies.length )
    {
      final String message =
        "System properties 'embedded.glassfish.classpath' and 'embedded.glassfish.artifacts' are " +
        "not specified, and no default dependencies were provided.";
      throw new IllegalStateException( message );
    }
    else
    {
      final String m2Repository = getMavenRepository();
      for ( final String spec : defaultDependencies )
      {
        final File file = specToFile( m2Repository, spec );
        if ( !file.exists() )
        {
          final String message =
            "System properties 'embedded.glassfish.classpath' and 'embedded.glassfish.artifacts' are not specified. " +
            "Attempting to use defaults but unable to locate default dependency '" + spec + "' in the local " +
            "maven repository at '" + file + "'.";
          throw new IllegalStateException( message );
        }
        elements.add( file.toURI().toURL() );
      }
    }
    return elements.toArray( new URL[ 0 ] );
  }

  static File specToFile( final String m2Repository, final String spec )
  {
    return new File( specToFilename( m2Repository, spec ) );
  }

  static String specToFilename( final String m2Repository, final String spec )
  {
    final String[] parts = spec.split( ":" );
    final String group = parts[ 0 ];
    final String artifact = parts[ 1 ];
    final String type = parts[ 2 ];
    final String version = parts[ 3 ];
    return m2Repository + File.separator +
           group.replace( ".", File.separator ) + File.separator +
           artifact + File.separator +
           version + File.separator +
           artifact + "-" + version + "." + type;
  }

  @Nonnull
  static String getMavenRepository()
  {
    final String m2Repository = System.getenv( "M2_REPO" );
    if ( null != m2Repository )
    {
      return m2Repository;
    }
    else
    {
      if ( null != System.getenv( "HOME" ) )
      {
        return System.getenv( "HOME" ) + File.separator + ".m2" + File.separator + "repository";
      }
      else
      {
        return System.getenv( "HOMEDRIVE" ) +
               File.separator +
               System.getenv( "HOMEPATH" ) +
               File.separator +
               ".m2" +
               File.separator +
               "repository";
      }
    }
  }
}
