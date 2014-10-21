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

import java.time.ZoneId;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

/**
 * date 2014-06-17
 *
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public class Country {

    private static Pattern ANY_POSTAL_CODE = Pattern.compile("[A-Z0-9]+", Pattern.CASE_INSENSITIVE);

    private final String code;
    private final String name;
    private final Map<String, State> states = new TreeMap<>();
    private final Collection<ZoneId> timeZones = new HashSet<>();
    private Pattern postalCodePattern = ANY_POSTAL_CODE;
    private String phonePrefix = "";

    public Country(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public Map<String, State> getStates() {
        return states;
    }

    public State getState(String code) {
        return states.get(code);
    }

    public Collection<ZoneId> getTimeZones() {
        return timeZones;
    }

    public ZoneId getFirstTimeZone() {
        return timeZones.iterator().next();
    }

    public Pattern getPostalCodePattern() {
        return postalCodePattern;
    }

    public void setPostalCodePattern(Pattern postalCodePattern) {
        this.postalCodePattern = postalCodePattern;
    }

    public String getPhonePrefix() {
        return phonePrefix;
    }

    public void setPhonePrefix(String phonePrefix) {
        this.phonePrefix = phonePrefix;
    }

    public State addState(String code, String name) {
        State state = new State(code, name);
        states.put(state.getCode(), state);
        return state;
    }

    @Override
    public String toString() {
        return code + " (" + name + ")";
    }

}
