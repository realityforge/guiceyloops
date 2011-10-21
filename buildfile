require 'buildr/java/emma'

desc "GuiceyLoops: Guice EE testing support to Guicey-fruit"
define('guiceyloops') do
  project.version = '0.2'
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

  emma.include 'org.realityforge.*'

  package(:jar)
end
