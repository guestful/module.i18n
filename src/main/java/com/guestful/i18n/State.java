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

/**
 * date 2014-06-17
 *
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public class State {

    private final String code;
    private final String name;
    private final Collection<ZoneId> timeZones = new HashSet<>();

    public State(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public void addTimeZone(String id) {
        timeZones.add(ZoneId.of(id));
    }

    public Collection<ZoneId> getTimeZones() {
        return timeZones;
    }

    public ZoneId getFirstTimeZone() {
        return timeZones.iterator().next();
    }

    @Override
    public String toString() {
        return code + " (" + name + ")";
    }

}
