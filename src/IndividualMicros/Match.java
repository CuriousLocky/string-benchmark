package IndividualMicros;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static utils.Utils.makeRandomString;

@Fork(value = 1)
@Warmup(iterations = 3, time = 5, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 2, time = 5, timeUnit = TimeUnit.SECONDS)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)

public class Match {
    @State(Scope.Thread)
    public static class MatchState {
        public final int iter = 100;
        public final long seed = 0;
        @Param({"20", "100", "500", "1000"})
        public int traceSize;
        @Param({"3", "10", "100"})
        public int patternSize;
        public final int patternNum = 3;
        private final Random rand = new Random(seed);
        public String trace;
        public String pattern;
        @Setup
        public void setup() {
            pattern = makeRandomString(patternSize, rand);
            int non_pattern_trace_length = traceSize - patternNum * patternSize;
            int non_pattern_length_per_part = non_pattern_trace_length / patternNum;
            StringBuilder traceBuilder = new StringBuilder();
            for (int i = 0; i < patternNum; i++) {
                traceBuilder.append(pattern);
                traceBuilder.append(makeRandomString(non_pattern_length_per_part, rand));
            }
            trace = traceBuilder.toString();
        }
    }

    public static void allIndexOf(String trace, String pattern, Blackhole bh) {
        int lastIndex = 0;
        while (lastIndex >= 0) {
            lastIndex = trace.indexOf(pattern, lastIndex + 1);
            bh.consume(lastIndex);
        }
    }

    @Benchmark
    public void indexOf(MatchState state, Blackhole bh) {
        for (int i = 0; i < state.iter; i++) {
            allIndexOf(state.trace, state.pattern, bh);
        }
    }

    public static void allRegexFind(String trace, String pattern, Blackhole bh) {
        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(trace);
        while (matcher.find()) {
            int index = matcher.start();
            bh.consume(index);
        }
    }


    @Benchmark
    public void regexFind(MatchState state, Blackhole bh) {
        for (int i = 0; i < state.iter; i++) {
            allRegexFind(state.trace, state.pattern, bh);
        }
    }

    @Benchmark
    public void regexSplit(MatchState state, Blackhole bh) {
        for (int i = 0; i < state.iter; i++) {
            Pattern compiledPattern = Pattern.compile(state.pattern);
            String[] parts = compiledPattern.split(state.trace, -1);
            int lastIndex = 0;
            for (String part: parts) {
                lastIndex += part.length();
                bh.consume(lastIndex);
                lastIndex += state.pattern.length();
            }
        }
    }

    @Benchmark
    public void slidingSubstring(MatchState state, Blackhole bh) {
        for (int i = 0; i < state.iter; i++) {
            for (int index = 0; index < state.trace.length() - state.pattern.length(); index++) {
                String subString = state.trace.substring(index, index + state.pattern.length());
                if (subString.equals(state.pattern)) {
                    bh.consume(index);
                }
            }
        }
    }

    public static void allSlidingCharAtNoOverlap(CharSequence trace, CharSequence pattern, Blackhole bh) {
        int index_base = 0;
        while (index_base <= trace.length() - pattern.length()) {
            boolean matched = true;
            int move_step = pattern.length();
            for (int index = 0; index < pattern.length(); index++) {
                if (pattern.charAt(index) != trace.charAt(index_base + index)) {
                    matched = false;
                    move_step = index + 1;
                    break;
                }
            }
            if (matched) {
                bh.consume(index_base);
            }
            index_base += move_step;
        }
    }

    @Benchmark
    public void slidingCharAtNoOverlap(MatchState state, Blackhole bh) {
        for (int i = 0; i < state.iter; i++) {
            allSlidingCharAtNoOverlap(state.trace, state.pattern, bh);
        }
    }

    @Benchmark
    public void slidingCharAt(MatchState state, Blackhole bh) {
        for (int i = 0; i < state.iter; i++) {
            for (int index_base = 0; index_base <= state.trace.length() - state.pattern.length(); index_base++) {
                boolean matched = true;
                for (int index = 0; index < state.pattern.length(); index++) {
                    if (state.pattern.charAt(index) != state.trace.charAt(index_base + index)) {
                        matched = false;
                        break;
                    }
                }
                if (matched) {
                    bh.consume(index_base);
                }
            }
        }
    }

    @Benchmark
    public void slidingStartsWith(MatchState state, Blackhole bh) {
        for (int i = 0; i < state.iter; i++) {
            for (int index = 0; index <= state.trace.length() - state.pattern.length(); index++) {
                if (state.trace.startsWith(state.pattern, index)) {
                    bh.consume(index);
                }
            }
        }
    }

}