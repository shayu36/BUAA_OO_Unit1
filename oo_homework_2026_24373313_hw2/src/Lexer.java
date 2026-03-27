import java.util.ArrayList;

public class Lexer {
    private final ArrayList<Token> tokens = new ArrayList<>();
    private int index = 0;
    
    public Lexer(String input) {
        int pos = 0;
        while (pos < input.length()) {
            if (pos + 1 < input.length() && input.charAt(pos) == '='
                && input.charAt(pos + 1) == '=') {
                tokens.add(new Token(Token.Type.EQ, "=="));
                pos += 2;
                continue;
            }
            if (input.charAt(pos) == '(') {
                tokens.add(new Token(Token.Type.L, "("));
                pos++;
            } else if (input.charAt(pos) == ')') {
                tokens.add(new Token(Token.Type.R, ")"));
                pos++;
            } else if (input.charAt(pos) == '[') {
                tokens.add(new Token(Token.Type.LB, "["));
                pos++;
            } else if (input.charAt(pos) == ']') {
                tokens.add(new Token(Token.Type.RB, "]"));
                pos++;
            } else if (input.charAt(pos) == '=') {
                tokens.add(new Token(Token.Type.ASSIGN, "="));
                pos++;
            } else if (input.charAt(pos) == '+') {
                tokens.add(new Token(Token.Type.ADD, "+"));
                pos++;
            } else if (input.charAt(pos) == '-') {
                tokens.add(new Token(Token.Type.SUB, "-"));
                pos++;
            } else if (input.charAt(pos) == '*') {
                tokens.add(new Token(Token.Type.MUL, "*"));
                pos++;
            } else if (input.charAt(pos) == 'x') {
                tokens.add(new Token(Token.Type.VAR, "x"));
                pos++;
            } else if (input.charAt(pos) == '^') {
                tokens.add(new Token(Token.Type.EXP, "^"));
                pos++;
            } else if (input.charAt(pos) == '?') {
                tokens.add(new Token(Token.Type.QUES, "?"));
                pos++;
            } else if (input.charAt(pos) == ':') {
                tokens.add(new Token(Token.Type.COLON, ":"));
                pos++;
            } else if (input.charAt(pos) == 'f') {
                tokens.add(new Token(Token.Type.F, "f"));
                pos++;
            } else if (input.charAt(pos) == 'e') {
                tokens.add(new Token(Token.Type.EXP_FUNC, "exp"));
                pos += 3;
            } else {
                pos = readNumber(input, pos);
            }
        }
    }
    
    private int readNumber(String input, int pos) {
        int cur = pos;
        StringBuilder sb = new StringBuilder();
        while (cur < input.length() && Character.isDigit(input.charAt(cur))) {
            sb.append(input.charAt(cur));
            cur++;
        }
        tokens.add(new Token(Token.Type.NUM, sb.toString()));
        return cur;
    }
    
    public void next() {
        index++;
    }
    
    public String peek() {
        return tokens.get(index).getContent();
    }
    
    public boolean isEnd() {
        return index >= tokens.size();
    }
}