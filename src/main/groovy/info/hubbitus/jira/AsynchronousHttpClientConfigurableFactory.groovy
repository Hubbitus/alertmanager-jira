package info.hubbitus.jira


import com.atlassian.httpclient.apache.httpcomponents.DefaultHttpClientFactory
import com.atlassian.httpclient.api.HttpClient
import com.atlassian.httpclient.api.factory.HttpClientOptions
import com.atlassian.jira.rest.client.api.AuthenticationHandler
import com.atlassian.jira.rest.client.internal.async.AsynchronousHttpClientFactory
import com.atlassian.jira.rest.client.internal.async.AtlassianHttpClientDecorator
import com.atlassian.jira.rest.client.internal.async.DisposableHttpClient
import com.atlassian.sal.api.executor.ThreadLocalContextManager

/**
* Extend {@link AsynchronousHttpClientFactory} to allow configure the HTTP client.
**/
class AsynchronousHttpClientConfigurableFactory extends AsynchronousHttpClientFactory {
    /**
    * Create a new {@link DisposableHttpClient} with the given {@link AuthenticationHandler} and {@link HttpClientOptions}.
    * This method mostly copy/paste from {@link AsynchronousHttpClientFactory#createClient(URI, AuthenticationHandler)} but we allow provide parameter httpOptions to configure the HTTP client.
    * The idea borrowed from https://stackoverflow.com/questions/22175374/java-jira-rest-client-timeout-issue/38217268#38217268}
    *
    * @param httpOptions The main parameter, added to configure client
    **/
    @SuppressWarnings("unchecked")
    static DisposableHttpClient createClient(final URI serverUri, final AuthenticationHandler authenticationHandler, HttpClientOptions httpOptions) {
        final DefaultHttpClientFactory defaultHttpClientFactory = new DefaultHttpClientFactory(new AsynchronousHttpClientFactory.NoOpEventPublisher(),
                new AsynchronousHttpClientFactory.RestClientApplicationProperties(serverUri),
                new ThreadLocalContextManager() {
                    @Override
                    Object getThreadLocalContext() {
                        return null
                    }

                    @Override
                    void setThreadLocalContext(Object context) {
                    }

                    @Override
                    void clearThreadLocalContext() {
                    }
                }
        )

        final HttpClient httpClient = defaultHttpClientFactory.create(httpOptions)

        return new AtlassianHttpClientDecorator(httpClient, authenticationHandler) {
            @Override
            void destroy() throws Exception {
                defaultHttpClientFactory.dispose(httpClient)
            }
        }
    }
}
