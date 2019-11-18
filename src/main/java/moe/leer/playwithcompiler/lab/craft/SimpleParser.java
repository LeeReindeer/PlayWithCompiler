package moe.leer.playwithcompiler.lab.craft;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 语法分析，生成AST
 * 规则如下：
 * <p>
 * programm -> intDeclare | expressionStatement | assignmentStatement
 * intDeclare -> 'int' Id ( = additive) ';'
 * expressionStatement -> addtive ';'
 * addtive -> multiplicative ( (+ | -) multiplicative)*
 * multiplicative -> primary ( (* | /) primary)*
 * primary -> IntLiteral | Id | (additive)
 *
 * @author leer
 * Created at 11/18/19 10:03 AM
 */
public class SimpleParser {

  private boolean debug;

  public SimpleParser(boolean debug) {
    this.debug = debug;
  }

  public SimpleParser() {
    debug = true;
  }

  public static void main(String[] args) {
    SimpleParser parser = new SimpleParser();
    String script = null;
    ASTNode tree = null;

    try {
      script = "int age = 45+2; age= 20; age+10*2;";
//      script = "int age = 45;age+10;";
      System.out.println("parse: " + script);
      tree = parser.parse(script);
      parser.dumpAST(tree, "|", "-");
    } catch (Throwable t) {
      System.out.println(t.getMessage());
    }

    try {
      script = "int a = - --b;";
      System.out.println("parse: " + script);
      tree = parser.parse(script);
      parser.dumpAST(tree, "|", "-");
    } catch (Throwable t) {
      System.out.println(t.getMessage());
    }

    try {
      script = "int a = 1 - --b;";
      System.out.println("parse: " + script);
      tree = parser.parse(script);
      parser.dumpAST(tree, "|", "-");
    } catch (Throwable t) {
      System.out.println(t.getMessage());
    }

    try {
      script = "int a = ---b;"; // error
      System.out.println("parse: " + script);
      tree = parser.parse(script);
      parser.dumpAST(tree, "|", "-");
    } catch (Throwable t) {
      t.printStackTrace();
      System.out.println(t.getMessage());
    }

    //测试异常语法
    try {
      script = "2+3+;";
      System.out.println("parse: " + script);
      tree = parser.parse(script);
      parser.dumpAST(tree, "");
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }

    //测试异常语法
    try {
      script = "2+3*;";
      System.out.println("parse: " + script);
      tree = parser.parse(script);
      parser.dumpAST(tree, "");
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
  }

  /**
   * Token parse
   *
   * @param code
   */
  public ASTNode parse(String code) {
    SimpleLexer lexer = new SimpleLexer();
    TokenReader tokens = lexer.tokenize(code);
    return programEntrance(tokens);
  }

  /**
   * program: statement+
   * statement: intDeclaration| expressionStatement| assignmentStatement;
   */
  private SimpleASTNode programEntrance(TokenReader tokens) {
    SimpleASTNode root = new SimpleASTNode(ASTNodeType.Programm, "SimpleScript");
    while (tokens.peek() != null) { // statement+
      SimpleASTNode child = null;
      child = intDeclareStatement(tokens);
      if (child == null) {
        child = expressionStatement(tokens);
      }
      if (child == null) {
        child = assignmentStatement(tokens);
      }
      if (child == null) {
        throw new IllegalStateException("Unsupported statement");
      } else {
        root.addChild(child);
      }
    }
    return root;
  }

  /**
   * Integer type variable definition
   * <p>
   * intDeclaration : 'int' Identifier ( '=' additiveExpression)? ';';
   */
  private SimpleASTNode intDeclareStatement(TokenReader tokens) {
    SimpleASTNode node = null;
    Token token = tokens.peek(); // preread
    if (token != null && token.getType() == TokenType.Int) {
      token = tokens.read(); // read int
      token = tokens.peek();
      if (token != null && token.getType() == TokenType.Identifier) {
        token = tokens.read(); // read id
        // 变量名记录到int节点的文本值中，也可以创建一个变量子节点
        node = new SimpleASTNode(ASTNodeType.IntDeclaration, token.getText());
        token = tokens.peek();
        if (token != null && token.getType() == TokenType.Assignment) {
          token = tokens.read(); // read =
          SimpleASTNode child = additive(tokens);
          if (child == null) {
            throw new IllegalStateException("invalid variable initialization, expecting an expression");
          } else {
            node.addChild(child);
          }
        } // 变量初始化部分可以没有
      } else {
        throw new IllegalStateException("variable name expected");
      }

      if (node != null) {
        token = tokens.peek();
        if (token != null && token.getType() == TokenType.SemiColon) {
          tokens.read();
        } else {
          throw new IllegalStateException("invalid statement, expecting semicolon");
        }
      }
    }
    return node;
  }

  /**
   * expressionStatement: additive ';' ;
   */
  private SimpleASTNode expressionStatement(TokenReader tokens) {
    int pos = tokens.getPosition();
    SimpleASTNode node = additive(tokens);
    if (node != null) {
      if (tokens.peek() != null && tokens.peek().getType() == TokenType.SemiColon) {
        tokens.read();
      } else {
        node = null;
        tokens.unread2(pos);
      }
    }
    return node;
  }

  /**
   * assignmentStatement : Identifier ('='|'+='|'-=') additiveExpression ';';
   */
  private SimpleASTNode assignmentStatement(TokenReader tokens) {
    SimpleASTNode node = null;
    Token token = tokens.peek();
    if (token != null && token.getType() == TokenType.Identifier) {
      Token id = tokens.read();
      token = tokens.peek();
      if (token != null &&
          (token.getType() == TokenType.Assignment ||
              token.getType() == TokenType.PlusAssignment ||
              token.getType() == TokenType.MinusAssignment
          )) {
        tokens.read();
        SimpleASTNode child = additive(tokens);
        if (child != null) {
          node = switch (token.getType()) {
            case Assignment -> new SimpleASTNode(ASTNodeType.AssignmentStmt, id.getText());
            case PlusAssignment -> new SimpleASTNode(ASTNodeType.PlusAssignmentStmt, id.getText());
            case MinusAssignment -> new SimpleASTNode(ASTNodeType.MinusAssignmentStmt, id.getText());
            default -> throw new IllegalStateException("Unexpected value: " + token.getType());
          };
          node.addChild(child);

          // 验证分号
          token = tokens.peek();
          if (token != null && token.getType() == TokenType.SemiColon) {
            tokens.read();
          } else {
            throw new IllegalStateException("invalid statement, expecting semicolon");
          }
        } else {
          throw new IllegalStateException("expect illegal additive expression");
        }
      } else { // not assignment, unread id, 其实这里也没有其他可能性了，可直接报错
        node = null;
        tokens.unread();
      }
    }
    return node;
  }

  /**
   * 上级算法调用下级算法。优先级越高的表达式，调用深度越深。
   * add: mul (+ mul)*;
   * * 表示重复零次或多次
   * 使用循环来代理递归
   *
   * @param tokens
   * @return
   */
  private SimpleASTNode additive(TokenReader tokens) {
    SimpleASTNode child1 = multiplicative(tokens);
    SimpleASTNode node = child1;
    if (child1 != null) {
      Token token;
      while ((token = tokens.peek()) != null &&
          (token.getType() == TokenType.Plus || token.getType() == TokenType.Minus)) {
        token = tokens.read(); // read '+' or '-'
        SimpleASTNode child2 = multiplicative(tokens);
        if (child2 == null) {
          throw new IllegalStateException("invalid additive expression, expecting the right part");
        }
        node = new SimpleASTNode(ASTNodeType.Additive, token.getText()); // + or -
        node.addChild(child1);
        node.addChild(child2);
        child1 = node; // let it be the left child
      }
    }
    return node;
  }

  /**
   * multiplicativeExpression
   * :  unary
   * |  unary Star multiplicativeExpression
   * <p>
   * <p>
   * mul
   * : unary (Star unary)*
   */
  private SimpleASTNode multiplicative(TokenReader tokens) {
    SimpleASTNode child1 = unary(tokens);
    SimpleASTNode node = child1;
    Token token;
    while ((token = tokens.peek()) != null &&
        (token.getType() == TokenType.Star || token.getType() == TokenType.Slash)) {
      tokens.read();
      SimpleASTNode child2 = unary(tokens);
      if (child2 == null) {
        throw new IllegalStateException("invalid multiplicative expression, expecting the right part");
      }
      node = new SimpleASTNode(ASTNodeType.Multiplicative, token.getText()); // * or /
      node.addChild(child1);
      node.addChild(child2);
      child1 = node;
    }
    return node;
  }

  /**
   * unary: primary | ++primary | --primary;
   */
  private SimpleASTNode unary(TokenReader tokens) {
    SimpleASTNode node = null;
    Token token = tokens.peek();

    if (token == null) return node;
    if (token.getType() == TokenType.PlusUnary) {
      tokens.read();
      Token next = tokens.peek();
      if (next != null && next.getType() == TokenType.Identifier) { // Identifier only
        tokens.read(); // read id
        // only create one node
        node = new SimpleASTNode(ASTNodeType.PlusUnary, next.getText());
      } else {
        throw new IllegalStateException("variable expected");
      }
    } else if (token.getType() == TokenType.MinusUnary) {
      tokens.read();
      Token next = tokens.peek();
      if (next != null && next.getType() == TokenType.Identifier) { // Identifier only
        tokens.read(); // read id
        // only create one node
        node = new SimpleASTNode(ASTNodeType.MinusUnary, next.getText());
      } else {
        throw new IllegalStateException("variable expected");
      }
    } else {
      node = primary(tokens);
    }
    return node;
  }

  /**
   * primaryExpression
   * :  IntLiteral
   * | Identifier
   * | Identifier++
   * | Identifier--
   * | LeftParen additiveExpression RightParen
   * | '-' primaryExpression
   */
  private SimpleASTNode primary(TokenReader tokens) {
    SimpleASTNode node = null;
    Token token = tokens.peek();

    if (token == null) return node;

    if (token.getType() == TokenType.IntLiteral) {
      token = tokens.read();
      node = new SimpleASTNode(ASTNodeType.IntLiteral, token.getText());
    } else if (token.getType() == TokenType.Identifier) {
      token = tokens.read();
      node = new SimpleASTNode(ASTNodeType.Identifier, token.getText());

      Token next = tokens.peek();
      if (next == null) return node;
      if (next.getType() == TokenType.PlusUnary) {
        tokens.read();
        node = new SimpleASTNode(ASTNodeType.UnaryPlus, token.getText());
      } else if (next.getType() == TokenType.MinusUnary) {
        tokens.read();
        node = new SimpleASTNode(ASTNodeType.UnaryMinus, token.getText());
      }
    } else if (token.getType() == TokenType.Minus) {
      token = tokens.read();
      SimpleASTNode child = unary(tokens);
      if (child != null) {
        node = new SimpleASTNode(ASTNodeType.NegativeExpression, token.getText());
        node.addChild(child);
      } else {
        throw new IllegalStateException("illegal negative expression");
      }
    } else if (token.getType() == TokenType.LeftParen) {
      tokens.read(); // read "("
      node = additive(tokens); // re top-down
      if (node != null) {
        token = tokens.peek();
        if (token != null && token.getType() == TokenType.RightParen) {
          tokens.read();
        } else {
          throw new IllegalStateException("expected right paren");
        }
      } else {
        throw new IllegalStateException("expected additive expression inside paren");
      }
    }
    return node;
  }

  /**
   * DFS print AST tree
   *
   * @param node   root node
   * @param indent indent char,
   * @param tab    every tree level plus one tab
   */
  public void dumpAST(ASTNode node, String indent, String tab) {
    System.out.println(indent + node.getType() + " " + node.getText());
    for (ASTNode child : node.getChildren()) {
      dumpAST(child, indent + tab, tab);
    }
  }

  public void dumpAST(ASTNode node, String indent) {
    dumpAST(node, indent, "\t");
  }

  private void logd(String s) {
    if (this.debug) {
      System.out.println(s);
    }
  }


  private class SimpleASTNode implements ASTNode {

    SimpleASTNode parent;
    List<ASTNode> children = new ArrayList<ASTNode>();
    List<ASTNode> readonlyChildren = Collections.unmodifiableList(children);
    ASTNodeType nodeType;
    String text;


    public SimpleASTNode(ASTNodeType nodeType, String text) {
      this.nodeType = nodeType;
      this.text = text;
    }

    public ASTNode getParent() {
      return parent;
    }

    public List<ASTNode> getChildren() {
      return readonlyChildren;
    }

    public ASTNodeType getType() {
      return nodeType;
    }

    public String getText() {
      return text;
    }

    public void addChild(SimpleASTNode child) {
      children.add(child);
      child.parent = this;
    }
  }
}
