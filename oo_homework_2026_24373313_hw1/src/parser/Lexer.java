package parser;

import java.util.ArrayList;

public class Lexer {
    private final ArrayList<Token> tokens = new ArrayList<>();
    private int index = 0;

    public Lexer(String input) {
        int pos = 0;
        while (pos < input.length()) {
            if (input.charAt(pos) == '(') {
                tokens.add(new Token(Token.Type.L, "("));
                pos++;
            } else if (input.charAt(pos) == ')') {
                tokens.add(new Token(Token.Type.R, ")"));
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
            } else {
                char now = input.charAt(pos);
                StringBuilder sb = new StringBuilder();
                while (now >= '0' && now <= '9') {
                    sb.append(now);
                    pos++;
                    if (pos >= input.length()) {
                        break;
                    }
                    now = input.charAt(pos);
                }
                tokens.add(new Token(Token.Type.NUM, sb.toString()));
            }
        }
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
