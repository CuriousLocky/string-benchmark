package CustomString;

import java.util.Arrays;
import java.util.stream.IntStream;

public class ArrayRope implements CharSequence {
    private static final int INITIAL_CAPACITY = 16;
    private static final int BINARYSEARCH_THRESHOLD = 128;
    private static final int GROWTH_THRESHOLD = 1024;

    private static class ArrayRopePiece implements CharSequence {
        int start;
        int end;
        final CharSequence content;
        ArrayRopePiece(int start, int end, CharSequence content) {
            this.start = start;
            this.end = end;
            if (content instanceof ArrayRopePiece) {
                this.start += ((ArrayRopePiece) content).start;
                this.end += ((ArrayRopePiece) content).start;
                this.content = ((ArrayRopePiece) content).content;
            } else {
                this.content = content;
            }
        }
        ArrayRopePiece(CharSequence content) {
            this(0, content.length(), content);
        }

        @Override
        public int length() {
            return end - start;
        }

        @Override
        public char charAt(int index) {
            return content.charAt(start + index);
        }

        @Override
        public boolean isEmpty() {
            return this.content.isEmpty() || this.length() == 0;
        }

        @Override
        public ArrayRopePiece subSequence(int start, int end) {
            return new ArrayRopePiece(this.start + start, this.start + end, content);
        }
    }

//    private final ArrayList<ArrayRopePiece> pieces;
    private ArrayRopePiece[] pieces;
    private int piecesLength;
    private int[] indices;
//    private final ArrayList<Integer> indices;
    @Override
    public int length() {
        if (piecesLength == 0) {return 0;}
        return indices[piecesLength - 1] + pieces[piecesLength - 1].length();
    }

    private int searchPieceIndex(int skip, int charIndex) {
        int piece_index;
        if (piecesLength <= BINARYSEARCH_THRESHOLD) {
//            linear search when number of pieces is small
            for (piece_index = skip; piece_index < piecesLength - 1; piece_index++) {
                if (indices[piece_index + 1] >= charIndex) {break;}
            }
        } else {
            piece_index = Arrays.binarySearch(indices, skip, piecesLength, charIndex);
            if (piece_index < 0) {
                piece_index = -piece_index - 2;
            }
        }
        return piece_index;
    }

    private int searchPieceIndex(int charIndex) {
        return searchPieceIndex(0, charIndex);
    }

    @Override
    public char charAt(int index) {
        int piece_index = searchPieceIndex(index);
        return pieces[piece_index].charAt(index - indices[piecesLength]);
    }

    @Override
    public boolean isEmpty() {
        return piecesLength == 0;
    }

    @Override
    public CharSequence subSequence(int start, int end) {
//        find the first and last piece
        int start_piece_index = searchPieceIndex(0, start);
        int end_piece_index = searchPieceIndex(start_piece_index, end);
        end_piece_index += start_piece_index;
//        locate subsequence in start and end pieces
        int char_index_in_start_piece = start - indices[start_piece_index];
        int char_index_in_end_piece = end - indices[end_piece_index];
        if (start_piece_index == end_piece_index) {
            return this.pieces[start_piece_index].subSequence(char_index_in_start_piece, char_index_in_end_piece);
        }
//        Assemble new pieces collection
        ArrayRopePiece[] subPieces = new ArrayRopePiece[end_piece_index - start_piece_index + 1];
        ArrayRopePiece first = this.pieces[start_piece_index];
        subPieces[0] = first.subSequence(char_index_in_start_piece, first.length());
        for (int i = start_piece_index + 1; i < end_piece_index; i++) {
            subPieces[i - start_piece_index] = pieces[start_piece_index];
        }
        ArrayRopePiece last = this.pieces[end_piece_index];
        subPieces[subPieces.length - 1] = last.subSequence(0, char_index_in_end_piece);
        return new ArrayRope(subPieces);
    }

    public ArrayRope() {
        this.pieces = new ArrayRopePiece[INITIAL_CAPACITY];
        this.indices = new int[INITIAL_CAPACITY];
        this.piecesLength = 0;
    }

    private ArrayRope(ArrayRopePiece[] pieces, int[] indices, int piecesLength) {
        this.pieces = pieces;
        this.indices = indices;
        this.piecesLength = piecesLength;
    }

    public ArrayRope(CharSequence... contents) {
//        count targetSize
        int target_size = 0;
        for (CharSequence content : contents) {
            if (content instanceof ArrayRope rope) {
                target_size += rope.piecesLength;
            } else {
                target_size += 1;
            }
        }
//        initialize piece array
        int pieces_size = INITIAL_CAPACITY;
        while (pieces_size < target_size) {
            pieces_size += Math.min(pieces_size, GROWTH_THRESHOLD);
        }
        this.pieces = new ArrayRopePiece[pieces_size];
        this.indices = new int[pieces_size];
        int current_piece_index = 0;
        int current_length = 0;
        for (CharSequence content : contents) {
            if (content instanceof ArrayRope rope) {
                System.arraycopy(rope.pieces, 0, this.pieces, current_piece_index, rope.piecesLength);
                for (int j = 0; j < rope.piecesLength; j++) {
                    indices[current_piece_index + j] = rope.indices[j] + current_length;
                }
                current_piece_index += rope.piecesLength;
                current_length += rope.length();
            } else {
                ArrayRopePiece new_piece = content instanceof ArrayRopePiece piece ? piece : new ArrayRopePiece(content);
                pieces[current_piece_index] = new_piece;
                indices[current_piece_index] = current_length;
                current_length += content.length();
                current_piece_index += 1;
            }
        }
        piecesLength = current_piece_index;
    }

    private void expand(int increased) {
//        compute after expand size
        int target_size = piecesLength + increased;
        int expanded_size = pieces.length;
        while (expanded_size < target_size) {
            expanded_size += Math.min(expanded_size, GROWTH_THRESHOLD);
        }
//        copy current piece info
        ArrayRopePiece[] new_pieces = new ArrayRopePiece[expanded_size];
        System.arraycopy(pieces, 0, new_pieces, 0, piecesLength);
        this.pieces = new_pieces;
        int[] new_indices = new int[expanded_size];
        System.arraycopy(indices, 0, new_indices, 0, piecesLength);
        this.indices = new_indices;
    }

    public ArrayRope append(CharSequence cs) {
        if (cs.isEmpty()) {
            return this;
        }
        if (cs instanceof ArrayRope appended) {
            if (piecesLength + appended.piecesLength > pieces.length) {
                expand(appended.piecesLength);
            }
            int current_length = length();
            System.arraycopy(appended.pieces, 0, pieces, piecesLength, appended.piecesLength);
            for (int i = 0; i < appended.piecesLength; i++ ) {
                indices[piecesLength + i] = current_length + appended.indices[i];
            }
            piecesLength += appended.piecesLength;
        } else {
            if (piecesLength + 1 > pieces.length) {
                expand(1);
            }
            ArrayRopePiece new_piece = cs instanceof ArrayRopePiece piece ? piece : new ArrayRopePiece(cs);
            pieces[piecesLength] = new_piece;
            indices[piecesLength] = length();
            piecesLength += 1;
        }
        return this;
    }

    public ArrayRope concat(CharSequence cs) {
        int increment = cs instanceof ArrayRope rope ? rope.piecesLength : 1;
        int target_size = piecesLength + increment;
        int new_pieces_length = pieces.length;
        if (new_pieces_length < target_size) {
            new_pieces_length += Math.min(new_pieces_length, GROWTH_THRESHOLD);
        }
        ArrayRopePiece[] new_pieces = new ArrayRopePiece[new_pieces_length];
        System.arraycopy(pieces, 0, new_pieces, 0, piecesLength);
        int[] new_indices = new int[new_pieces_length];
        System.arraycopy(indices, 0, new_indices, 0, piecesLength);
        ArrayRope new_rope = new ArrayRope(new_pieces, new_indices, piecesLength);
        new_rope.append(cs);
        return new_rope;
    }

    @Override
    public String toString() {
        char[] charBuffer = new char[this.length()];
        int pos = 0;
        for (int i = 0; i < piecesLength; i++) {
            ArrayRopePiece piece = pieces[i];
            for (int j = 0; j < piece.length(); j++) {
                charBuffer[pos + j] = piece.content.charAt(j + piece.start);
            }
            pos += piece.length();
        }
        return new String(charBuffer);
    }

    @Override
    public IntStream chars() {
//        TODO: Add state to avoid search cost in charAt()
        return CharSequence.super.chars();
    }

    @Override
    public IntStream codePoints() {
        return CharSequence.super.codePoints();
    }

}