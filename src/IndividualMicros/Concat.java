package IndividualMicros;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import CustomString.ArrayRope;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static utils.Utils.makeRandomString;

@Fork(value = 1)
@Warmup(iterations = 3, time = 5, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 2, time = 5, timeUnit = TimeUnit.SECONDS)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)

public class Concat {
    @State(Scope.Thread)
    public static class ConcatBenchmarkState {
        @Param({"100", "1000", "10000"})
        public int iter;
        public final int seed = 0;
        public final Random rand = new Random(seed);
        @Param({"5", "10", "50", "100", "500"})
        public int stringLength;
        public String str1;
        public String str2;
        public String formatStr;
        public String[] str2Arr;
        public String[] str1str2Arr;
        public String result;
        @Setup
        public void setup() {
            str1 = makeRandomString(stringLength, rand);
            str2 = makeRandomString(stringLength, rand);
            formatStr = "%s".repeat(iter);
            str2Arr = new String[iter];
            Arrays.fill(str2Arr, str2);
            str1str2Arr = new String[iter + 1];
            str1str2Arr[0] = str1;
            Arrays.fill(str1str2Arr, 1, iter + 1, str2);
            result = str1 + String.join("", str2Arr);
        }
    }

    public String[] arrayFill(ConcatBenchmarkState state) {
        String[] array = new String[state.iter + 1];
        array[0] = state.str1;
        Arrays.fill(array, 1, state.iter + 1, state.str2);
        return array;
    }

    @Benchmark
    public String plus(ConcatBenchmarkState state, Blackhole bh) {
        String str = state.str1;
        for (int i = 0; i < state.iter; i++) {
            str += state.str2;
        }
        bh.consume(str);
        return str;
    }

    @Benchmark
    public String concat(ConcatBenchmarkState state, Blackhole bh) {
        String str = state.str1;
        for(int i = 0; i < state.iter; i++) {
            str = str.concat(state.str2);
        }
        bh.consume(str);
        return str;
    }

    @Benchmark
    public String join(ConcatBenchmarkState state, Blackhole bh) {
        String str = state.str1;
        for(int i = 0; i < state.iter; i++) {
            str = String.join("", str, state.str2);
        }
        bh.consume(str);
        return str;
    }


    @Benchmark
    public String joinBatch(ConcatBenchmarkState state, Blackhole bh) {
        String[] str1str2Arr = arrayFill(state);
        String str = String.join("", str1str2Arr);
        bh.consume(str);
        return str;
    }

    @Benchmark
    public String format(ConcatBenchmarkState state, Blackhole bh) {
        String str = state.str1;
        for(int i = 0; i < state.iter; i++) {
            str = String.format("%s%s", str, state.str2);
        }
        bh.consume(str);
        return str;
    }

    @Benchmark
    public String formatManual(ConcatBenchmarkState state, Blackhole bh) {
        String str = state.str1;
        Formatter formatter = new Formatter();
        formatter.format("%s", str);
        for(int i = 0; i < state.iter; i++) {
            formatter = formatter.format("%s", state.str2);
        }

        String result = formatter.toString();
        bh.consume(result);
        return result;
    }

    @Benchmark
    public String formatBatch(ConcatBenchmarkState state, Blackhole bh) {
        String[] str1str2Arr = arrayFill(state);
        String str = String.format(state.formatStr, (Object[]) str1str2Arr);
        bh.consume(str);
        return str;
    }

    @Benchmark
    public String streamAPI(ConcatBenchmarkState state, Blackhole bh) {
        String str = state.str1;
        for (int i = 0; i < state.iter; i++) {
            List<String> stringList = List.of(str, state.str2);
            str = stringList.stream().collect(Collectors.joining());
        }
        bh.consume(str);
        return str;
    }

    @Benchmark
    public String streamAPIBatch(ConcatBenchmarkState state, Blackhole bh) {
        String[] str1str2Arr = arrayFill(state);
        List<String> list = Arrays.asList(str1str2Arr);
        String str = list.stream().collect(Collectors.joining());
        bh.consume(str);
        return str;
    }

    @Benchmark
    public String stringBuffer(ConcatBenchmarkState state, Blackhole bh) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("String");
        for(int i = 0; i < state.iter; i++) {
            buffer.append(state.str2);
        }
        String str = buffer.toString();
        bh.consume(str);
        return str;
    }

    public static String stringBuilderRepeat(String head, String tail, int repeat) {
        StringBuilder builder = new StringBuilder(head);
        for (int i = 0; i < repeat; i++) {
            builder.append(tail);
        }
        return builder.toString();
    }

    @Benchmark
    public String stringBuilder(ConcatBenchmarkState state, Blackhole bh) {
        String str = stringBuilderRepeat(state.str1, state.str2, state.iter);
        bh.consume(str);
        return str;
    }

    @Benchmark
    public String stringBuilderDum(ConcatBenchmarkState state, Blackhole bh) {
        StringBuilder builder = new StringBuilder(state.str1);
        for(int i = 0; i < state.iter; i++) {
            builder.append(state.str2);
            String currnet = builder.toString();
            builder = new StringBuilder(currnet);
        }
        String str = builder.toString();
        bh.consume(str);
        return str;
    }

    public static String stringJoinerRepeat(String head, String tail, int repeat) {
        StringJoiner joiner = new StringJoiner("");
        joiner.add(head);
        for (int i = 0; i < repeat; i++) {
            joiner.add(tail);
        }
        return joiner.toString();
    }

    @Benchmark
    public String stringJoiner(ConcatBenchmarkState state, Blackhole bh) {
        String str = stringJoinerRepeat(state.str1, state.str2, state.iter);
        bh.consume(str);
        return str;
    }

    @Benchmark
    public String rawArrayCopy(ConcatBenchmarkState state, Blackhole bh) {
        int size1 = state.str1.length();
        int size2 = state.str2.length();
        int size = size1 + size2 * state.iter;
        char[] chars = new char[size];
        System.arraycopy(state.str1.toCharArray(), 0, chars, 0, size1);
        char[] str2chars = state.str2.toCharArray();
        for(int i = 0; i < state.iter; i++) {
            System.arraycopy(str2chars, 0, chars, size1 + i * size2, size2);
        }
        String str = String.valueOf(chars);
        bh.consume(str);
        return str;
    }

    @Benchmark
    public String rawLoop(ConcatBenchmarkState state, Blackhole bh) {
        int size1 = state.str1.length();
        int size2 = state.str2.length();
        char[] chars = new char[size1 + size2 * state.iter];
        char[] str1chars = state.str1.toCharArray();
        for (int i = 0; i < size1; i++) {
            chars[i] = str1chars[i];
        }
        char[] str2chars = state.str2.toCharArray();
        int index = size1;
        for (int i = 0; i < state.iter; i++) {
            for (int j = 0; j < size2; j++) {
                chars[index++] = str2chars[j];
            }
        }
        String str = String.valueOf(chars);
        bh.consume(str);
        return str;
    }

    @Benchmark
    public String arrayRope(ConcatBenchmarkState state, Blackhole bh) {
        ArrayRope arrayRope = new ArrayRope(state.str1str2Arr);
        return arrayRope.toString();
    }
}