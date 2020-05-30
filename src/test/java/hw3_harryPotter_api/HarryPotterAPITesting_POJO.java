package hw3_harryPotter_api;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pojos.HPCharacter;

import java.util.*;

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class HarryPotterAPITesting_POJO {
    static RequestSpecification reqSpec;
    @BeforeAll
    public static void setup(){
        baseURI="https://www.potterapi.com/v1";
        reqSpec= new RequestSpecBuilder()
                .setAccept(ContentType.JSON)
                .setBaseUri("https://www.potterapi.com/v1")
                .build();
        reqSpec.queryParam("key","$2a$10$4H98diBcZAHUG86kMTRPqeXeSyWGOA3ISghG2hUVyAI1aSoLoNnRq");
    }

    @Test
    @DisplayName("Verify sorting hat")
    public void verifySortingHat(){
        String[]houses={"\"Gryffindor\"", "\"Ravenclaw\"", "\"Slytherin\"", "\"Hufflepuff\""};
        given()
                .accept("application/json; charset=utf-8")
                .when()
                .get("/sortingHat")
                .then()
                .assertThat().statusCode(200).and()
                .contentType("application/json; charset=utf-8")
                .body(is(in(houses)))
                .log().ifError();
    }

    @Test
    @DisplayName("Verify bad key")
    public void verifyBadKey(){
        given()
                .accept(ContentType.JSON)
                .queryParam("key","invalid")
                .when()
                .get("/characters")
                .then()
                .assertThat()
                .statusCode(401).and()
                .contentType("application/json; charset=utf-8").and()
                .statusLine(containsString("Unauthorized")).and()
                .body(is("{\"error\":\"API Key Not Found\"}"))
                .log().ifError();
    }
    @Test
    @DisplayName("Verify no key")
    public void verifyNoKey() {
        given()
                .accept(ContentType.JSON)
                .when()
                .get("/characters")
                .then()
                .assertThat()
                .statusCode(409).and()
                .contentType("application/json; charset=utf-8").and()
                .statusLine(containsString("Conflict")).and()
                .body(is("{\"error\":\"Must pass API key for request\"}"))
                .log().ifError();
    }

    @Test
    @DisplayName("Verify number of characters")
    public void verifyNumberOfCharacters(){
        Response response=given()
                .spec(reqSpec)
                .when()
                .get("/characters")
                .then()
                .assertThat()
                .statusCode(200).and()
                .contentType("application/json; charset=utf-8").extract().response();
        List<HPCharacter> characters=response.jsonPath().getList("",HPCharacter.class);
//        characters.forEach(System.out::println);
//        List<String> characters=response.jsonPath().getList("_id");
        Assertions.assertEquals(194,characters.size(),"There isnt 194 characters");
    }
    @Test
    @DisplayName("Verify number of character id and house")
    public void verifyCharacters(){
        Response response= given()
                .spec(reqSpec)
                .when()
                .get("/characters")
                .then()
                .assertThat()
                .statusCode(200).and()
                .contentType("application/json; charset=utf-8").extract().response();

        String[] houses={"Gryffindor", "Ravenclaw", "Slytherin", "Hufflepuff"};

        List<String> idList=response.jsonPath().getList("_id");
        Assertions.assertFalse(idList.contains(""),"There is an empty id");

        List<Boolean> dumbledoresArmy=response.jsonPath().getList("dumbledoresArmy");
        Assertions.assertEquals(idList.size(),dumbledoresArmy.size(),"dumbledoresArmy is not boolean in all chararcters");
        List<String> houseList=response.jsonPath().getList("house");
        //houseList.removeIf(each->)
        houseList.forEach(each->Assertions.assertTrue(Arrays.asList(houses).contains(each),"House is not as expected, house name :"+each));
    }

    @Test
    @DisplayName("Verify all character information")
    public void verifyAllCharacterInfo(){
        Response response= given()
                .spec(reqSpec)
                .when()
                .get("/characters")
                .then()
                .assertThat()
                .statusCode(200).and()
                .contentType("application/json; charset=utf-8").extract().response();
        List<Map<String,Object>> allCharacters=response.jsonPath().get("");
        //allCharacters.forEach(System.out::println);
        Random rand=new Random();
        int randomIndex=rand.nextInt(allCharacters.size()-1);
        //System.out.println(allCharacters.get(randomIndex));
        String randomName=(String)allCharacters.get(randomIndex).get("name");

        Response response1= given()
                .spec(reqSpec)
                .queryParam("name",randomName)
                .when()
                .get("/characters")
                .then()
                .assertThat()
                .statusCode(200).and()
                .contentType("application/json; charset=utf-8").extract().response();

        Map<String,Object> charX=response1.jsonPath().getMap("[0]");
        Assertions.assertEquals(allCharacters.get(randomIndex),charX,"All character information is not same");
    //    System.out.println(charX);
    }

    @Test
    @DisplayName("Verify name search")
    public void verifyNameSearch(){
         given()
                .spec(reqSpec)
                .queryParam("name","Harry Potter")
                .when()
                .get("/characters")
                .then()
                .assertThat()
                .statusCode(200).and()
                .contentType("application/json; charset=utf-8").and()
                 .body("[0].name",is("Harry Potter"));

        given()
                .spec(reqSpec)
                .queryParam("name","Marry Potter")
                .when()
                .get("/characters")
                .then()
                .assertThat()
                .statusCode(200).and()
                .contentType("application/json; charset=utf-8").and()
                .body(is("[]"));
    }

    @Test
    @DisplayName("Verify house members")
    public void verifyHouseMembers(){
        Response response=given()
                .spec(reqSpec)
                .when()
                .get("/houses")
                .then()
                .assertThat()
                .statusCode(200).and()
                .contentType("application/json; charset=utf-8").and().extract().response();
        List<Map<String,?>> houses=response.jsonPath().get();
        String id="";
        List<String> houseMembers=new ArrayList<>();
        for(Map<String,?> house:houses){
            if(house.get("name").equals("Gryffindor")) {
                id = (String) house.get("_id");
                houseMembers=(List<String>) house.get("members");
            }
        }
//        System.out.println("id = " + id);
//        System.out.println(houseMembers);

        Response response1=given()
                .spec(reqSpec)
                .pathParam("id",id)
                .when()
                .get("/houses/{id}")
                .then()
                .assertThat()
                .statusCode(200).and()
                .contentType("application/json; charset=utf-8").and().extract().response();

        List<String> actualMembers=response1.jsonPath().getList("members._id[0]");
//        System.out.println("houseMembers = " + houseMembers);
//        System.out.println("actualMembers = " + actualMembers);
        Assertions.assertEquals(houseMembers,actualMembers,"House members are not same");
    }
    @Test
    @DisplayName("Verify house members again")
    public void verifyHouseNumberAgain(){
        Response response=given()
                .spec(reqSpec)
                .pathParam("id","5a05e2b252f721a3cf2ea33f")
                .when()
                .get("/houses/{id}")
                .then()
                .assertThat()
                .statusCode(200).and()
                .contentType("application/json; charset=utf-8").and().extract().response();
        List<String> houseMembers=response.jsonPath().getList("members._id[0]");
       // System.out.println("houseMembers = " + houseMembers);

        Response response1= given()
                .spec(reqSpec)
                .queryParam("house","Gryffindor")
                .when()
                .get("/characters")
                .then()
                .assertThat()
                .statusCode(200).and()
                .contentType("application/json; charset=utf-8").extract().response();

        List<String> actualMembers=response1.jsonPath().getList("_id");
        Assertions.assertEquals(houseMembers,actualMembers,"Responses doesn't contain same members, expected: "+houseMembers.size()+" actual: "+actualMembers.size());
    }

    @Test
    @DisplayName("Verify house with most members")
    public void verifyBiggestHouse(){
        Response response=given()
                .spec(reqSpec)
                .when()
                .get("/houses")
                .then()
                .assertThat()
                .statusCode(200).and()
                .contentType("application/json; charset=utf-8").extract().response();

        List<Map<String,?>> houses=response.jsonPath().get();
        List<String> houseMembers=new ArrayList<>();
        Map<String,Integer> memberCounts=new HashMap<>();
        for(Map<String,?> house:houses){
            houseMembers=(List<String>) house.get("members");
            memberCounts.put((String)house.get("name"),houseMembers.size());
        }
        System.out.println(memberCounts);
        int max=0;
        String biggestHouseName="";
        for(String key:memberCounts.keySet()){
            if(memberCounts.get(key)>max){
                max=memberCounts.get(key);
                biggestHouseName=key;
            }
        }
        System.out.println("biggestHouseName = " + biggestHouseName);
        Assertions.assertEquals("Gryffindor",biggestHouseName,"Gryffindor doesn't have the most members");
    }
}
