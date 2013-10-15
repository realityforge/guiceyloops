require 'buildr/jacoco'
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
               :glassfish_embedded,
               :javax_mail,
               :google_guice,
               :aopalliance,
               :greenmail,
               :google_guice_assistedinject

  test.with :h2db
  test.using :testng

  package(:jar)
  package(:sources)
  package(:javadoc)

  jacoco.includes << 'org.realityforge.*'
  jacoco.generate_html = true
  jacoco.generate_xml = true
end
