require 'buildr/jacoco'
require 'buildr/git_auto_version'

PROVIDED_DEPS = [:javax_ejb, :javax_persistence, :javax_jaxrpc, :javax_transaction,
                 :javax_inject, :javax_annotation, :javax_mail, :javax_jms]

OPTIONAL_DEPS = [:jndikit, :greenmail, :eclipselink, :glassfish_embedded]

desc "GuiceyLoops: Guice EE testing support to Guicey-fruit"
define 'guiceyloops' do
  project.group = 'org.realityforge.guiceyloops'
  compile.options.source = '1.7'
  compile.options.target = '1.7'
  compile.options.lint = 'all'

  compile.with PROVIDED_DEPS,
               OPTIONAL_DEPS,
               :testng,
               :mockito,
               :google_guice,
               :aopalliance,
               :google_guice_assistedinject

  # Make sure embedded glassfish jar is present as it is used in the tests
  compile do
    artifact(:glassfish_embedded).invoke
  end

  test.with :h2db
  test.using :testng

  package(:jar)
  package(:sources)
  package(:javadoc)

  jacoco.includes << 'org.realityforge.*'
  jacoco.generate_html = true
  jacoco.generate_xml = true
end
