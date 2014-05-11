require 'buildr/jacoco'
require 'buildr/git_auto_version'
require 'buildr/gpg'

PROVIDED_DEPS = [:javax_javaee, :javax_annotation]

OPTIONAL_DEPS = [:jndikit, :greenmail, :eclipselink, :glassfish_embedded]

desc "GuiceyLoops: Guice EE testing support to Guicey-fruit"
define 'guiceyloops' do
  project.group = 'org.realityforge.guiceyloops'
  compile.options.source = '1.7'
  compile.options.target = '1.7'
  compile.options.lint = 'all'

  pom.add_apache_v2_license
  pom.add_github_project("realityforge/guiceyloops")
  pom.add_developer('realityforge', "Peter Donald")
  pom.provided_dependencies.concat PROVIDED_DEPS
  pom.optional_dependencies.concat OPTIONAL_DEPS

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
