require 'buildr/git_auto_version'
require 'buildr/gpg'

Buildr::MavenCentral.define_publish_tasks(:profile_name => 'org.realityforge', :username => 'realityforge')

PROVIDED_DEPS = [:javax_javaee, :javax_annotation]

OPTIONAL_DEPS = [:jndikit, :greenmail, :glassfish_embedded]

desc 'GuiceyLoops: Guice EE testing support'
define 'guiceyloops' do
  project.group = 'org.realityforge.guiceyloops'
  compile.options.source = '17'
  compile.options.target = '17'
  compile.options.lint = 'all'

  project.version = ENV['PRODUCT_VERSION'] if ENV['PRODUCT_VERSION']

  pom.add_apache_v2_license
  pom.add_github_project('realityforge/guiceyloops')
  pom.add_developer('realityforge', 'Peter Donald')
  pom.provided_dependencies.concat PROVIDED_DEPS
  pom.optional_dependencies.concat OPTIONAL_DEPS

  compile.with PROVIDED_DEPS,
               OPTIONAL_DEPS,
               :testng,
               :mockito_core,
               :byte_buddy,
               :objenesis,
               :hamcrest,
               :google_guice,
               :guava,
               :guava_failureaccess,
               :aopalliance,
               :asm

  # Make sure embedded glassfish jar is present as it is used in the tests
  compile do
    artifact(:glassfish_embedded).invoke
  end

  project.test.options[:properties] =
    {
      'user.timezone' => 'Australia/Melbourne',
      'embedded.glassfish.artifacts' => artifact(:glassfish_embedded).to_spec
    }

  test.with :h2db
  test.using :testng

  package(:jar)
  package(:sources)
  package(:javadoc)

  ipr.add_component_from_artifact(:idea_codestyle)
  ipr.add_testng_configuration('core',
                               :jvm_args => "-ea -Duser.timezone=Australia/Melbourne -Dembedded.glassfish.artifacts=#{artifact(:glassfish_embedded).to_spec}")

end
