## 0.56 (Pending):
* Implement AbstractServerTest.getEntityModule() and make return null so that subclasses need not implement.
* Deprecate AbstractServerTest.getEntityModule() and suggest overriding getModules() instead.

## 0.55:
* Add FlushingTestModule.bindService() method that takes classnames.
* Rework PersistenceTestModule constructor to pass databasePrefix as an optional parameter.
* Remove PersistenceTestModule.registerUserTransaction() as it is unused.
* Pass the tables to clean as a parameter to the constructor.
* Remove support for deriving the tables to clean in PersistenceTestModule from @Table annotations as
  domgen now explicitly lists all the tables to clean.
* Always inject EntityListeners in PersistenceTestModule as no longer need to support EE6 and make
  requestInjectionForAllEntityListeners() method private.
* Make the persistence unit name mandatory in PersistenceTestModule and pass it in through the constructor.
* Require a non-null persistence unit name be passed into MockPersistenceTestModule.
* Remove AbstractPersistenceTestModule as no longer providing functionality to any sub-class.
* Remove ability of MockPersistenceTestModule to bind EntityManager without a name.
* Remove MockPersistenceTestModule.registerUserTransaction() as this is unused in any sub-class.
* Remove AbstractPersistenceTestModule.registerTransactionSynchronizationRegistry() as this is unused
  since equivalent functionality became part of the ServerTestModule class.

## 0.54:
* Add simplified constructor to FlushingTestModule that defaults to flushing at start of interception.

## 0.53:
* Remove deprecated org.realityforge.guiceyloops.server.AbstractModule
* Implement AbstractSharedTest.getDefaultTestModule() and return null, and removed
  implementation in AbstractServerTest.

## 0.52:
* Migrate all the type listeners to the server package.

## 0.51:
* Refactor ServerTestModule so that it is not able to be extended and ensure it is added to
  modules in AbstractServerTest by default.
* Introduce a minimalistic BeanManager implementation used during testing.
* Bind BeanManager into JNDI if it is present in the Guice Injector.
* Rework the JNDI infrastructure so that it is set up correctly before the test and cleared in
  postTest(). The setup occurs prior to the injector construction and after the test completes.
* Update AbstractServerTest.getDefaultTestModule() to stop returning ServerTestModule as all
  subclasses override the method.

## 0.50:
* Update AbstractModule.bindMock() to return the mocks.

## 0.49:
* Update AbstractServerTest.getDefaultTestModule() to return a module rather than a ServerTestModule.

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
