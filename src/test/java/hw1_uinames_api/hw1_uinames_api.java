package hw1_uinames_api;

import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

public class hw1_uinames_api {
    @BeforeAll
    public static void setup(){
        baseURI="https://cybertek-ui-names.herokuapp.com/api/";
    }
    @Test
    @DisplayName("No params test")
    public void noParamsTest(){
        when()
                .get().prettyPeek()
                .then()
                .assertThat().statusCode(200)
                .assertThat().contentType("application/json; charset=utf-8")
                .body("name",is(not(empty())),"surname",is(not(empty())),"gender",is(not(empty())),"region",is(not(empty())));
    }

    @Test
    @DisplayName("Gender test")
    public void genderTest(){
        String gender="female";
        given()
                .queryParam("gender",gender)
                .when()
                .get().prettyPeek()
                .then()
                .assertThat()
                .statusCode(200).and()
                .contentType("application/json; charset=utf-8").and()
                .body("gender",is(gender))
                .log().ifError();
    }

    @Test
    @DisplayName("2 Params test")
    public void twoParamsTest(){
        String gender="male";
        String region="Germany";
        given()
                .queryParam("gender",gender)
                .queryParam("region",region)
                .when()
                .get()
                .then().assertThat()
                .statusCode(200).and()
                .contentType("application/json; charset=utf-8").and()
                .body("gender",is(gender))
                .body("region",is(region))
                .log().ifError();

    }
    @Test
    @DisplayName("Invalid gender test")
    public void invalidGenderTest(){
        String gender="femaleX";
        given()
                .queryParam("gender",gender)
                .when()
                .get()
                .then()
                .assertThat()
                .statusCode(400).and()
                .statusLine(containsString("Bad Request"))
                .body("error",is("Invalid gender"))
                .log().ifError();
    }

    @Test
    @DisplayName("Invalid region test")
    public void invalidRegionTest(){
        String region="Banana Republic";
        given()
                .queryParam("region",region)
                .when()
                .get()
                .then().assertThat()
                .statusCode(400).and()
                .statusLine(containsString("Bad Request"))
                .body("error",is("Region or language not found"))
                .log().ifError();
    }

    @Test
    @DisplayName("Amount and regions test")
    public void amountRegionsTest(){
        String region="Turkey";
        int amount=50;
        Response response=given()
                .queryParam("region",region)
                .queryParam("amount",amount)
                .when()
                .get()
                .then().assertThat()
                .statusCode(200).and()
                .contentType("application/json; charset=utf-8").extract().response();

       List<Map<String,Object>> people=response.jsonPath().get("");

       Set<String> nameSurnameSet=new HashSet<>();
       for(Map<String,Object> person:people)
           nameSurnameSet.add(person.get("name")+" "+person.get("surname"));
        System.out.println("nameSurnameSet = " + nameSurnameSet);
        Assertions.assertEquals(amount,nameSurnameSet.size(),"all name surnames are not unique");
    }

    @Test
    @DisplayName("3 params test")
    public void threeParamsTest(){
        String region = "Ukraine";
        String gender="female";
        int amount = 20;
        Response response=given()
                .queryParam("region",region)
                .queryParam("gender",gender)
                .queryParam("amount",amount)
                .when()
                .get()
                .then().assertThat()
                .statusCode(200).and()
                .contentType("application/json; charset=utf-8").and()
                .extract().response();
        List<String> regions=response.jsonPath().getList("region");
        List<String> genders=response.jsonPath().getList("gender");
        Assertions.assertTrue(regions.stream().allMatch(each->each.equals(region)),"regions are not same");
        Assertions.assertTrue(genders.stream().allMatch(each->each.equals(gender)),"genders are not same");
    }

    @Test
    @DisplayName("Amount count test")
    public void amountCountTest(){
        int amount = 20;
        given()
                .queryParam("amount",amount)
                .when()
                .get().prettyPeek()
                .then().assertThat()
                .statusCode(200).and()
                .contentType("application/json; charset=utf-8").and()
                .body("name",hasSize(amount))
                .log().ifError();
    }
}
