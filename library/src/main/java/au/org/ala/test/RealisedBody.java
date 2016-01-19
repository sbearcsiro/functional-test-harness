package au.org.ala.test;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import lombok.val;
import okhttp3.MediaType;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static au.org.ala.test.Util.*;
import static com.jayway.jsonpath.Option.ALWAYS_RETURN_LIST;
import static com.jayway.jsonpath.Option.SUPPRESS_EXCEPTIONS;

/**
 * Takes the HTTP response body and consumes the input stream to create the response in a number of different formats so
 * that the body can be reused multiple times.
 *
 * Obviously this is not good for memory usage.
 */
public class RealisedBody {

    private static final Logger logger = LoggerFactory.getLogger(RealisedBody.class);

    private static final DOMImplementationRegistry registry;
    private static final XPathFactory xPathFactory = XPathFactory.newInstance();
    private static final Configuration jsonPathConfig = Configuration.defaultConfiguration().addOptions(ALWAYS_RETURN_LIST, SUPPRESS_EXCEPTIONS);

    static {
        try {
            registry = DOMImplementationRegistry.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static final Charset UTF8 = Charset.forName("UTF-8");

    public final int statusCode;

    public final byte[] bytes;
    public final MediaType mediaType;
    public final String string;
    public final DocumentContext jsonDocument;
    public final Document xmlDocument;

    public RealisedBody(Response response) throws IOException {
        val body = response.body();
        statusCode = response.code();
        bytes = body.bytes();
        mediaType = body.contentType();
        string = new String(bytes, mediaType != null ? mediaType.charset(UTF8) : UTF8);
        if (mediaType != null && (mediaType.toString().startsWith(APPLICATION_JSON) || mediaType.subtype().contains(JSON_BASED))) {
            jsonDocument = JsonPath.using(jsonPathConfig).parse(string);
        } else jsonDocument = null;

        if (mediaType != null && (mediaType.toString().startsWith(APPLICATION_XML) || mediaType.subtype().contains(XML_BASED))) {
            val impl = (DOMImplementationLS)registry.getDOMImplementation("LS");
            val parser = impl.createLSParser(DOMImplementationLS.MODE_SYNCHRONOUS, null);
            val input = impl.createLSInput();
            input.setStringData(string);
            xmlDocument = parser.parse(input);
        } else xmlDocument = null;

        body.close();
    }

    public boolean isSuccessful() {
        return statusCode >= 200 && statusCode < 300;
    }

    public boolean evaluatePath(PathTest test) {
        if (jsonDocument != null) {
            return evaluateJsonPath(test);
        } else if (xmlDocument != null) {
            return evaluateXmlPath(test);
        } else {
            throw new IllegalStateException("Can't evaluate " + test + " because the response didn't contain a JSON or XML body");
        }
    }

    public boolean evaluateJsonPath(PathTest test) {
        List<String> results = jsonDocument.read(test.path);
        return evaluateStringList(test, results);
    }

    public boolean evaluateXmlPath(PathTest test) {
        val xpath = xPathFactory.newXPath();
        try {
            val expr = xpath.compile(test.path);
            NodeList nodeList = (NodeList) expr.evaluate(xmlDocument, XPathConstants.NODESET);
            // val results = nodeList.map(Node::textContent)
            List<String> results = new ArrayList<>(nodeList.getLength());
            for (int i = 0; i < nodeList.getLength(); ++i) {
                results.add(nodeList.item(i).getTextContent());
            }
            return evaluateStringList(test, results);
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean evaluateStringList(PathTest test, List<String> pathResults) {
        boolean retVal = true;
        if (!test.isOptional() && pathResults.isEmpty()) {
            logger.error("Path result {} is not optional but there are no matching paths", test);
            return false;
        }
        val pattern = test.getPattern();
        for (val pathResult : pathResults) {
            val matcher = pattern.matcher(pathResult);
            val matched = matcher.find();
            if (!matched) logger.error("Path result of {} does not match {}", pathResult, pattern);
            retVal &= matched;
        }
        return retVal;
    }
}
