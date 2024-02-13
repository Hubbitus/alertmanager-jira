package info.hubbitus

import com.atlassian.jira.rest.client.api.domain.BasicIssue
import groovy.json.JsonBuilder
import info.hubbitus.DTO.AlertRequest
import jakarta.inject.Inject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.Response
import org.jboss.logging.Logger

import static jakarta.ws.rs.core.MediaType.*

@Path('/')
@Consumes(WILDCARD)
@Produces(APPLICATION_JSON)
@SuppressWarnings('unused')
class AlertController {
    @Inject
    JiraService jira

    @Inject
    Logger log

    @GET
    @Path('/ping')
    @Consumes(WILDCARD)
    @Produces(TEXT_PLAIN)
    @SuppressWarnings(['GrUnnecessaryPublicModifier', 'GrMethodMayBeStatic']) // That is controller, public required
    public Response ping() {
        return Response.ok().entity('pong').build()
    }

    @POST
    @Path('/alert')
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @SuppressWarnings(['GrUnnecessaryPublicModifier']) // That is controller, public required
    public Response alert(AlertRequest alertRequest) {
        log.debug("Got alertRequest with ${alertRequest.alerts.size()} alerts")

        List<BasicIssue> issues = jira.process(alertRequest)

        return Response.ok().entity(
            new JsonBuilder([
                result: 'ok',
                affected_issues: issues*.key
            ])
        ).build()
    }
}
