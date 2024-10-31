package CombinedMicros;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import static IndividualMicros.Concat.stringBuilderRepeat;
import static IndividualMicros.Concat.stringJoinerRepeat;
import static IndividualMicros.Match.*;
import static utils.Utils.makeRandomString;

@Fork(value = 1)
@Warmup(iterations = 3, time = 5, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 2, time = 5, timeUnit = TimeUnit.SECONDS)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class ConcatMatch {
    @State(Scope.Thread)
    public static class ConcatMatchState {
        @Param({"100", "500", "10000"})
        public int concatIter = 1000;
        @Param({"5", "20", "100"})
        public int stringSize = 5;
        public int patternSize = 3;
        @Param({"10", "100", "1000"})
        public int matchIter = 100;
        private final int seed = 0;
        private final Random rand = new Random(seed);
        public String str1;
        public String str2;
        public String pattern;
        public String result;

        @Setup
        public void setup() {
            str1 = makeRandomString(stringSize, rand);
            str2 = makeRandomString(stringSize, rand);
            StringBuilder sb = new StringBuilder(str1);
            sb.append(String.valueOf(str2).repeat(concatIter));
            result = sb.toString();
            int patternIndex = rand.nextInt(result.length() - patternSize);
            pattern = result.substring(patternIndex, patternIndex + patternSize);
        }
    }

    @Benchmark
    public void stringBuilderIndexOf(ConcatMatchState state, Blackhole bh) {
        String result = stringBuilderRepeat(state.str1, state.str2, state.concatIter);
        for (int i = 0; i < state.matchIter; i++) {
            allIndexOf(result, state.pattern, bh);
        }
    }

    @Benchmark
    public void stringBuilderRegexFind(ConcatMatchState state, Blackhole bh) {
        String result = stringBuilderRepeat(state.str1, state.str2, state.concatIter);
        for (int i = 0; i < state.matchIter; i++) {
            allRegexFind(result, state.pattern, bh);
        }
    }

    @Benchmark
    public void stringJoinerIndexOf(ConcatMatchState state, Blackhole bh) {
        String result = stringJoinerRepeat(state.str1, state.str2, state.concatIter);
        for (int i = 0; i < state.matchIter; i++) {
            allIndexOf(result, state.pattern, bh);
        }
    }

    @Benchmark
    public void stringJoinerRegexFind(ConcatMatchState state, Blackhole bh) {
        String result = stringJoinerRepeat(state.str1, state.str2, state.concatIter);
        for (int i = 0; i < state.matchIter; i++) {
            allRegexFind(result, state.pattern, bh);
        }
    }

    @Benchmark
    public void stringBuilderCharAt(ConcatMatchState state, Blackhole bh) {
        StringBuilder sb = new StringBuilder(state.str1);
        for (int i = 0; i < state.concatIter; i++) {
            sb.append(state.str2);
        }
        for (int i = 0; i < state.matchIter; i++) {
            allSlidingCharAtNoOverlap(sb, state.pattern, bh);
        }
    }

}
