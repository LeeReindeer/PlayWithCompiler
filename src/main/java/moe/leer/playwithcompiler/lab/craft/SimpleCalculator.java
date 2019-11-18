package moe.leer.playwithcompiler.lab.craft;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Grammar:
 * <p>
 * additiveExpression
 * : multiplicativeExpression
 * | multiplicativeExpression Plus additiveExpression
 * ;
 * <p>
 * multiplicativeExpression
 * :  primaryExpression
 * |  primaryExpression Star multiplicativeExpression
 * <p>
 * primaryExpression
 * :  IntLiteral
 * | Identifier
 * | LeftParen additiveExpression RightParen
 * <p>
 * 最左推导
 *
 * @author leer
 * Created at 11/14/19 9:37 PM
 */
public class SimpleCalculator {

  private boolean debug;

  public SimpleCalculator(boolean debug) {
    this.debug = debug;
  }

  public static void main(String[] args) {
    String script = "int a = (2+3)*5;";
    SimpleLexer lexer = new SimpleLexer();
    TokenReader tokens = lexer.tokenize(script);
    SimpleCalculator calculator = new SimpleCalculator(true);

    // test integer declare
    try {
      SimpleASTNode node = calculator.intDeclare(tokens);
      calculator.dumpAST(node, "");
    } catch (Throwable t) {
      System.out.println(t.getMessage());
    }

    // test expression
    script = "2+3*5";
    System.out.println("calculate: " + script);
    System.out.println(calculator.evaluate(script));

    // test paren expression
    script = "(2+3)*5";
    System.out.println("calculate: " + script);
    System.out.println(calculator.evaluate(script));

    // test grammar error
    script = "2+";
    System.out.println("calculate: " + script);
    System.out.println(calculator.evaluate(script));

    script = "2+3+4;";
    System.out.println("calculate: " + script);
    System.out.println(calculator.evaluate(script));
  }

  /**
   * Integer type variable definition
   *
   * @param tokens token reader
   * @return AST root node
   */
  public SimpleASTNode intDeclare(TokenReader tokens) {
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
        }
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
   * 上级算法调用下级算法。优先级越高的表达式，调用深度越深。
   * <p>
   * additiveExpression
   * : multiplicativeExpression
   * | multiplicativeExpression Plus additiveExpression         // additiveExpression 写在右边来避免左递归，但是会造成结合性问题
   * ;
   * <p>
   * 改写为：
   * add
   * :  mul |  add'
   * ;
   * <p>
   * add'
   * : + mul add' | ε
   * ;
   * <p>
   * ε (epsilon) 表示空集
   * <p>
   * 再改写为NBNF:
   * <p>
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
   * :  primaryExpression
   * |  primaryExpression Star multiplicativeExpression
   * <p>
   * <p>
   * mul
   * : pri (Star pri)*
   *
   * @param tokens
   * @return
   */
  private SimpleASTNode multiplicative(TokenReader tokens) {
    SimpleASTNode child1 = primary(tokens);
    SimpleASTNode node = child1;
    Token token;
    while ((token = tokens.peek()) != null &&
        (token.getType() == TokenType.Star || token.getType() == TokenType.Slash)) {
      tokens.read();
      SimpleASTNode child2 = primary(tokens);
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
   * primaryExpression
   * :  IntLiteral
   * | Identifier
   * | LeftParen additiveExpression RightParen
   *
   * @param tokens
   * @return
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
   * Print script result, and calculate step
   *
   * @param script
   */
  public int evaluate(String script) {
    try {
      ASTNode tree = parse(script);
      if (debug) {
        dumpAST(tree, "");
      }
      return evaluate(tree, "");
    } catch (Throwable t) {
      System.out.println("Syntax error: " + t.getMessage());
    }
    return 0;
  }

  /**
   * DFS
   *
   * @param node
   * @param indent
   * @return
   */
  private int evaluate(ASTNode node, String indent) {
    int result = 0;
    logd(indent + "Calculating: " + node.getType());
    switch (node.getType()) {
      case Programm:
        for (ASTNode child : node.getChildren()) {
          result = evaluate(child, indent + "\t");
        }
        break;
      case Additive:
        ASTNode child1 = node.getChildren().get(0);
        int value1 = evaluate(child1, indent + "\t");
        ASTNode child2 = node.getChildren().get(1);
        int value2 = evaluate(child2, indent + "\t");
        if (node.getText().equals("+")) {
          result = value1 + value2;
        } else {
          result = value1 - value2;
        }
        break;
      case Multiplicative:
        child1 = node.getChildren().get(0);
        value1 = evaluate(child1, indent + "\t");
        child2 = node.getChildren().get(1);
        value2 = evaluate(child2, indent + "\t");
        if (node.getText().equals("*")) {
          result = value1 * value2;
        } else {
          if (value2 == 0) {
            //todo
            throw new ArithmeticException("/ by zero");
          }
          result = value1 / value2;
        }
        break;
      case Identifier:
        break;
      case IntLiteral:
        result = Integer.parseInt(node.getText());
        break;
      default:
        break;
    }
    logd(indent + "Result: " + result);
    return result;
  }

  /**
   * Token parse
   *
   * @param code
   */
  private ASTNode parse(String code) {
    SimpleLexer lexer = new SimpleLexer();
    TokenReader tokens = lexer.tokenize(code);
    return buildAST(tokens);
  }

  private SimpleASTNode buildAST(TokenReader tokens) {
    SimpleASTNode root = new SimpleASTNode(ASTNodeType.Programm, "Calculator");
    SimpleASTNode child = additive(tokens);
    if (child != null) {
      root.addChild(child);
    }
    return root;
  }

  /**
   * DFS print AST tree
   *
   * @param node   root node
   * @param indent indent char, every tree level plus one tab
   */
  public void dumpAST(ASTNode node, String indent) {
    System.out.println(indent + node.getType() + " " + node.getText());
    for (ASTNode child : node.getChildren()) {
      dumpAST(child, indent + "\t");
    }
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
