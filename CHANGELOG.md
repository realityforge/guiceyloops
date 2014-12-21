## 0.48:
* Use File.pathSeparator when parsing embedded.glassfish.classpath so that guiceyloops will work
  under windows.
* Update the GlassFishContainer(int port) constructor to derive the classpath from system properties.
* Add GlassFishContainerUtil.getEmbeddedGlassFishClasspath() that expects one of the system
  properties to be specified.
* Support embedded.glassfish.artifacts system property to configure the class path for the
  embedded glassfish server.

## 0.47:
* Randomize the smtp port used in GreenMail tests to allow multiple tests to run concurrently.
* Upgrade greenmail dependency to 1.4.0

## 0.46:
* Fix regression introduced in 0.45 that meant buildr required greenmail to be present on
  the classpath to scan for test classes.

## 0.45:
* Ensure all the GreenMail mail server threads have started before completing start operation.

## 0.44:
* Update PersistenceTestModule to change the way that entity listeners are injected.

## 0.43:
* Add 4.1 to GlassFishVersion and make it the default.

## 0.42:
* Add some helper methods for binding dependencies in AbstractModuleTest.

## 0.41:
* Remove some unnecessary final qualifiers on methods in AbstractSharedTest.

## 0.40:
* Extract AbstractServerTest.resetJndiContext() from shutdownTransactionSynchronizationRegistry()
* Fix bug in AbstractSharedTest where getModules() called getDefaultTestModule()
  rather than getTestModule().

## 0.39:
* Extract AbstractSharedTest from AbstractServerTest to allow reuse of the test
  infrastructure in client-side (i.e. GWT) and non-JEE frameworks.
* Move AbstractModule to the shared package but create a deprecated old AbstractModule
  that extends the new AbstractModule.

## 0.38:
* Support multiple databases with the framework. Use prefixes to system settings to
  configure the non-primary database.

## 0.37:
* Ensure that the @Resource injections will use value of lookup parameter as the name
  if not null.

## 0.36:
* Tighten up the types on InjectUtil.toObject( type, object ).
* Add DbCleaner.isTransactionActive() and DbCleaner.isCleanScheduled() utility methods.

## 0.35:
* Add helper method to GlassFishContainer to create custom boolean resources.
* Avoid calling EntityTransaction.flush() in AbstractServerTest.flush() method when transaction is not active.

## 0.34:
* Simplify testing with mock persistence contexts, by null checking in the AbstractServerTest.flush() method.

## 0.33:
* Ensure @javax.enterprise.context.Dependent is a scope guice supports.
* Improve exception throw from PersistenceTestModule.requestInjectionForEntityListener when unable to find model in session.

## 0.32:
* Rework GlassFishContainer to raise an exception when the command is anything less than success.

## 0.31:
* Support different glassfish container versions in GlassFishContainer. Default to 4.0.

## 0.30:
* Move to eclipselink 2.5.1.

## 0.29:

* Remove support for recently added "embedded.glassfish.specs" system setting in as the implementation is unlikely
  to be usable.
* Add utility method GlassFishContainer.addSpecToClasspath() that can add maven style dependencies to classpath when
  creating GlassFish.
* Add guard against adding file that does not exist to GlassFishContainer's classpath.
* Add an extra constructor to GlassFishContainer for usability purposes.

## 0.28:

* Initial public release.
