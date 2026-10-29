package io.mosip.testrig.adminui.hooks;

import io.mosip.testrig.adminui.config.AdminConfigManager;
import io.mosip.testrig.adminui.config.AdminUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Per-run test data, generated once at suite start and referenced from feature files as
 * {@code ${name}} placeholders (e.g. {@code ${data}1}).
 */
public final class AdminTestData {

    private static final Logger logger = LoggerFactory.getLogger(AdminTestData.class);
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Pattern PLACEHOLDER = Pattern.compile("\\$\\{([A-Za-z]+)}");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    private static final Map<String, String> values = new LinkedHashMap<>();
    private static boolean ready = false;

    private AdminTestData() {}

    public static synchronized void ensureReady() {
        if (ready) return;
        Set<String> usedDates = new HashSet<>();
        values.put("data", AdminConfigManager.getPreappend()
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm")));
        values.put("blocklistedWord", randomLetters(11));
        values.put("holidayDate", randomFutureDate(usedDates));
        values.put("holidayDateCenter", randomFutureDate(usedDates));
        values.put("validityDate", randomFutureDate(usedDates));
        // Masterdata rejects a machine whose key fingerprint is already mapped, so every run needs new keys.
        values.put("publicKey", randomRsaPublicKey());
        values.put("signPublicKey", randomRsaPublicKey());
        logger.info("Generated test data: data={}, blocklistedWord={}, holidayDate={}, holidayDateCenter={}, validityDate={}",
                values.get("data"), values.get("blocklistedWord"), values.get("holidayDate"),
                values.get("holidayDateCenter"), values.get("validityDate"));

        AdminUtil.mapAdminUserToZone();
        ready = true;
    }

    public static String get(String name) {
        String value = values.get(name);
        if (value == null) throw new IllegalArgumentException("Unknown test data '" + name + "'. Known: " + values.keySet());
        return value;
    }

    /** Replaces every {@code ${name}} placeholder in {@code raw} with its generated value. */
    public static String resolve(String raw) {
        if (raw == null) return null;
        Matcher m = PLACEHOLDER.matcher(raw);
        StringBuilder out = new StringBuilder();
        while (m.find()) {
            m.appendReplacement(out, Matcher.quoteReplacement(get(m.group(1))));
        }
        m.appendTail(out);
        return out.toString();
    }

    private static String randomLetters(int length) {
        StringBuilder word = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            word.append((char) ('a' + RANDOM.nextInt(26)));
        }
        return word.toString();
    }

    private static String randomFutureDate(Set<String> usedDates) {
        String formatted;
        do {
            formatted = LocalDate.now().plusDays(1 + RANDOM.nextInt(699)).format(DATE_FORMAT);
        } while (!usedDates.add(formatted));
        return formatted;
    }

    private static String randomRsaPublicKey() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048, RANDOM);
            return Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(generator.generateKeyPair().getPublic().getEncoded());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("RSA key generation not available", e);
        }
    }
}
