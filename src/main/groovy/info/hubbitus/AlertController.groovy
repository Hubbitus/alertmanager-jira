package info.hubbitus

import com.atlassian.jira.rest.client.api.domain.SearchResult
import groovy.json.JsonBuilder
import info.hubbitus.DTO.Alert
import jakarta.inject.Inject
import jakarta.ws.rs.*
import jakarta.ws.rs.core.Response
import org.jboss.logging.Logger

import static jakarta.ws.rs.core.MediaType.*

@Path('/')
@Consumes(WILDCARD)
@Produces(APPLICATION_JSON)
class AlertController {
    @Inject
    JiraService jira

    @Inject
    Logger log

    @GET
    @Path('/ping')
    @Consumes(WILDCARD)
    @Produces(TEXT_PLAIN)
    @SuppressWarnings('UnnecessaryPublicModifier') // That is controller, public required
    public Response ping() {
        return Response.ok().entity('pong').build()
    }

    @POST
    @Path('/alert')
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @SuppressWarnings('UnnecessaryPublicModifier') // That is controller, public required
    public Response alert(Alert alert) {
        log.debug('Got alert: ' + alert)
        log.debug("Jira service: ${jira}")
        log.debug("Jira service: ${jira.jiraRestClient}")
//        IssueRestClient issueClient = jira.jiraRestClient.getIssueClient()
//        jira.jiraRestClient.getSearchClient()
//        Promise<Issue> promise =  issueClient.getIssue('DATA-1')
//        Issue issue = promise.claim()

        def searchClient = jira.jiraRestClient.getSearchClient()
        SearchResult res = searchClient.searchJql('project = DATA AND labels = perfomance').claim()

        return Response.ok().entity(
            new JsonBuilder([result: 'ok'])
        ).build()
    }
}
