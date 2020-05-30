package hw2_github_api;
import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeAll;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class GitHubApiTesting {

    @BeforeAll
    public static void setup(){
        baseURI="https://api.github.com";
    }

    @Test
    @DisplayName("Verify organization information")
    public void getOrganizationInformation(){
        given()
                .contentType("application/json; charset=utf-8")
                .when()
                .get("/orgs/{org}","cucumber")
                .then()
                .assertThat()
                .statusCode(200).and()
                .contentType("application/json; charset=utf-8").and()
                .body("login",is("cucumber")).and()
                .body("name",is("Cucumber")).and()
                .body("id",is(320565)).and()
                .log().ifError();
    }

    @Test
    @DisplayName("Verify error message")
    public void getErrorMessage(){
        given()
                .accept("application/xml")
                .pathParam("org","cucumber")
                .when()
                .get("/orgs/{org}")
                .then()
                .assertThat()
                .statusCode(415).and()
                .contentType("application/json; charset=utf-8")
                .statusLine(containsString("Unsupported Media Type"))
                .log().ifError();
    }

    @Test
    @DisplayName("Number of repositories")
    public void getNumberOfRepos(){
        Response response=given()
                .accept(ContentType.JSON)
                .pathParam("org","cucumber")
                .when()
                .get("/orgs/{org}")
                .then()
                .assertThat().statusCode(200).extract().response();
        int publicRepos=response.jsonPath().getInt("public_repos");

        Response response1=given()
                .accept(ContentType.JSON)
                .when()
                .get("/orgs/{org}/repos","cucumber")
                .then()
                .assertThat()
                .statusCode(200).extract().response();
        int actual=response1.jsonPath().getList("id").size();
        Assertions.assertEquals(publicRepos,actual,"Number of repositories doesn't match");
    }
    @Test
    @DisplayName("Repository id information")
    public void verifyIDNumbers(){
        Response response=given()
                .accept(ContentType.JSON)
                .when()
                .get("/orgs/{org}/repos","cucumber")
                .then()
                .assertThat().statusCode(200).extract().response();

        List<Integer> idList=response.jsonPath().getList("id");
        Set<Integer> idSet=new HashSet<>(idList);
        Assertions.assertEquals(idList.size(),idSet.size(),"ID field is not unique");

        List<String> nodeIDList=response.jsonPath().getList("node_id");
        Set<String> nodeIDSet=new HashSet<>(nodeIDList);
        Assertions.assertEquals(nodeIDList.size(),nodeIDSet.size(),"Node ids are not unique");
    }

    @Test
    @DisplayName("Repository owner information")
    public void verifyRepositoryOwner(){
        Response response=given()
                .accept(ContentType.JSON)
                .when()
                .get("/orgs/{org}","cucumber")
                .then()
                .assertThat().statusCode(200).extract().response();
        int id=response.jsonPath().getInt("id");

        Response response1=given()
                .accept(ContentType.JSON)
                .when()
                .get("/orgs/{org}/repos","cucumber")
                .then()
                .assertThat().statusCode(200).extract().response();

        List<Integer> ownerIDList=response1.jsonPath().getList("owner.id");
        int countDifferent= (int)ownerIDList.stream().filter(each->each!=id).count();

        Assertions.assertEquals(0,countDifferent,"All owner ids are same");
    }

    @Test
    @DisplayName("Ascending order by full_name sort")
    public void verifySortedByFullName(){
        Response response=
                given()
                .accept(ContentType.JSON)
                .pathParam("org","cucumber")
                .queryParam("sort","full_name")
                .when()
                .get("/orgs/{org}/repos")
                .then().assertThat().statusCode(200).extract().response();
        List<String> fullNames=response.jsonPath().getList("full_name");
        //fullNames.forEach(System.out::println);
        List<String> sortedList=response.jsonPath().getList("full_name");
        Collections.sort(sortedList);
        Assertions.assertEquals(sortedList,fullNames,"Response is not sorted based on Full name");
    }
    @Test
    @DisplayName("Descending order by full_name sort")
    public void verifyDescendingSortedByFullName(){
        Response response=
                given()
                        .accept(ContentType.JSON)
                        .pathParam("org","cucumber")
                        .queryParam("sort","full_name")
                        .queryParam("direction","desc")
                        .when()
                        .get("/orgs/{org}/repos")
                        .then().assertThat().statusCode(200).extract().response();
        List<String> fullNames=response.jsonPath().getList("full_name");
       // fullNames.forEach(System.out::println);
        List<String> sortedList=response.jsonPath().getList("full_name");
        Collections.sort(sortedList);
        Collections.reverse(sortedList);
        Assertions.assertEquals(sortedList,fullNames,"Response is not sorted in descending order based on Full name");
    }

    @Test
    @DisplayName("Default sort")
    public void verifyDefaultSort(){
        Response response=
                given()
                        .accept(ContentType.JSON)
                        .pathParam("org","cucumber")
                        .when()
                        .get("/orgs/{org}/repos")
                        .then().assertThat().statusCode(200).extract().response();
        List<LocalDateTime> createDateList=response.jsonPath().getList("created_at");
        List<LocalDateTime> sortedCreateDateList=response.jsonPath().getList("created_at");
        Collections.sort(sortedCreateDateList);
        Assertions.assertEquals(sortedCreateDateList,
                createDateList,
                "Default all repositories are NOT listed in descending order based on the value of the field\n" +
                "created_at");
    }
}
