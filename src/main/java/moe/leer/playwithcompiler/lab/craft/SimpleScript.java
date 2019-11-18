package moe.leer.playwithcompiler.lab.craft;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;

/**
 * @author leer
 * Created at 11/18/19 9:57 AM
 */
public class SimpleScript {

  private boolean debug;
  private HashMap<String, Integer> intVariableMap = new HashMap<>();

  public static void main(String[] args) {
    SimpleScript script = new SimpleScript();
    if (args.length == 1 && args[0].equals("-v")) {
      script.debug = true;
    } else if (args.length != 0) {
      System.out.println("Usage:\n\tjava SimpleScript\n\t-v: print debug messages");
      return;
    }

    SimpleParser parser = new SimpleParser(script.debug);
    StringBuilder scriptText = new StringBuilder();
    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
    while (true) {
      System.out.println("\n>");
      try {
        String line = reader.readLine().trim();
        if (line.equals("exit();")) {
          break;
        }
        scriptText.append(line).append("\n"); // next statement
        if (line.endsWith(";")) {
          ASTNode ast = parser.parse(scriptText.toString());
          if (script.debug) {
            parser.dumpAST(ast, "");
          }
          script.evaluate(ast, "");
          scriptText.setLength(0);
        }
      } catch (Throwable t) {
        System.out.println(t.getMessage());
        scriptText.setLength(0);
      }
    }
  }

  /**
   * DFS
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
        String varName = node.getText();
        if (intVariableMap.containsKey(varName)) {
          Integer value = intVariableMap.get(varName);
          if (value != null) {
            result = intVariableMap.get(varName);
          } else {
            throw new IllegalStateException("variable " + varName + " is not initialized");
          }
        } else {
          throw new IllegalStateException("variable " + varName + " is not declared");
        }
        break;
      case IntLiteral:
        result = Integer.parseInt(node.getText());
        break;
      case AssignmentStmt:
        varName = node.getText();
        if (!intVariableMap.containsKey(varName)) {
          throw new IllegalStateException("variable " + varName + " is not declared");
        }
        // fallthrough
      case IntDeclaration:
        varName = node.getText();
        child1 = node.getChildren().get(0);
        if (child1 != null) {
          result = evaluate(child1, indent + "\t");
          intVariableMap.put(varName, result);
        } else {
          intVariableMap.put(varName, null);
        }
        break;
      default:
        break;
    }
    if (debug) {
      System.out.println(indent + "Result: " + result);
    } else if (indent.equals("\t")) { // 顶层的语句
      if (node.getType() == ASTNodeType.IntDeclaration || node.getType() == ASTNodeType.AssignmentStmt) {
        System.out.println(node.getText() + ": " + result);
      }else if (node.getType() != ASTNodeType.Programm){
        System.out.println(result);
      }
    }
    return result;
  }


  private void logd(String s) {
    if (this.debug) {
      System.out.println(s);
    }
  }
}