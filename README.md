# functional-test-harness [![Build Status](https://travis-ci.org/sbearcsiro/functional-test-harness.svg?branch=develop)](https://travis-ci.org/sbearcsiro/functional-test-harness)
Simple test harness for describing HTTP tests

---

## Configuring tests.


The tests use a [HOCON (Human Optimised Config Object Notation)](https://github.com/typesafehub/config) file to create a suite of tests.

For example:

```
tests {  // tests wrapper
  httpbin-get { // creates a test named httpbin-get
    url="http://httpbin.org/get" // gives the test a URL
    pathTests= [
      { path="url", testExpression="http://httpbin.org/get" } // defines some jsonpath tests
    ]
  }
  httpbin-xml {
    url="http://httpbin.org/xml"
    acceptHeader=application/xml // request XML instead of JSON
    pathTests = [
      { path="/slideshow/slide/title", testExpression="\\w" } // xpath tests are also supported
    ]
  }
  httpbin-robots {
    url="http://httpbin.org/robots.txt"
    acceptHeader=text/html
    script=example.groovy // evaluate a groovy script that returns a boolean
  }
}
```

### Path tests

Path tests can be a [JSON path](http://goessner.net/articles/JsonPath/) or XPath expression, automatically detected based on the content type of the return service.

Path testing occurs as follows:
 
 1. Path expression is evaluated against the JSON or XML document and returns a list of nodes
 2. XML documents extract the text content of the nodes, JSON documents are assumed to only return single value nodes
 3. If the list is empty then the test fails (unless the test is marked as optional=true
 4. The testExpression is parsed as a regular expression
 5. If any entry in the list does not contain the regular expression, the test fails.
  

## Running tests

### Using Spock

Using spock, you can create a specification like this:

```groovy
class FunctionalTestSpec extends Specification {

    @Shared HttpTestRunner runner

    def setupSpec() {
        runner = HttpTestRunner.Builder.withClasspathResolver('test.conf').groovyScriptEvaluator().build()
    }

    def cleanupSpec() {
        server.shutdown()
    }

    @Unroll
    def "test #test.name"() {
        setup:

        when:
        def response = runner.run(test)

        then:

        response.statusCode == 200

        for (def pt : test.pathTests)
            assert response.evaluatePath(pt)

        if (test.script)
            assert runner.evaluateScriptTest(test.script, response)

        where:
        test << runner.tests.toList()
    }

}
```

This will read `test.conf` from the classpath and unroll each entry in the `tests` wrapper into it's own individual test.

### Using the command line

The `cli` subproject contains a version of the Spock test above which can be run via the command line.  Execute it like this:

    java -jar functional-test-harness-jar-with-dependencies.jar example.conf
    
Where `example.conf` is a path to a configuration file.  It will produce output like this:

```
$ java -jar target/functional-test-harness-jar-with-dependencies.jar example.conf 
Running httpbin-xml (1/3)
HTTP Request was successful... ✓
Evaluating path /slideshow/slide/title against \w... ✓

Running httpbin-robots (2/3)
HTTP Request was successful... ✓
Evaluating script: example.groovy... ✓

Running httpbin-get (3/3)
HTTP Request was successful... ✓
Evaluating path url against http://httpbin.org/get... ✓

```


The return code of the process indicates the number of failed tests.