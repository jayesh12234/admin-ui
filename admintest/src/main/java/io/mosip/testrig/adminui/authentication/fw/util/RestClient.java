package io.mosip.testrig.adminui.authentication.fw.util;

import static io.restassured.RestAssured.given;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import io.mosip.testrig.adminui.kernel.util.ConfigManager;
import io.restassured.RestAssured;
import io.restassured.config.HttpClientConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.response.Response;

public class RestClient {

	private static final Logger RESTCLIENT_LOGGER = Logger.getLogger(RestClient.class);
	public static String ZONECODE;
	private static RestAssuredConfig config = RestAssured.config()
			.httpClient(HttpClientConfig.httpClientConfig().setParam("http.connection.timeout", 500000)
					.setParam("http.socket.timeout", 500000).setParam("http.connection-manager.timeout", 500000));
	
	
	
	
	public static Response getRequest(String url, String contentHeader, String acceptHeader) {
		RESTCLIENT_LOGGER.info("RESSURED: Sending a GET request to " + url);
		Response getResponse = given().config(config).relaxedHTTPSValidation().log().all().when().get(url).then().log()
				.all().extract().response();
		RESTCLIENT_LOGGER.info("REST-ASSURED: The response from the request is: " + getResponse.asString());
		RESTCLIENT_LOGGER.info("REST-ASSURED: The response Time is: " + getResponse.time());
		return getResponse;
	}
	
	static String getZoneCode(JSONArray responseArray, String code) {
		String  ZoneCode="";
	
		for (int i = 0; i < responseArray.length(); i++) {
			ZoneCode = responseArray.getJSONObject(i).getString("code");
		}
		
		return ZoneCode;
	}
	public static Response getRequestWithCookieAndPathParm(String url, Map<String, String> body, String contentHeader,
			String acceptHeader, String cookieName, String cookieValue) {
		Response getResponse;
		JSONObject responseJson = null;

			RESTCLIENT_LOGGER.info("REST-ASSURED: Sending a GET request to " + url);
			
			getResponse = given().config(config).relaxedHTTPSValidation().pathParams(body)
					.cookie(cookieName, cookieValue).log().all().when().get(url).then().log().all().extract()
					.response();
			
			RESTCLIENT_LOGGER.info( getResponse.asString());
			RESTCLIENT_LOGGER.info( getResponse.time());
			responseJson = new JSONObject(getResponse.getBody().asString());
			if(responseJson.has("response")) {
				org.json.JSONArray responseArray = responseJson.getJSONArray("response");
			 ZONECODE = getZoneCode (responseArray,"code");
				RESTCLIENT_LOGGER.info( ZONECODE);
				
			}
			

		return getResponse;
	}
	
	public static Response getRequestWithCookie(String url, String contentHeader, String acceptHeader,
			String cookieName, String cookieValue) {
		RESTCLIENT_LOGGER.info("REST-ASSURED: Sending a GET request to " + url);
		Response getResponse = given().config(config).relaxedHTTPSValidation().cookie(cookieName, cookieValue).log()
				.all().when().get(url).then().log().all().extract().response();
		RESTCLIENT_LOGGER.info("REST-ASSURED: The response from the request is: " + getResponse.asString());
		RESTCLIENT_LOGGER.info("REST-ASSURED: The response Time is: " + getResponse.time());
		return getResponse;
	}
	
	public static Response postRequestWithCookie(String url, Object body, String contentHeader, String acceptHeader,
			String cookieName, String cookieValue) {
		RESTCLIENT_LOGGER.info("REST-ASSURED: Sending a POST request to " + url);
		Response postResponse = given().config(config).relaxedHTTPSValidation().body(body).contentType(contentHeader)
				.cookie(cookieName, cookieValue).accept(acceptHeader).log().all().when().post(url).then().log().all()
				.extract().response();
		RESTCLIENT_LOGGER.info("REST-ASSURED: The response from the request is: " + postResponse.asString());
		RESTCLIENT_LOGGER.info("REST-ASSURED: The response Time is: " + postResponse.time());
		return postResponse;
	}
	public static Response patchRequestWithCookieAndQueryParm(String url, HashMap<String, String> body,
			String contentHeader, String acceptHeader, String cookieName, String cookieValue) {
		RESTCLIENT_LOGGER.info("REST-ASSURED: Sending a PATCH request to " + url);
		Response postResponse = given().config(config).relaxedHTTPSValidation().queryParams(body)
				.contentType(contentHeader).cookie(cookieName, cookieValue).accept(acceptHeader).log().all().when()
				.patch(url).then().log().all().extract().response();
		RESTCLIENT_LOGGER.info(postResponse.asString());
		RESTCLIENT_LOGGER.info(postResponse.time());
		return postResponse;
	}
}