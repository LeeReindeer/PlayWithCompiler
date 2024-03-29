package moe.leer.playwithcompiler.lab.craft;

/**
 * @author leer
 * Created at 11/14/19 9:58 PM
 */
public enum ASTNodeType {
  Programm,           //程序入口，根节点

  IntDeclaration,     //整型变量声明
  ExpressionStmt,     //表达式语句，即表达式后面跟个分号
  NegativeExpression,    // 负数，负表达式

  AssignmentStmt,     //赋值语句
  PlusAssignmentStmt,// +=
  MinusAssignmentStmt,// -=

  //unary operators
  PlusUnary,//++a
  MinusUnary,//--a
  UnaryPlus, //a++
  UnaryMinus, //a--

  Primary,            //基础表达式
  Multiplicative,     //乘法表达式
  Additive,           //加法表达式

  Identifier,         //标识符
  IntLiteral          //整型字面量
}
