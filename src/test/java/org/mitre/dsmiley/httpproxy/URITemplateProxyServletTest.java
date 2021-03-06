package org.mitre.dsmiley.httpproxy;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import org.junit.Ignore;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertThat;
import static org.junit.internal.matchers.StringContains.containsString;

public class URITemplateProxyServletTest extends ProxyServletTest {

  String urlParams;

  //a hack to pass info from rewriteMakeMethodUrl to getExpectTargetUri
  String lastMakeMethodUrl;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    lastMakeMethodUrl = null;
  }

  @Override
  protected void setUpServlet(Properties servletProps) {
    //Register a parameterized proxy servlet.
    // for the test, host should be localhost, $2 should be localTestServer port, and path should be targetPath
    String hostParam = "localhost";
    String portParam = String.valueOf(localTestServer.getServiceAddress().getPort());
    String pathParam = "targetPath";
    String userParam = "user2";
    String tenantParam = "tenant1";
    urlParams = "_host=" + hostParam + "&_port=" + portParam + "&_path=" + pathParam + "&_user=" + userParam;
    targetBaseUri = "http://" + hostParam + ":" + portParam + "/" + pathParam + "/" + userParam + "/" + tenantParam;
    servletProps.setProperty("targetUri", "http://{_host}:{_port}/{_path}/{_user}/{_tenant}");//template
    servletRunner.registerServlet("/proxyParameterized/*", URITemplateProxyServlet.class.getName(), servletProps);
    sourceBaseUri = "http://localhost/proxyParameterized";//localhost:0 is hard-coded in ServletUnitHttpRequest
  }

  @Override
  protected String rewriteMakeMethodUrl(String url) {
    lastMakeMethodUrl = url;
    //append parameters for the template
    url += (url.indexOf('?')<0 ? '?' : '&') + urlParams;
    return url;
  }

  @Override
  protected String getExpectedTargetUri(WebRequest request, String expectedUri) throws MalformedURLException, URISyntaxException {
    if (expectedUri == null) {
      expectedUri = lastMakeMethodUrl.substring(sourceBaseUri.length());
    } else {
      if (expectedUri.endsWith(urlParams))
        expectedUri = expectedUri.substring(0, expectedUri.length() - urlParams.length());
    }
    return new URI(this.targetBaseUri).getPath() + expectedUri;
  }

  @Override @Test
  @Ignore // because internally uses "new URI()" which is strict
  public void testProxyWithUnescapedChars() throws Exception {
  }

  @Override @Test
  @Ignore //because HttpUnit is faulty
  public void testSendFile() throws Exception {
  }

  @Override
  protected GetMethodWebRequest makeGetMethodRequest(final String url) {
    GetMethodWebRequest getMethodWebRequest = super.makeGetMethodRequest(url);
    getMethodWebRequest.setHeaderField("_tenant", "tenant1");
    getMethodWebRequest.setHeaderField("_user", "user2"); //header param overrides query param
    return getMethodWebRequest;
  }

  @Test
  public void shouldRetainArrayParmeterInQueryString() throws Exception {
    GetMethodWebRequest request = makeGetMethodRequest(String.format("%s?array=item_1&array=item_2", sourceBaseUri));
    String expectedTargetUri = getExpectedTargetUri(request, "?array=item_1&array=item_2");
    WebResponse response = servletRunner.getResponse(request);
    assertThat(response.getText(), containsString(expectedTargetUri));
  }

  @Test
  public void shouldReplaceVariablesFromSourceURL() throws Exception {

  }

  @Override
  protected PostMethodWebRequest makePostMethodRequest(final String url) {
    PostMethodWebRequest postMethodWebRequest = super.makePostMethodRequest(url);
    postMethodWebRequest.setHeaderField("_tenant", "tenant1");
    postMethodWebRequest.setHeaderField("_user", "user2"); //header param overrides query param
    return postMethodWebRequest;
  }

}
