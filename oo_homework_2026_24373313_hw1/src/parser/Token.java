package parser;

public class Token {
    private final String content;

    public Token(Type type, String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public enum Type {
        ADD, SUB, MUL, DIV, L, R, NUM, VAR, EXP
    }
}
