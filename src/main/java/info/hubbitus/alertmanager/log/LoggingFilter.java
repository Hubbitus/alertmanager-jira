package info.hubbitus.alertmanager.log;

import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
* Class to log raw JSON request for the easy reproducing and debug alertmanager events.
* @link <a href="https://stackoverflow.com/questions/46088258/logging-request-with-jax-rs-resteasy-and-containerrequestfilter-containerrespons/46088558#46088558">By SO answer</a>
**/
@Provider
@SuppressWarnings("unused") // Used implicitly by @Provider annotation and Quarkus injection
class LoggingFilter implements ContainerRequestFilter {

    @Inject
    Logger log;

    /* Useful stuff for later development purposes.
    @Context
    UriInfo info;

    @Context
    HttpServerRequest request;
    */

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        byte[] requestSource = requestContext.getEntityStream().readAllBytes();
        log.debug("Raw JSON request on URL [" + requestContext.getUriInfo().getAbsolutePath() + "]:\n" + new String(requestSource, UTF_8));
        // Set back input stream for the controllers
        requestContext.setEntityStream(new ByteArrayInputStream(requestSource));
    }
}
