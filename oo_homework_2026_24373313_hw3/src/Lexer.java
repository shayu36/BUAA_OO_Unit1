import java.util.ArrayList;

public class Lexer {
    private final ArrayList<Token> tokens = new ArrayList<>();
    private int index = 0;
    
    public Lexer(String input) {
        int pos = 0;
        int len = input.length();
        while (pos < len) {
            if (pos + 1 < len && input.charAt(pos) == '=' && input.charAt(pos + 1) == '=') {
                tokens.add(new Token(Token.Type.EQ, "=="));
                pos += 2;
                continue;
            }
            int nextPos = readDerivativeToken(input, pos, len);
            if (nextPos != -1) {
                pos = nextPos;
            } else if (input.charAt(pos) == '{') {
                tokens.add(new Token(Token.Type.LBRACE, "{"));
                pos++;
            } else if (input.charAt(pos) == '}') {
                tokens.add(new Token(Token.Type.RBRACE, "}"));
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
            } else if (input.charAt(pos) == 'x' || input.charAt(pos) == 'y') {
                tokens.add(new Token(Token.Type.VAR, String.valueOf(input.charAt(pos))));
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
    
    private int readDerivativeToken(String input, int pos, int len) {
        if (pos + 3 < len && input.charAt(pos) == 'g') {
            tokens.add(new Token(Token.Type.GRAD, "grad"));
            return pos + 4;
        }
        if (pos + 1 < len && input.charAt(pos) == 'd' && input.charAt(pos + 1) == 'x') {
            tokens.add(new Token(Token.Type.DX, "dx"));
            return pos + 2;
        }
        if (pos + 1 < len && input.charAt(pos) == 'd' && input.charAt(pos + 1) == 'y') {
            tokens.add(new Token(Token.Type.DY, "dy"));
            return pos + 2;
        }
        if (input.charAt(pos) == '(') {
            tokens.add(new Token(Token.Type.L, "("));
            return pos + 1;
        }
        if (input.charAt(pos) == ')') {
            tokens.add(new Token(Token.Type.R, ")"));
            return pos + 1;
        }
        return -1;
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