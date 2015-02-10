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

import java.util.regex.Pattern;

/**
 * date 2014-06-17
 *
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public class PhoneNumber {

    private final static Pattern PATTERN = Pattern.compile("\\+?\\d{8,}(x\\d+)?");

    private final Country country;
    private final String phoneNumber;
    private final String extension;

    PhoneNumber(Country country, String phoneNumber, String extension) {
        // normalize phone number
        if ("FR".equals(country.getCode()) && phoneNumber.startsWith("0")) {
            // In france, 04 78 12 13 14 translates to +33 4 78 12 13 14
            phoneNumber = phoneNumber.substring(1);
        }
        this.country = country;
        this.phoneNumber = phoneNumber;
        this.extension = extension;
    }

    public String getCountryPrefix() {
        return country.getPhonePrefix();
    }

    public String getCountryCode() {
        String s = country.getPhonePrefix();
        return s.length() == 0 ? s : s.substring(1);
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getExtension() {
        return extension;
    }

    public String format() {
        return getCountryPrefix() + getPhoneNumber();
    }

    public String getFormatedPhoneNumber() {
        if ("FR".equals(country.getCode())) {
            // In france, 04 78 12 13 14 translates to +33 4 78 12 13 14
            return "0" + phoneNumber;
        }
        return phoneNumber;
    }

    public String getFormatedExtension() {
        return extension.length() == 0 ? "" : "x" + extension;
    }

    @Override
    public String toString() {
        return (getCountryPrefix() + " " + getFormatedPhoneNumber() + " " + getFormatedExtension()).trim();
    }

    public static String normalize(String s) {
        String phone = s.toLowerCase().replaceAll("[^x\\d\\+]", "");
        String phone2 = phone.replace("+", "");
        return phone.startsWith("+") ? ("+" + phone2) : phone2;
    }

    public static boolean isNormalized(String s) {
        return PATTERN.matcher(s).matches();
    }

}
