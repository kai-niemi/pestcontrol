package io.cockroachdb.pest.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import org.springframework.data.util.Pair;

public abstract class PatternUtils {
    private static final Pattern LOCALITY_PATTERN
            = Pattern.compile("([^=,]+)=([^\0]+?)(?=,[^,]+=|$)", Pattern.CASE_INSENSITIVE);

    private PatternUtils() {
    }

    public static Map<String, String> parseLocality(String locality) {
        final Matcher matcher = LOCALITY_PATTERN.matcher(locality);

        Map<String, String> map = new LinkedHashMap<>();

        while (matcher.find()) {
            for (int i = 1; i <= matcher.groupCount(); i++) {
                map.put(matcher.group(1), matcher.group(2));
            }
        }

        return map;
    }

    public static List<Integer> parseIntRange(String input) {
        List<Integer> list = new ArrayList<>();

        for (String part : input.split(",")) {
            parseRange(part).ifPresentOrElse(pair ->
                            IntStream.rangeClosed(pair.getFirst(), pair.getSecond()).forEach(list::add),
                    () -> list.add(Integer.parseInt(part)));
        }

        return list;
    }

    public static Optional<Pair<Integer, Integer>> parseRange(String input) {
        Pattern tuples = Pattern.compile("(\\d+)\\s*[:-]\\s*(\\d+)");

        Matcher m = tuples.matcher(input);
        if (m.matches()) {
            return Optional.of(Pair.of(
                    Integer.parseInt(m.group(1)),
                    Integer.parseInt(m.group(2))));
        }

        return Optional.empty();
    }
}
