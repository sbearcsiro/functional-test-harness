package au.org.ala.test

import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import static au.org.ala.test.Util.*

class ResponseDispatcher extends Dispatcher {

    private static final Logger log = LoggerFactory.getLogger(ResponseDispatcher)

    @Override
    MockResponse dispatch(RecordedRequest request) throws InterruptedException {
        try {
            if (request.getPath().equals("/test")) {
                return test(request);
            }
            return new MockResponse().setResponseCode(404);
        } catch (e) {
            log.error("Exception handling $request", e)
            return new MockResponse().setResponseCode(500);
        }
    }

    MockResponse test(RecordedRequest request) {
        def accept = request.getHeader("Accept");
        def type
        switch(accept) {
            case APPLICATION_JSON:
                type = 'json'
                break
            case APPLICATION_XML:
                type = 'xml'
                break
            default:
                type = 'html'
                break
        }

        return new MockResponse().setResponseCode(200).setHeader("Content-Type", accept).setBody(resource("responses/test.$type"));
    }

    String resource(String name) {
        return this.getClass().classLoader.getResource(name).newReader().text
    }
}
