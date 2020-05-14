# guiceyloops

[![Build Status](https://api.travis-ci.com/realityforge/guiceyloops.svg?branch=master)](http://travis-ci.com/realityforge/guiceyloops)
[<img src="https://img.shields.io/maven-central/v/org.realityforge.guiceyloops/guiceyloops.svg?label=latest%20release"/>](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.realityforge.guiceyloops%22%20a%3A%22guiceyloops%22)
[![codecov](https://codecov.io/gh/realityforge/guiceyloops/branch/master/graph/badge.svg)](https://codecov.io/gh/realityforge/guiceyloops)

GuiceyLoops is a minimalistic library for aiding the testing of JEE applications
using Guice. The library add some type listeners that are aware of the JEE annotations
such as `@EJB`, `@Resource`, `@WebServiceRef` and `@PersistenceContext` so that fields
marked with these annotations will be injected in a Guice container.

The simplest way to use the library is to add the JEETestingModule as well as any modules
required to provide resources used in the test (i.e. an EntityManager module) as well as
any module that defines the components under test into one injector and access the components
under test from specified module. i.e.

```java
  Injector injector =
    Guice.createInjector( new MyTestModule(),
                          new MyEntityManagerModule(),
                          new JEETestingModule() );

  MyEJBService objectToTest = injector.getInstance( MyEJBService.class );
  ...
```
