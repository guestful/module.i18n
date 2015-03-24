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

import javax.json.*;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.ZoneId;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Mathieu Carbou (mathieu.carbou@gmail.com)
 */
public class Internationalization {

    //private static final List<Locale> SUPPORTED_LOCALES = Collections.unmodifiableList(Arrays.asList(Locale.ENGLISH, Locale.FRENCH));
    public static final List<String> SUPPORTED_LANGS = Collections.unmodifiableList(Arrays.asList(Locale.getISOLanguages()));

    private static final Map<String, Country> COUNTRIES = load();

    public static Collection<String> getTimeZones() {
        return ZoneId.getAvailableZoneIds();
    }

    public static String getTimeZone(String countryCode) {
        return getTimeZone(countryCode, null, null);
    }

    public static String getTimeZone(String countryCode, String stateCode) {
        return getTimeZone(countryCode, stateCode, null);
    }

    public static String getTimeZone(String countryCode, String stateCode, String asked) {
        ZoneId choosed = asked == null ? null : ZoneId.of(asked);
        ZoneId resolved = null;
        if (countryCode != null) {
            Country country = COUNTRIES.get(countryCode);
            if (country != null) {
                if (stateCode != null) {
                    State state = country.getState(stateCode);
                    if (state != null && !state.getTimeZones().isEmpty()) {
                        if (choosed == null || state.getTimeZones().size() == 1) {
                            resolved = state.getFirstTimeZone();
                        } else if (state.getTimeZones().contains(choosed)) {
                            resolved = choosed;
                        } else {
                            resolved = state.getFirstTimeZone();
                        }
                    }
                }
                if (resolved == null && !country.getTimeZones().isEmpty()) {
                    if (choosed == null || country.getTimeZones().size() == 1) {
                        resolved = country.getFirstTimeZone();
                    } else if (country.getTimeZones().contains(choosed)) {
                        resolved = choosed;
                    } else {
                        resolved = country.getFirstTimeZone();
                    }
                }
            }
        }
        if (resolved == null) {
            resolved = choosed;
        }
        return resolved == null ? null : resolved.getId();
    }

    public static String getCountryName(String countryCode, Locale language) {
        if (countryCode == null) return "";
        Locale locale = new Locale("", countryCode);
        return locale.getDisplayCountry(language);
    }

    public static Collection<String> getCountryCodes() {
        return Arrays.asList(Locale.getISOCountries());
    }

    public static Collection<String> getStateCodes(String countryCode) {
        if (countryCode == null) return Collections.emptyList();
        Country country = COUNTRIES.get(countryCode);
        if (country == null) return Collections.emptyList();
        return country.getStates().keySet();
    }

    public static String getStateName(String countryCode, String stateCode) {
        if (countryCode == null || stateCode == null) return "";
        Country country = COUNTRIES.get(countryCode);
        if (country == null) return "";
        State state = country.getState(stateCode);
        return state == null ? "" : state.getName();
    }

    public static Pattern getPostalCodePattern(String countryCode) {
        if (countryCode == null || !COUNTRIES.containsKey(countryCode)) {
            throw new IllegalArgumentException("Unsupported country code: " + countryCode);
        }
        return COUNTRIES.get(countryCode).getPostalCodePattern();
    }

    public static boolean isPostalCodeValid(String countryCode, String postalCode) {
        return postalCode != null && getPostalCodePattern(countryCode).matcher(postalCode).matches();
    }

    public static String getMessage(Map<String, ?> bundle, Locale locale, Locale defaultLocale) {
        return getMessage(bundle, locale, defaultLocale, "; ");
    }

    public static String getMessage(Map<String, ?> bundle, Locale locale, Locale defaultLocale, String join) {
        if (bundle == null) return null;
        if (locale == null) locale = defaultLocale;
        if (locale == null) return null;
        Object o = bundle.get(locale.getLanguage());
        if (o == null) {
            o = bundle.get(defaultLocale.getLanguage());
        }
        if (o == null) return null;
        if (!(o instanceof Collection)) return String.valueOf(o);
        return String.join(join, ((Collection<?>) o).stream().map(String::valueOf).collect(Collectors.toList()));
    }

    public static List<String> getPhonePrefixes() {
        return COUNTRIES.values().stream().map(Country::getPhonePrefix).filter(p -> p.length() > 0).distinct().sorted().collect(Collectors.toList());
    }

    public static String getPhonePrefix(String countryCode) {
        if (countryCode == null) return "";
        Country country = COUNTRIES.get(countryCode);
        if (country == null) return "";
        return country.getPhonePrefix();
    }

    public static Country getCountry(String countryCode) {
        if (countryCode == null) throw new NullPointerException();
        Country country = COUNTRIES.get(countryCode);
        if (country == null) {
            throw new IllegalArgumentException(countryCode);
        }
        return country;
    }

    public static PhoneNumber parsePhoneNumber(String phoneNumber, String phoneCode, String defaultCountryCode) {
        final String phonePrefix;
        if (phoneCode != null && phoneCode.length() > 0 && !phoneCode.startsWith("+")) {
            phonePrefix = "+" + phoneCode;
        } else {
            phonePrefix = phoneCode;
        }

        if (!PhoneNumber.isNormalized(phoneNumber)) {
            phoneNumber = PhoneNumber.normalize(phoneNumber);
        }

        // parse extension
        String extension = "";
        int p_ext = phoneNumber.indexOf('x');
        if (p_ext >= 0) {
            extension = phoneNumber.substring(p_ext + 1);
            phoneNumber = phoneNumber.substring(0, p_ext);
        }

        // find country
        Country country;
        Optional<Country> o = COUNTRIES.values().stream().filter(c -> c.getPhonePrefix().equals(phonePrefix)).findFirst();
        if (o.isPresent()) {
            country = o.get();
        } else {
            // could the addition of + be a mistake, and also the eventually starting country code ?
            // Only keep the last 1 digits
            country = getCountry(defaultCountryCode);
        }

        return new PhoneNumber(country, phoneNumber, extension);
    }

    public static PhoneNumber parsePhoneNumber(String phoneNumber, String defaultCountryCode) {
        if (!PhoneNumber.isNormalized(phoneNumber)) {
            phoneNumber = PhoneNumber.normalize(phoneNumber);
        }

        // parse extension
        String extension = "";
        int p_ext = phoneNumber.indexOf('x');
        if (p_ext >= 0) {
            extension = phoneNumber.substring(p_ext + 1);
            phoneNumber = phoneNumber.substring(0, p_ext);
        }

        // find country
        Country country;
        if (phoneNumber.length() > 10 && !phoneNumber.startsWith("+")) {
            // user probably entered its phone number without the "+" sign but with the international prefix
            phoneNumber = "+" + phoneNumber;
        }
        if (phoneNumber.startsWith("+")) {
            final String phone = phoneNumber;
            List<String> prefixes = Arrays.asList(2, 3, 4).stream().map(end -> phone.substring(0, end)).collect(Collectors.toList());
            Optional<Country> o = COUNTRIES.values().stream().filter(c -> prefixes.contains(c.getPhonePrefix())).findFirst();
            if (o.isPresent()) {
                country = o.get();
                phoneNumber = phoneNumber.substring(country.getPhonePrefix().length());
            } else {
                // could the addition of + be a mistake, and also the eventually starting country code ?
                // Only keep the last 1 digits
                country = getCountry(defaultCountryCode);
                phoneNumber = phoneNumber.substring(Integer.max(phoneNumber.length(), 11) - 10);
            }
        } else {
            country = getCountry(defaultCountryCode);
        }

        return new PhoneNumber(country, phoneNumber, extension);
    }

    public static List<String> getLocalizedPaths(String path, Locale locale) {
        ArrayList<String> list = new ArrayList<>(3);
        StringBuilder templateName = new StringBuilder(path);
        int pos = templateName.lastIndexOf(".");
        if (pos == -1) {
            throw new IllegalArgumentException("Illegal path: extension needed");
        }
        List<String> locales = Arrays.asList("_" + locale, "_" + locale.getLanguage(), "");
        int prev = 2, i = 0;
        while (i < locales.size()) {
            templateName.replace(pos, pos + locales.get(prev).length(), locales.get(i));
            String str = templateName.toString();
            if (!list.contains(str)) list.add(str);
            i++;
            prev = (2 + i) % 3;
        }
        return list;
    }

    static Map<String, Country> load() {
        Map<String, Country> countries = new TreeMap<>();

        // load country names
        try (Scanner scanner = new Scanner(Internationalization.class.getResourceAsStream("/com/guestful/i18n/countries.csv"), "UTF-8")) {
            scanner.useDelimiter(",");
            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                Country country = new Country(line.substring(0, 2), line.substring(3));
                countries.put(country.getCode(), country);
            }
        }

        // load available timeZones for each countries
        try (Scanner scanner = new Scanner(Internationalization.class.getResourceAsStream("/com/guestful/i18n/timezones.csv"), "UTF-8")) {
            scanner.useDelimiter(",");
            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                countries.get(line.substring(0, 2)).getTimeZones().add(ZoneId.of(line.substring(3)));
            }
        }

        // load postal code validators
        try (Reader reader = new InputStreamReader(Internationalization.class.getResourceAsStream("/com/guestful/i18n/postalCodeData.json"), "UTF-8")) {
            JsonObject postalCodeData = Json.createReader(reader).readObject().getJsonObject("supplemental").getJsonObject("postalCodeData");
            for (Map.Entry<String, JsonValue> entry : postalCodeData.entrySet()) {
                Country country = countries.get(entry.getKey());
                if (country == null) {
                    throw new IllegalStateException("No country: " + entry.getKey());
                }
                country.setPostalCodePattern(Pattern.compile(((JsonString) entry.getValue()).getString(), Pattern.CASE_INSENSITIVE));
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        // add missings
        countries.get("IL").setPostalCodePattern(Pattern.compile("\\d{5}(\\d{2})?", Pattern.CASE_INSENSITIVE));
        countries.get("YU").setPostalCodePattern(Pattern.compile("\\d{5}", Pattern.CASE_INSENSITIVE));

        // load country phone extensions
        try (Reader reader = new InputStreamReader(Internationalization.class.getResourceAsStream("/com/guestful/i18n/telephoneCodeData.json"), "UTF-8")) {
            JsonObject telephoneCodeData = Json.createReader(reader).readObject().getJsonObject("supplemental").getJsonObject("telephoneCodeData");
            for (Map.Entry<String, JsonValue> entry : telephoneCodeData.entrySet()) {
                Country country = countries.get(entry.getKey());
                if (country == null) {
                    throw new IllegalStateException("No country: " + entry.getKey());
                }
                country.setPhonePrefix("+" + Integer.parseInt(((JsonArray) entry.getValue()).getJsonObject(0).getString("telephoneCountryCode")));
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        // add states
        {
            Country ca = countries.get("CA");
            ca.addState("AB", "Alberta").addTimeZone("Canada/Mountain");
            ca.addState("BC", "British Columbia").addTimeZone("Canada/Pacific");
            ca.addState("MB", "Manitoba").addTimeZone("Canada/Central");
            ca.addState("NB", "New Brunswick").addTimeZone("Canada/Eastern");
            ca.addState("NL", "Newfoundland and Labrador").addTimeZone("Canada/Newfoundland");
            ca.addState("NT", "Northwest Territories").addTimeZone("Canada/Mountain");
            ca.addState("NS", "Nova Scotia").addTimeZone("Canada/Atlantic");
            ca.addState("NU", "Nunavut").addTimeZone("Canada/Eastern");
            ca.addState("ON", "Ontario").addTimeZone("Canada/Eastern");
            ca.addState("PE", "Prince Edward Island").addTimeZone("Canada/Atlantic");
            ca.addState("QC", "Quebec").addTimeZone("Canada/Eastern");
            ca.addState("SK", "Saskatchewan").addTimeZone("Canada/Central");
            ca.addState("YT", "Yukon").addTimeZone("Canada/Pacific");

            Country us = countries.get("US");
            us.addState("AL", "Alabama").addTimeZone("US/Central");
            us.addState("AK", "Alaska").addTimeZone("US/Alaska");
            us.addState("AZ", "Arizona").addTimeZone("US/Arizona");
            us.addState("AR", "Arkansas").addTimeZone("US/Central");
            us.addState("CA", "California").addTimeZone("US/Pacific");
            us.addState("CO", "Colorado").addTimeZone("US/Mountain");
            us.addState("CT", "Connecticut").addTimeZone("US/Eastern");
            us.addState("DE", "Delaware").addTimeZone("US/Eastern");
            us.addState("DC", "District of Columbia").addTimeZone("US/Eastern");
            us.addState("FL", "Florida").addTimeZone("US/Eastern");
            us.addState("GA", "Georgia").addTimeZone("US/Eastern");
            us.addState("HI", "Hawaii").addTimeZone("US/Hawaii");
            us.addState("ID", "Idaho").addTimeZone("US/Mountain");
            us.addState("IL", "Illinois").addTimeZone("US/Central");
            us.addState("IN", "Indiana").addTimeZone("US/Eastern");
            us.addState("IA", "Iowa").addTimeZone("US/Central");
            us.addState("KS", "Kansas").addTimeZone("US/Central");
            us.addState("KY", "Kentucky").addTimeZone("US/Eastern");
            us.addState("LA", "Louisiana").addTimeZone("US/Central");
            us.addState("ME", "Maine").addTimeZone("US/Eastern");
            us.addState("MD", "Maryland").addTimeZone("US/Eastern");
            us.addState("MA", "Massachusetts").addTimeZone("US/Eastern");
            us.addState("MI", "Michigan").addTimeZone("US/Eastern");
            us.addState("MN", "Minnesota").addTimeZone("US/Central");
            us.addState("MS", "Mississippi").addTimeZone("US/Central");
            us.addState("MO", "Missouri").addTimeZone("US/Central");
            us.addState("MT", "Montana").addTimeZone("US/Mountain");
            us.addState("NE", "Nebraska").addTimeZone("US/Central");
            us.addState("NV", "Nevada").addTimeZone("US/Pacific");
            us.addState("NH", "New Hampshire").addTimeZone("US/Eastern");
            us.addState("NJ", "New Jersey").addTimeZone("US/Eastern");
            us.addState("NM", "New Mexico").addTimeZone("US/Mountain");
            us.addState("NY", "New York").addTimeZone("US/Eastern");
            us.addState("NC", "North Carolina").addTimeZone("US/Eastern");
            us.addState("ND", "North Dakota").addTimeZone("US/Central");
            us.addState("OH", "Ohio").addTimeZone("US/Eastern");
            us.addState("OK", "Oklahoma").addTimeZone("US/Central");
            us.addState("OR", "Oregon").addTimeZone("US/Pacific");
            us.addState("PA", "Pennsylvania").addTimeZone("US/Eastern");
            us.addState("RI", "Rhode Island").addTimeZone("US/Eastern");
            us.addState("SC", "South Carolina").addTimeZone("US/Eastern");
            us.addState("SD", "South Dakota").addTimeZone("US/Central");
            us.addState("TN", "Tennessee").addTimeZone("US/Central");
            us.addState("TX", "Texas").addTimeZone("US/Central");
            us.addState("UT", "Utah").addTimeZone("US/Mountain");
            us.addState("VT", "Vermont").addTimeZone("US/Eastern");
            us.addState("VA", "Virginia").addTimeZone("US/Eastern");
            us.addState("WA", "Washington").addTimeZone("US/Pacific");
            us.addState("WV", "West Virginia").addTimeZone("US/Eastern");
            us.addState("WI", "Wisconsin").addTimeZone("US/Central");
            us.addState("WY", "Wyoming").addTimeZone("US/Mountain");
        }

        // be sure all timeZones are there
        countries.values()
            .forEach(country ->
                country.getTimeZones().addAll(country.getStates().values().stream().flatMap(state ->
                    state.getTimeZones().stream()).collect(Collectors.toSet())));

        return countries;
    }

    @SuppressWarnings("unchecked")
    public static <String, T> Map<String, T> map(Object... entries) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < entries.length; i += 2) {
            if (entries[i + 1] != null) {
                map.put((String) entries[i], entries[i + 1]);
            }
        }
        return (Map<String, T>) map;
    }

}
