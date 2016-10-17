package org.json.simple.parser;

/**
 * A lexical token.
 * 
 * @author FangYidong<fangyidong@yahoo.com.cn>
 */
final class Yytoken {
    /**
     * JSON primitive value: string, number, boolean, null.
     */
    static final int TYPE_VALUE = 0;

    /**
     * The left brace token.
     */
    static final int TYPE_LEFT_BRACE = 1;

    /**
     * The right brace token.
     */
    static final int TYPE_RIGHT_BRACE = 2;

    /**
     * The left square bracket token.
     */
    static final int TYPE_LEFT_SQUARE = 3;

    /**
     * The right square bracket token.
     */
    static final int TYPE_RIGHT_SQUARE = 4;

    /**
     * A comma.
     */
    static final int TYPE_COMMA = 5;

    /**
     * A colon.
     */
    static final int TYPE_COLON = 6;

    /**
     * End of file token.
     */
    static final int TYPE_EOF = -1;

    /**
     * The token type; one of the values of the constants on this class.
     */
    final int type;

    /**
     * The actual token value.
     */
    final Object value;

    /**
     * Construct a new lexical token.
     * 
     * @param type
     *            The token type.
     * @param value
     *            The token value.
     */
    Yytoken(final int type, final Object value) {
        this.type = type;
        this.value = value;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.valueOf(type + "=>|" + value + "|");
    }
}
