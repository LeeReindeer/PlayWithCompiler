package moe.leer.playwithcompiler.lab.craft;

import java.util.ArrayList;
import java.util.List;

/**
 * @author leer
 * Created at 11/11/19 3:27 PM
 */
public class SimpleLexer {

  List<Token> tokens;
  StringBuffer tokenText;
  SimpleToken currToken;

  public static void main(String[] args) {
    SimpleLexer lexer = new SimpleLexer();
    lexer.tokenize("int age = 21");
    lexer.print();

    lexer.tokenize("intA >= 115");
    lexer.print();

    lexer.tokenize("in > 115");
    lexer.print();

    lexer.tokenize("2+3*5");
    lexer.print();

    lexer.tokenize("int$ 100"); // illegal
  }

  public void tokenize(String stream) {

    DfaState dfaState = DfaState.Initial;

    tokens = new ArrayList<Token>();
    tokenText = new StringBuffer();
    currToken = new SimpleToken();

    for (char ch : stream.toCharArray()) {
      switch (dfaState) {
        case Initial:
          dfaState = saveTokenThenInitState(ch);
          break;
        case Id:
          if (isAlpha(ch) || isDigit(ch) || ch == '_') {
            tokenText.append(ch); // stay Id state
          } else {
            dfaState = saveTokenThenInitState(ch); // save token, go to next state
          }
          break;
        case GT:
          if (ch == '=') {
            dfaState = DfaState.GE;
            currToken.type = TokenType.GE;
            tokenText.append(ch);
          } else {
            dfaState = saveTokenThenInitState(ch);
          }
          break;
        case Assignment:
        case GE:
        case Plus:
        case Minus:
        case Star:
        case Slash:
        case SemiColon:
        case LeftParen:
        case RightParen:
          dfaState = saveTokenThenInitState(ch);
          break;
        case IntLiteral:
          if (isDigit(ch)) {
            tokenText.append(ch); // stay state
          } else {
            dfaState = saveTokenThenInitState(ch);
          }
          break;
        case Id_int1:
          if (ch == 'n') {
            dfaState = DfaState.Id_int2;
            tokenText.append(ch);
          } else if (isAlpha(ch) || isDigit(ch) || ch == '_') {
            dfaState = DfaState.Id;
            currToken.type = TokenType.Identifier;
            tokenText.append(ch);
          } else {
            dfaState = saveTokenThenInitState(ch);
          }
          break;
        case Id_int2:
          if (ch == 't') {
            dfaState = DfaState.Id_int3;
            tokenText.append(ch);
          } else if (isAlpha(ch) || isDigit(ch) || ch == '_') {
            dfaState = DfaState.Id;
            currToken.type = TokenType.Identifier;
            tokenText.append(ch);
          } else {
            dfaState = saveTokenThenInitState(ch);
          }
          break;
        case Id_int3:
          if (isBlank(ch)) {
            currToken.type = TokenType.Int;
            dfaState = saveTokenThenInitState(ch);
          } else if (isAlpha(ch) || isDigit(ch) || ch == '_') {
            dfaState = DfaState.Id;
            currToken.type = TokenType.Identifier;
            tokenText.append(ch);
          } else {
            dfaState = saveTokenThenInitState(ch);
          }
          break;
        default:
          throw new IllegalStateException("Unexpected token: " + ch);
      }
    }

    if (tokenText.length() > 0) {
      currToken.text = tokenText.toString();
      tokens.add(currToken);
    }
  }

  /**
   * Save last token, DFA start from initial state, return next state
   *
   * @param ch first char
   */
  private DfaState saveTokenThenInitState(char ch) {
    // re-start from initial state, save last token
    if (tokenText.length() > 0) {
      currToken.text = tokenText.toString();
      tokens.add(currToken);

      tokenText = new StringBuffer();
      currToken = new SimpleToken();
    }

    DfaState dfaState = DfaState.Initial;
    if (isAlpha(ch)) {
      if (ch == 'i') {
        dfaState = DfaState.Id_int1;
      } else {
        dfaState = DfaState.Id;
      }
      currToken.type = TokenType.Identifier;
      tokenText.append(ch);
    } else if (isDigit(ch)) {
      dfaState = DfaState.IntLiteral;
      currToken.type = TokenType.IntLiteral;
      tokenText.append(ch);
    } else if (ch == '>') {
      dfaState = DfaState.GT;
      currToken.type = TokenType.GT;
      tokenText.append(ch);
    } else if (ch == '+') {
      dfaState = DfaState.Plus;
      currToken.type = TokenType.Plus;
      tokenText.append(ch);
    } else if (ch == '-') {
      dfaState = DfaState.Minus;
      currToken.type = TokenType.Minus;
      tokenText.append(ch);
    } else if (ch == '*') {
      dfaState = DfaState.Star;
      currToken.type = TokenType.Star;
      tokenText.append(ch);
    } else if (ch == '/') {
      dfaState = DfaState.Slash;
      currToken.type = TokenType.Slash;
      tokenText.append(ch);
    } else if (ch == ';') {
      dfaState = DfaState.SemiColon;
      currToken.type = TokenType.SemiColon;
      tokenText.append(ch);
    } else if (ch == '(') {
      dfaState = DfaState.LeftParen;
      currToken.type = TokenType.LeftParen;
      tokenText.append(ch);
    } else if (ch == ')') {
      dfaState = DfaState.RightParen;
      currToken.type = TokenType.RightParen;
      tokenText.append(ch);
    } else if (ch == '=') {
      dfaState = DfaState.Assignment;
      currToken.type = TokenType.Assignment;
      tokenText.append(ch);
    } else if (isBlank(ch)) {
      dfaState = DfaState.Initial;
    } else {
      throw new IllegalStateException("Illegal token: " + ch);
    }
    return dfaState;
  }

  public void print() {
    System.out.println("Type\t\tText");
    for (Token token : tokens) {
      System.out.println(token.toString());
    }
  }

  enum DfaState {
    Initial,

    If, Id_if1, Id_if2, Else, Id_else1, Id_else2, Id_else3, Id_else4, Int, Id_int1, Id_int2, Id_int3, Id, GT, GE,

    Assignment,

    Plus, Minus, Star, Slash,

    SemiColon,
    LeftParen,
    RightParen,

    IntLiteral
  }

  //是否是字母
  public static boolean isAlpha(int ch) {
    return ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z';
  }

  //是否是数字
  public static boolean isDigit(int ch) {
    return ch >= '0' && ch <= '9';
  }

  //是否是空白字符
  public static boolean isBlank(int ch) {
    return ch == ' ' || ch == '\t' || ch == '\n';
  }

  /**
   * Token的一个简单实现。只有类型和文本值两个属性。
   */
  private final class SimpleToken implements Token {
    //Token类型
    private TokenType type = null;

    //文本值
    private String text = null;

    public TokenType getType() {
      return type;
    }

    public String getText() {
      return text;
    }

    @Override
    public String toString() {
      return type + "\t\t" + text;
    }
  }

  private class SimpleTokenReader implements TokenReader {

    private List<Token> tokens;
    // current read position
    private int pos = 0;

    public SimpleTokenReader(List<Token> tokens) {
      this.tokens = tokens;
    }

    public Token read() {
      if (pos < tokens.size()) {
        return tokens.get(pos++);
      }
      return null;
    }

    public Token peek() {
      if (pos < tokens.size()) {
        return tokens.get(pos);
      }
      return null;
    }

    public void unread() {
      if (pos > 0) {
        pos--;
      }
    }

    public int getPosition() {
      return pos;
    }

    public void setPosition(int position) {
      this.pos = position;
    }
  }
}
