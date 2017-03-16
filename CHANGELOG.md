## 0.85 (Pending):
* Add some factory methods to `GlassFishContainer` to create concurrent resources.

## 0.84:
* Ensure `PersistenceTestModule` will gracefully handle listeners that do not
  implement `EntityListener`. 

## 0.83:
* Add helper class `OpenMQUtil` to help test OpenMQ state during tests.
* Add several methods to `OpenMQContainer` that support creating queues and topics
  within the broker.
* Add method to expose the `BrokerInstance` in `OpenMQContainer`.
* Add method to expose the `ConnectionFactory` field in `OpenMQContainer`.
* Add method to create queue and topic references in `OpenMQContainer`.
* Customize `JMSConnectionFactory` to make it a binding annotation so that it works within the tests.

## 0.82:
* Add helper methods `AbstractServerTest.ctran` that clear the EntityManager before running block.

## 0.81:
* Add `JaxbUtil` to help testing jaxb annotated entities.
* Add helper methods `AbstractServerTest.inTransaction` and `AbstractServerTest.tran`
  to make execute code blocks in a transaction easier in Java 8.
* Make several methods in AbstractAppServer public to ease access from within test
  infrastructure.
* Throw an exception in OpenMQContainer.createConnection() if it is called before the
  broker is started.
* Update logging in OpenMQContainer to emit the address that the broker is bound to.

## 0.80:
* Update AbstractServerTest to support binding EntityManager under alternative name.
* Update AbstractServerTest to add helper methods that iterate over all EntityManager
  and begin, commit and rollback transactions on EntityManager instances.
* Update AbstractServerTest.clear() to clear all EntityManager instances.
* Update AbstractServerTest.flush() to flush all EntityManager instances.

## 0.79:
* Update PersistenceTestModule to support binding under alternative name.

## 0.78:
* Add PersistenceTestModule.getPersistenceUnitName() helper method.

## 0.77:
* Make PersistenceTestModule.configure non-final.

## 0.76:
* Lock down the default timezone to Australia/Melbourne.

## 0.75:
* Prefix the default sentinel values in DatabaseUtil with the database prefix. 
* Make sure TinyHttpd returns the correct address after the server has been started.
* Support named servers in TinyHttpdFactory.

## 0.74:
* Make the port for TinyHttpd configurable but default to the OS selecting a free port.
* Add TinyHttpd.getAddress() method.

## 0.73:
* Delay the creation of the underlying httpd server until TinyHttpd.start() called.

## 0.72:
* Add GlassFishContainer.createJmsTopic() helper method.

## 0.71:
* Incorporate the TinyHttpd test code that is duplicated through our codebases.

## 0.70:
* Ensure jdbc30DataSource is set to true when configuring jtds driver as Payara as of version
  4.1.1.162 will no longer silently ignore jdbc3 only drivers.

## 0.69:
* Support managing OpenMQ in AbstractAppServer.
* Remove AbstractAppServer.getProperty() as not really the right place to put it.

## 0.68:
* Introduce AbstractAppServer to simplify managing singleton GlassFish instance within test suite.

## 0.67:
* Fix AssertUtil.assertNoFinalMethodsForCDI() so that parent classes are also checked.

## 0.66:
* Add simplified accessor AbstractServerTest.em(UnitName) for EntityManagers with specific names.
* Bind mock Principal in ServerTestModule. Useful to get caller principal in CDI applications.
* Update ServerTestModule to make it possible to override creation of specific resources.
* Add AssertUtil.assertNoFinalMethodsForCDI() helper method to help test to ensure types are CDI compatible.

## 0.65:
* Remove excessive logging in GreenMailTestModule.
* Add AbstractSharedTest.postInjector() hook method that is invoked after the injector is created.
* Import ValueUtil from all downstream projects.

## 0.64:
* Issue restart to GlassFish container after jms host changes.

## 0.63:
* Resurrect part of DerbyUtil that ensures logs are not emitted in working directory.

## 0.62:
* Add some utility methods to GlassFishContainer to simplify manipulation of jms, javamail and iiop resources.
* Expose host address in OpenMQContainer via getHostAddress().
* Add some nullability annotations to GlassFishContainer.
* Remove DerbyUtil as has not worked in a long time.

## 0.61:
* Support setRollbackOnly(), getRollbackOnly() and getTransactionStatus() on TestTransactionSynchronizationRegistry.
* Move to using embedded payara rather than embedded glassfish.
* Expose port in test MQ instance via OpenMQContainer.getPort().
* Mark OpenMQContainer class as final.
* Support overriding of properties in MQ instance by passing overrides into th constructor.
* Expose complete properties for MQ instance via OpenMQContainer.getProperties().

## 0.60:
* Ensure properties are correctly encoded when creating custom resources.

## 0.59:
* Add some new methods to GlassFishContainer for creating custom resources of different types.

## 0.58:
* Ensure that the DbCleaner clears the second level EntityManager cache when it performs a clean.

## 0.57:
* Add support for specifying additional database properties in PersistenceTestModule constructor
  and DatabaseUtil.createEntityManager() method.

## 0.56:
* Add support for Microsoft Windows environment vars when attempting to determine Maven repo path. Submitted by James Walker.
* Add Support for parsing postgres urls in DatabaseUtil. Submitted by James Walker.
* Add GlassFishContainer.createPostgresJdbcResource() helper methods. Submitted by James Walker.
* Remove default database driver, from DatabaseUtil and force users to specify driver.
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
