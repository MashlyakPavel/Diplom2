import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.apache.http.HttpStatus.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class UserCreateTest {
    private BurgerUser burgerUser;
    private ValidatableResponse response;

    @Before
    public void setUp() {
        RestAssured.baseURI = BurgerEndpoints.apiBasicURL;
    }

    @After
    public void cleanUp() {
        //Очистка тестовых данных
        response = burgerUser.apiUserDelete();
    }

    // Положительный тест
    // Пользователь успешно создан

    @Test
    @DisplayName("Create User. Happy path")
    @Description("Data provided, response code 200, Login successful")
    public void userCreateSuccessTest() {
        // Инициализация тестовых данных
        burgerUser = new BurgerUser();

        // Тестовый запуск
        ValidatableResponse response = burgerUser.apiUserCreate();

        //Проверка кода состояния
        int statusCode = response.extract().statusCode();
        Assert.assertEquals("User creation failed. Response code is incorrect " ,SC_OK, statusCode);

        //Проверка тела ответа
        boolean isUserCreated = response.extract().path("success");
        assertTrue("User creation failed. Body content is incorrect", isUserCreated );

        //Верификация курьера через логин
        response = burgerUser.apiUserLogin();

        Assert.assertEquals("User creation is not successful. Login attempt is failed.", SC_OK, response.extract().statusCode());

    }

    // Отрицательные тесты
    // Пользовательский дубликат отклонен
    @Test
    @DisplayName("Create User duplicate")
    @Description("Create User two times, response code 403")
    public void userCreateDuplicateTest() {
        // Инициализация тестовых данных
        burgerUser = new BurgerUser();

        // Тестовый запуск - создание пользователя в первый раз
        ValidatableResponse response = burgerUser.apiUserCreate();

        //Убедитесь, что курьер создан
        int statusCode = response.extract().statusCode();
        Assert.assertEquals("User creation failed. Response code is incorrect",SC_OK, statusCode);

        // Тестовый запуск - создание курьера во второй раз
        response = burgerUser.apiUserCreate();

        //Проверка код состояния
        statusCode = response.extract().statusCode();
        Assert.assertEquals("User duplication check failed. Response code is incorrect", SC_FORBIDDEN, statusCode);

        //Проверка текста ответа
        String strExpected = "User already exists";
        String strResponse = response.extract().path("message");
        Assert.assertEquals("User duplication check failed. Response message is different", strExpected, strResponse);
    }

    // Недостаточно пользовательских данных: email
    @Test
    @DisplayName("Create User - no login")
    @Description("Create User without login, response code 403")
    public void userCreateNoLoginTest(){
        // Инициализация тестовых данных
        burgerUser = new BurgerUser(null, "123123", "oa");

        // Тестовый запуск
        ValidatableResponse response = burgerUser.apiUserCreate(true);

        //Проверка кода состояния
        int statusCode = response.extract().statusCode();
        Assert.assertEquals("User rejection failed. Response code is incorrect",SC_FORBIDDEN , statusCode);

        //Проверка тела ответа
        String strExpected = "Email, password and name are required fields";
        String strResponse = response.extract().path("message");
        assertEquals("User rejection failed. Body content is incorrect", strExpected, strResponse );

    }

    // Недостаточно пользовательских данных: Пароль
    @Test
    @DisplayName("Create user - no password")
    @Description("Create user without password, response code 403")
    public void userCreateNoPasswordTest(){
        // Инициализация тестовых данных
        burgerUser = new BurgerUser("pavel@ya.ru", null, "oa");

        // Tестовый запуск
        ValidatableResponse response = burgerUser.apiUserCreate(true);

        //Проверка кода состояния
        int statusCode = response.extract().statusCode();
        Assert.assertEquals("User rejection failed. Response code is incorrect",SC_FORBIDDEN, statusCode);

        //Проверка тела ответа
        String strExpected = "Email, password and name are required fields";
        String strResponse = response.extract().path("message");
        assertEquals("Courier rejection failed. Body content is incorrect", strExpected, strResponse );

    }

    // Недостаточно пользовательских данных: Имя
    @Test
    @DisplayName("Create User - no Name")
    @Description("Create User without Name should be rejected, response code 403")
    public void userCreateNoFirstNameTest(){
        // Инициализация тестовых данных
        burgerUser = new BurgerUser("pavel@ya.ru", "123123", null);

        // Тестовый запуск
        ValidatableResponse response = burgerUser.apiUserCreate(true);

        //Проверка кода состояния
        int statusCode = response.extract().statusCode();
        Assert.assertEquals("Courier creation failed. Response code is incorrect", SC_FORBIDDEN, statusCode);

        //Проверка тела ответа
        String strExpected = "Email, password and name are required fields";
        String strResponse = response.extract().path("message");
        Assert.assertEquals("User . Body content is incorrect", strExpected, strResponse );
    }

}