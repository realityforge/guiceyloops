## 0.33:
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
