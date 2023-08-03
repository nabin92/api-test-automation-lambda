package org.example;

import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;

import java.util.function.Consumer;
import java.util.function.Function;

import org.ado.fixture.CreateProjectFixture;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.apache.http.HttpStatus.SC_ACCEPTED;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.Matchers.*;

public class ProjectTest {
    private static final String pat = "qv2ja6dcqfrpxpdzppywnlwjnjtbdba55kks2ooelktuokwpabia";
    private static final String baseUri = "https://dev.azure.com";
    private String organization = "nabinghosh11";
    CreateProjectFixture createProjectFixture = new CreateProjectFixture();

    @Test
    public void shouldGetProject() {
        Response response = getResponse(r -> r.get("{organization}/_apis/projects/"));
        validateResponse(v -> v.body("$", hasKey("count")), response, SC_OK);
    }

    @Test
    public void shouldCreateProject() {
        Response processResponse = getResponse(r -> r.get("{organization}/_apis/process/processes"));
        String templateId = processResponse.jsonPath().getString("value[0].id");
        Response createProject = getResponse(r -> r.body(createProjectFixture.getProjectRequest(templateId)).post("{organization}/_apis/projects"));
        validateResponse(v -> v.body("$", hasKey("id")), createProject, SC_ACCEPTED);
    }

    public Response getResponse(Function<RequestSpecification, Response> function) {
        return function.apply(given()
                .header("Content-Type", "application/json")
                .queryParam("api-version", 7.0)
                .pathParam("organization", organization)
                .baseUri(baseUri)
                .filters(new RequestLoggingFilter(), new ResponseLoggingFilter())
                .auth().preemptive()
                .basic("accessToken", pat));
    }

    public void validateResponse(Consumer<ValidatableResponse> consumer, Response response, int statusCode) {
        consumer.accept(response.then().assertThat().statusCode(statusCode));
    }
}

