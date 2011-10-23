require 'buildr/java/emma'

desc "GuiceyLoops: Guice EE testing support to Guicey-fruit"
define('guiceyloops') do
  project.version = '0.2.1-dev'
  project.group = 'org.realityforge'
  compile.options.source = '1.6'
  compile.options.target = '1.6'
  compile.options.lint = 'all'

  compile.with :javax_inject,
               :javax_ejb,
               :javax_persistence,
               :javax_annotation,
               :google_guice,
               :aopalliance,
               :google_guice_assistedinject

  test.using :testng

  emma.include 'org.realityforge.*'

  package(:jar)
  package(:sources)
end
