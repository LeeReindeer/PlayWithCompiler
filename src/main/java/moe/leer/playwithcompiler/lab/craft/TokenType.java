package moe.leer.playwithcompiler.lab.craft;

/**
 * @author leer
 * Created at 11/11/19 3:29 PM
 */
public enum TokenType {
  Plus,   // +
  Minus,  // -
  Star,   // *
  Slash,  // /

  GE,     // >=
  GT,     // >
  EQ,     // ==
  NEQ,    // !=
  LE,     // <=
  LT,     // <

  SemiColon, // ;
  LeftParen, // (
  RightParen,// )

  Assignment,// =

  If,
  Else,

  Int,

  Identifier,     //标识符

  IntLiteral,     //整型字面量
  StringLiteral   //字符串字面量
}
