require 'buildr/java/emma'
require 'buildr/git_auto_version'

desc "GuiceyLoops: Guice EE testing support to Guicey-fruit"
define 'guiceyloops' do
  project.group = 'org.realityforge.guiceyloops'
  compile.options.source = '1.6'
  compile.options.target = '1.6'
  compile.options.lint = 'all'

  compile.with :javax_inject,
               :testng,
               :mockito,
               :eclipselink,
               :jndikit,
               :javax_ejb,
               :javax_persistence,
               :javax_transaction,
               :javax_annotation,
               :javax_mail,
               :google_guice,
               :aopalliance,
               :greenmail,
               :google_guice_assistedinject

  test.with :h2db
  test.using :testng

  emma.include 'org.realityforge.*'

  package(:jar)
  package(:sources)
  package(:javadoc)
end
