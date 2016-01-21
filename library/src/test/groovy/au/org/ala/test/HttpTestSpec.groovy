package au.org.ala.test

import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockWebServer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class HttpTestSpec extends Specification {

    private static final Logger log = LoggerFactory.getLogger(HttpTestSpec.class)

    @Shared HttpTestRunner runner
    @Shared MockWebServer server
    @Shared OkHttpClient client

    def setupSpec() {
        server = new MockWebServer()
        server.dispatcher = new ResponseDispatcher()
        server.start()
        System.setProperty('mockServer', server.url('').toString())
        client = new OkHttpClient()
        runner = HttpTestRunner.Builder.withClasspathResolver('test.conf').client(client).groovyScriptEvaluator().build()
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

        response.statusCode in 200..299

        for (def pt : test.pathTests)
            assert response.evaluatePath(pt)

        if (test.script)
            assert runner.evaluateScriptTest(test.script, response)

        where:
        test << runner.tests.toList()
    }

}
