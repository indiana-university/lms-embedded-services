package edu.iu.uits.lms.iuonly;

/*-
 * #%L
 * lms-canvas-iu-custom-services
 * %%
 * Copyright (C) 2015 - 2025 Indiana University
 * %%
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the Indiana University nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

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
        Assertions.assertFalse(AuthorizedUserService.convertPropertyToBoolean("0"));
        Assertions.assertFalse(AuthorizedUserService.convertPropertyToBoolean("1"));

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
