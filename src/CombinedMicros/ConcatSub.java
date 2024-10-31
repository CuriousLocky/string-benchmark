package CombinedMicros;

import CustomString.ArrayRope;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Random;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;

import static IndividualMicros.Concat.stringBuilderRepeat;
import static utils.Utils.makeRandomString;

@Fork(value = 1)
@Warmup(iterations = 3, time = 5, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 2, time = 5, timeUnit = TimeUnit.SECONDS)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class ConcatSub {
    @State(Scope.Thread)
    public static class ConcatSubState {
        @Param({"10", "100", "1000"})
        public int concatIter = 1000;
        @Param({"10", "100", "1000"})
        public int subIter = 1000;
        @Param({"5", "100", "1000"})
        public int stringSize = 1000;
        public final int subStringSize = 5;
        public final boolean checkResult = false;
        private final int seed = 0;
        private final Random rand = new Random(seed);
        public String str1;
        public String str2;
        public String result;
        public final int[] subStringsIndices = new int[subIter];
        @Setup
        public void setup() {
            for (int i = 0; i < subIter; i++) {
                subStringsIndices[i] = i;
            }
            str1 = makeRandomString(stringSize, rand);
            str2 = makeRandomString(stringSize, rand);
        }
    }

    @Benchmark
    public void stringBuilderNoMat(ConcatSubState state, Blackhole bh) {
        StringBuilder srcBuilder = new StringBuilder(state.str1);
        for (int i = 0; i < state.concatIter; i++) {
            srcBuilder.append(state.str2);
        }
        StringBuilder tgtBuilder = new StringBuilder();
        for (int i : state.subStringsIndices) {
            tgtBuilder.append(srcBuilder.subSequence(i, i + state.subStringSize));
        }
        bh.consume(tgtBuilder);
    }

    @Benchmark
    public void stringNaive(ConcatSubState state, Blackhole bh) {
        String src = state.str1;
        src += state.str2.repeat(state.concatIter);
        String tgt = "";
        for (int i : state.subStringsIndices) {
            tgt += src.substring(i, i + state.subStringSize);
        }
        bh.consume(tgt);
    }

    @Benchmark
    public void stringBuilder(ConcatSubState state, Blackhole bh) {
        StringBuilder tgtBuilder = new StringBuilder();
        String srcStr = stringBuilderRepeat(state.str1, state.str2, state.concatIter);
        for (int i : state.subStringsIndices) {
            tgtBuilder.append(srcStr, i, i + state.subStringSize);
        }
        String result = tgtBuilder.toString();
        bh.consume(result);
    }

    @Benchmark
    public void stringJoiner(ConcatSubState state, Blackhole bh) {
        StringJoiner srcJoiner = new StringJoiner("");
        srcJoiner.add(state.str1);
        for (int i = 0; i < state.concatIter; i++) {
            srcJoiner.add(state.str2);
        }
        String srcStr = srcJoiner.toString();
        StringJoiner tgtJoiner = new StringJoiner("");
        for (int i : state.subStringsIndices) {
            tgtJoiner.add(srcStr.subSequence(i, i + state.subStringSize));
        }
        String result = tgtJoiner.toString();
        bh.consume(result);
    }

    @Benchmark
    public void arrayRope(ConcatSubState state, Blackhole bh) {
        ArrayRope rope = new ArrayRope(state.str1);
        for (int i = 0; i < state.concatIter; i++) {
            rope.append(state.str2);
        }
        ArrayRope rope2 = new ArrayRope();
        for (int i : state.subStringsIndices) {
            rope2.append(rope.subSequence(i, i + state.subStringSize));
        }
        String result = rope2.toString();
        bh.consume(result);
    }

    @Benchmark
    public void arrayRopeImmutable(ConcatSubState state, Blackhole bh) {
        ArrayRope rope = new ArrayRope(state.str1);
        for (int i = 0; i < state.concatIter; i++) {
            rope = rope.concat(state.str2);
        }
        ArrayRope rope2 = new ArrayRope();
        for (int i : state.subStringsIndices) {
            rope2 = rope2.concat(rope.subSequence(i, i + state.subStringSize));
        }
        String result = rope2.toString();
        bh.consume(result);
    }

    @Benchmark
    public void arrayRopeNoMat(ConcatSubState state, Blackhole bh) {
        ArrayRope rope = new ArrayRope(state.str1);
        for (int i = 0; i < state.concatIter; i++) {
            rope.append(state.str2);
        }
        ArrayRope rope2 = new ArrayRope();
        for (int i : state.subStringsIndices) {
            rope2.append(rope.subSequence(i, i + state.subStringSize));
        }
        bh.consume(rope2);
    }
}