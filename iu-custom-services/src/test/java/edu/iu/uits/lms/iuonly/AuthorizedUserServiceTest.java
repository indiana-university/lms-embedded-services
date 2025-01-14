package edu.iu.uits.lms.iuonly;

import edu.iu.uits.lms.iuonly.services.AuthorizedUserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class AuthorizedUserServiceTest {

    @Test
    void testConvertPropertyToBoolean() {
        Assertions.assertFalse(AuthorizedUserService.convertPropertyToBoolean(null));
        Assertions.assertFalse(AuthorizedUserService.convertPropertyToBoolean(""));
        Assertions.assertFalse(AuthorizedUserService.convertPropertyToBoolean("asdf"));
        Assertions.assertFalse(AuthorizedUserService.convertPropertyToBoolean("false"));
        Assertions.assertFalse(AuthorizedUserService.convertPropertyToBoolean("False"));
        Assertions.assertFalse(AuthorizedUserService.convertPropertyToBoolean("FALSE"));
        Assertions.assertFalse(AuthorizedUserService.convertPropertyToBoolean("no"));
        Assertions.assertFalse(AuthorizedUserService.convertPropertyToBoolean("yes"));

        Assertions.assertTrue(AuthorizedUserService.convertPropertyToBoolean("true"));
        Assertions.assertTrue(AuthorizedUserService.convertPropertyToBoolean("True"));
        Assertions.assertTrue(AuthorizedUserService.convertPropertyToBoolean("TRUE"));
    }

    @Test
    void testConvertPropertyToStringArray() {
        String[] empty = new String[]{};
        String[] result = AuthorizedUserService.convertPropertyToStringArray(null);
        Assertions.assertArrayEquals(empty, result);

        String[] singleEmpty = new String[]{ "" };
        result = AuthorizedUserService.convertPropertyToStringArray("");
        Assertions.assertArrayEquals(singleEmpty, result);

        String[] singleItem = new String[] { "asdf" };
        result = AuthorizedUserService.convertPropertyToStringArray("asdf");
        Assertions.assertArrayEquals(singleItem, result);

        result = AuthorizedUserService.convertPropertyToStringArray("   asdf   ");
        Assertions.assertArrayEquals(singleItem, result);

        String[] twoItems = new String[] { "asdf", "qwerty" };
        result = AuthorizedUserService.convertPropertyToStringArray("asdf,qwerty");
        Assertions.assertArrayEquals(twoItems, result);

        result = AuthorizedUserService.convertPropertyToStringArray("  asdf  ,  qwerty  ");
        Assertions.assertArrayEquals(twoItems, result);


        String[] threeItems = new String[] { "asdf", "foo bar", "qwerty" };
        result = AuthorizedUserService.convertPropertyToStringArray("asdf,foo bar,qwerty");
        Assertions.assertArrayEquals(threeItems, result);

        result = AuthorizedUserService.convertPropertyToStringArray("  asdf  ,  foo bar    ,    qwerty  ");
        Assertions.assertArrayEquals(threeItems, result);
    }

    @Test
    void testConvertPropertyToList() {
        List<String> empty = new ArrayList<>();
        List<String> result = AuthorizedUserService.convertPropertyToList(null);
        Assertions.assertEquals(empty, result);

        List<String> singleEmpty = List.of("");
        result = AuthorizedUserService.convertPropertyToList("");
        Assertions.assertEquals(singleEmpty, result);

        List<String> singleItem = List.of("asdf");
        result = AuthorizedUserService.convertPropertyToList("asdf");
        Assertions.assertEquals(singleItem, result);

        result = AuthorizedUserService.convertPropertyToList("   asdf   ");
        Assertions.assertEquals(singleItem, result);

        List<String> twoItems = List.of("asdf", "qwerty");
        result = AuthorizedUserService.convertPropertyToList("asdf,qwerty");
        Assertions.assertEquals(twoItems, result);

        result = AuthorizedUserService.convertPropertyToList("  asdf  ,  qwerty  ");
        Assertions.assertEquals(twoItems, result);

        List<String> threeItems = List.of("asdf", "foo bar", "qwerty");
        result = AuthorizedUserService.convertPropertyToList("asdf,foo bar,qwerty");
        Assertions.assertEquals(threeItems, result);

        result = AuthorizedUserService.convertPropertyToList("  asdf  ,  foo bar    ,    qwerty  ");
        Assertions.assertEquals(threeItems, result);
    }
}
