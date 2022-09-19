import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Configuration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openqa.selenium.Keys;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Calendar;
import java.util.Date;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$x;
import static com.codeborne.selenide.Selenide.open;

public class CardDeliveryTest {

    String dateSetup(int dayAddition) {
        DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) + dayAddition);
        Date date = cal.getTime();
        return dateFormat.format(date);
    }

    @ParameterizedTest
    @CsvSource({
            "3, 'Шойгу Сергей'",
            "4, 'Шойгу Сергей'",
            "1337, 'Шойгу Сергей'",
            "1337, 'Шойгу-Сергей'",
            "1337, 'ШойгуСергей'",
            "1337, 'Шой-гуСергей'"
    })
    void shouldReserveOnHappyPath(int dayLag, String name) {
        Configuration.holdBrowserOpen = true;
        String reserveDate = dateSetup(dayLag);

        open("http://localhost:9999");
        $("span[data-test-id='city'] input").setValue("Кызыл");
        $("span[data-test-id='date'] input").doubleClick().sendKeys(Keys.DELETE);
        $("span[data-test-id='date'] input").setValue(reserveDate);
        $("span[data-test-id='name'] input").setValue(name);
        $("span[data-test-id='phone'] input").setValue("+74956968800");
        $x("//label[@data-test-id='agreement']/span").click();
        $("button .button__text").click();
        $("div[data-test-id='notification']").should(Condition.visible, Duration.ofSeconds(15));

        String notification__title = "Успешно!";
        String notification__content = "Встреча успешно забронирована на " + reserveDate;
        Assertions.assertEquals(notification__title, $("div.notification__title").should(Condition.visible, Duration.ofSeconds(15)).getText().trim());
        Assertions.assertEquals(notification__content, $("div.notification__content").should(Condition.visible, Duration.ofSeconds(15)).getText().trim());
    }

    @Test
    void shouldFailWhenNotFromRegionalCenter() {
        Configuration.holdBrowserOpen = true;
        String reserveDate = dateSetup(5);

        open("http://localhost:9999");
        $("span[data-test-id='city'] input").setValue("Северосибирск");
        $("span[data-test-id='date'] input").doubleClick().sendKeys(Keys.DELETE);
        $("span[data-test-id='date'] input").setValue(reserveDate);
        $("span[data-test-id='name'] input").setValue("Гудман Сол");
        $("span[data-test-id='phone'] input").setValue("+15058425662");
        $x("//label[@data-test-id='agreement']/span").click();
        $("button .button__text").click();

        String expected = "Доставка в выбранный город недоступна";
        Assertions.assertEquals(expected, $("span[data-test-id='city'].input_invalid .input__sub").getText().trim());
        $("div[data-test-id='notification']").should(Condition.hidden, Duration.ofSeconds(15));
    }

    @ParameterizedTest
    @CsvSource({
            "Saul Goodman",
            "Гудман; Cол",
            "Гудман С0л"
    })
    void shouldFailWhenInappropriateName(String name) {
        Configuration.holdBrowserOpen = true;
        String reserveDate = dateSetup(5);

        open("http://localhost:9999");
        $("span[data-test-id='city'] input").setValue("Южно-Сахалинск");
        $("span[data-test-id='date'] input").doubleClick().sendKeys(Keys.DELETE);
        $("span[data-test-id='date'] input").setValue(reserveDate);
        $("span[data-test-id='name'] input").setValue(name);
        $("span[data-test-id='phone'] input").setValue("+15058425662");
        $x("//label[@data-test-id='agreement']/span").click();
        $("button .button__text").click();

        String expected = "Имя и Фамилия указаные неверно. Допустимы только русские буквы, пробелы и дефисы.";
        Assertions.assertEquals(expected, $("span[data-test-id='name'].input_invalid .input__sub").getText().trim());
        $("div[data-test-id='notification']").should(Condition.hidden, Duration.ofSeconds(15));
    }

    @ParameterizedTest
    @CsvSource({
            "79268873971",
            "499740274038",
            "+4997402740",
            "+499740274038",
            "+susamogus00",
            "+7578-32-22",
    })
    void shouldFailWhenInappropriatePhone(String phone) {
        Configuration.holdBrowserOpen = true;
        String reserveDate = dateSetup(5);

        open("http://localhost:9999");
        $("span[data-test-id='city'] input").setValue("Южно-Сахалинск");
        $("span[data-test-id='date'] input").doubleClick().sendKeys(Keys.DELETE);
        $("span[data-test-id='date'] input").setValue(reserveDate);
        $("span[data-test-id='name'] input").setValue("Ямасаки Хако");
        $("span[data-test-id='phone'] input").setValue(phone);
        $x("//label[@data-test-id='agreement']/span").click();
        $("button .button__text").click();

        String expected = "Телефон указан неверно. Должно быть 11 цифр, например, +79012345678.";
        Assertions.assertEquals(expected, $("span[data-test-id='phone'].input_invalid .input__sub").getText().trim());
        $("div[data-test-id='notification']").should(Condition.hidden, Duration.ofSeconds(15));
    }

    @ParameterizedTest
    @CsvSource({
            "2",
            "1",
            "0",
            "-1",
            "-42"
    })
    void shouldFailWhenTooEarlyDate(int dayLag) {
        Configuration.holdBrowserOpen = true;
        String reserveDate = dateSetup(dayLag);

        open("http://localhost:9999");
        $("span[data-test-id='city'] input").setValue("Южно-Сахалинск");
        $("span[data-test-id='date'] input").doubleClick().sendKeys(Keys.DELETE);
        $("span[data-test-id='date'] input").setValue(reserveDate);
        $("span[data-test-id='name'] input").setValue("Гудман Сол");
        $("span[data-test-id='phone'] input").setValue("+15058425662");
        $x("//label[@data-test-id='agreement']/span").click();
        $("button .button__text").click();

        String expected = "Заказ на выбранную дату невозможен";
        Assertions.assertEquals(expected, $("span[data-test-id='date'] .input_invalid .input__sub").getText().trim());
        $("div[data-test-id='notification']").should(Condition.hidden, Duration.ofSeconds(15));
    }

    @Test
    void shouldFailWhenNoAgreement() {
        Configuration.holdBrowserOpen = true;
        String reserveDate = dateSetup(5);

        open("http://localhost:9999");
        $("span[data-test-id='city'] input").setValue("Биробиджан");
        $("span[data-test-id='date'] input").doubleClick().sendKeys(Keys.DELETE);
        $("span[data-test-id='date'] input").setValue(reserveDate);
        $("span[data-test-id='name'] input").setValue("Уотерс Роджер");
        $("span[data-test-id='phone'] input").setValue("+15058425662");
        $("button .button__text").click();

        Assertions.assertTrue($("label[data-test-id='agreement'].input_invalid").isDisplayed());
        $("div[data-test-id='notification']").should(Condition.hidden, Duration.ofSeconds(15));
    }

    @ParameterizedTest
    @CsvSource({
            "03.15.2099",
            "2099.15.03",
            "15.03.99",
            "03 15 2099",
            "0",
            "null",
            "amogus",
            "сус"
    })
    void shouldFailWhenInappropriateDateFormat(String reserveDate) {
        Configuration.holdBrowserOpen = true;

        open("http://localhost:9999");
        $("span[data-test-id='city'] input").setValue("Южно-Сахалинск");
        $("span[data-test-id='date'] input").doubleClick().sendKeys(Keys.DELETE);
        $("span[data-test-id='date'] input").setValue(reserveDate);
        $("span[data-test-id='name'] input").setValue("Гудман Сол");
        $("span[data-test-id='phone'] input").setValue("+15058425662");
        $x("//label[@data-test-id='agreement']/span").click();
        $("button .button__text").click();

        String expected = "Неверно введена дата";
        Assertions.assertEquals(expected, $("span[data-test-id='date'] .input_invalid .input__sub").getText().trim());
        $("div[data-test-id='notification']").should(Condition.hidden, Duration.ofSeconds(15));
    }

    @Test
    void shouldFailWhenEmptyCityName() {
        Configuration.holdBrowserOpen = true;
        String reserveDate = dateSetup(5);

        open("http://localhost:9999");
        $("span[data-test-id='date'] input").doubleClick().sendKeys(Keys.DELETE);
        $("span[data-test-id='date'] input").setValue(reserveDate);
        $("span[data-test-id='name'] input").setValue("Гудман Сол");
        $("span[data-test-id='phone'] input").setValue("+15058425662");
        $x("//label[@data-test-id='agreement']/span").click();
        $("button .button__text").click();

        String expected = "Поле обязательно для заполнения";
        Assertions.assertEquals(expected, $("span[data-test-id='city'].input_invalid .input__sub").getText().trim());
        $("div[data-test-id='notification']").should(Condition.hidden, Duration.ofSeconds(15));
    }

    @Test
    void shouldFailWhenEmptyName() {
        Configuration.holdBrowserOpen = true;
        String reserveDate = dateSetup(5);

        open("http://localhost:9999");
        $("span[data-test-id='city'] input").setValue("Южно-Сахалинск");
        $("span[data-test-id='date'] input").doubleClick().sendKeys(Keys.DELETE);
        $("span[data-test-id='date'] input").setValue(reserveDate);
        $("span[data-test-id='phone'] input").setValue("+15058425662");
        $x("//label[@data-test-id='agreement']/span").click();
        $("button .button__text").click();

        String expected = "Поле обязательно для заполнения";
        Assertions.assertEquals(expected, $("span[data-test-id='name'].input_invalid .input__sub").getText().trim());
        $("div[data-test-id='notification']").should(Condition.hidden, Duration.ofSeconds(15));
    }

    @Test
    void shouldFailWhenEmptyPhone() {
        String reserveDate = dateSetup(5);

        open("http://localhost:9999");
        $("span[data-test-id='city'] input").setValue("Южно-Сахалинск");
        $("span[data-test-id='date'] input").doubleClick().sendKeys(Keys.DELETE);
        $("span[data-test-id='date'] input").setValue(reserveDate);
        $("span[data-test-id='name'] input").setValue("Ямасаки Хако");
        $x("//label[@data-test-id='agreement']/span").click();
        $("button .button__text").click();

        String expected = "Поле обязательно для заполнения";
        Assertions.assertEquals(expected, $("span[data-test-id='phone'].input_invalid .input__sub").getText().trim());
        $("div[data-test-id='notification']").should(Condition.hidden, Duration.ofSeconds(15));
    }
}
