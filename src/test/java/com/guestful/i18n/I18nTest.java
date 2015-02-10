/**
 * Copyright (C) 2013 Guestful (info@guestful.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.guestful.i18n;

import com.guestful.json.groovy.GroovyJsonMapper;
import com.guestful.jsr310.groovy.GroovyJsr310;
import groovy.json.JsonOutput;
import org.codehaus.groovy.runtime.ResourceGroovyMethods;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

/**
 * date 2014-06-17
 *
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
@RunWith(JUnit4.class)
public class I18nTest {

    @Test
    public void verify() throws IOException {
        Map<String, Country> map = Internationalization.load();

        GroovyJsonMapper jsonMapper = new GroovyJsonMapper();
        GroovyJsr310.addJsr310EncodingHook(jsonMapper.getSerializer());

        ResourceGroovyMethods.setText(new File("target/i18n.json"), JsonOutput.prettyPrint(jsonMapper.toJson(map)));

        List<Country> list = map.values().stream().filter(c -> c.getTimeZones().isEmpty()).collect(Collectors.toList());
        System.out.println(list);
        Assert.assertTrue(list.isEmpty());
    }

    @Test
    public void phones() throws Exception {
        assertEquals("+33 0478509846 x456", Internationalization.parsePhoneNumber("04 78 50 98 46 x 456", "FR").toString());
        assertEquals("+33478509846", Internationalization.parsePhoneNumber("04 78 50 98 46 x 456", "FR").format());

        assertEquals("+1 5146604287", Internationalization.parsePhoneNumber("514 660 4287", "CA").toString());
        assertEquals("+15146604287", Internationalization.parsePhoneNumber("514 660 4287", "CA").format());

        assertEquals("+1 5146604287", Internationalization.parsePhoneNumber("+1 514 660 4287", "FR").toString());
        assertEquals("+15146604287", Internationalization.parsePhoneNumber("+1 514 660 4287", "FR").format());
        assertEquals("+15146604287", Internationalization.parsePhoneNumber("+1 514+660+4287", "FR").format());

        assertEquals("+1 5146604287", Internationalization.parsePhoneNumber("+999 514 660 4287", "CA").toString());
        assertEquals("+15146604287", Internationalization.parsePhoneNumber("+999 514 660 4287", "CA").format());
        assertEquals("+15146604287", Internationalization.parsePhoneNumber("+999+514+660+4287", "CA").format());

        assertEquals("+33 05146604287", Internationalization.parsePhoneNumber("099 514 660 4287", "FR").toString());
        assertEquals("+335146604287", Internationalization.parsePhoneNumber("099 514 660 4287", "FR").format());
    }

}
