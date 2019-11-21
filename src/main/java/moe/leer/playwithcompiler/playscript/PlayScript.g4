grammar PlayScript;

import CommonLexer;

@header {
package moe.leer.playwithcompiler.playscript;
}

functionDeclare
    : typeTypeOrVoid? IDENTIFIER formalParams ('[' ']')*
      (THROWS qualifiedNameList)?
      functionBody
    ;

functionBody
    : block
//    | ';'
    ;

qualifiedNameList
    : qualifiedName (',' qualifiedName)*
    ;

formalParams
    : '(' formalParamList? ')'
    ;

formalParamList
    : formalParam (',' formalParam)* (',' lastFormalParam)?
    ;

formalParam
    : variableModifier* typeType variableDeclaratorId
    ;

lastFormalParam
    : variableModifier* typeType '...' variableDeclaratorId
    ;

variableModifier
    : FINAL
    ;

qualifiedName
    : IDENTIFIER ('.' IDENTIFIER)*
    ;

fieldDeclare
    //: typeType variableDeclarators
    : variableDeclarators
    ;

constructorDeclare
    : IDENTIFIER formalParams (THROWS qualifiedNameList)? constructorBody=block
    ;

variableDeclarators
    : typeType variableDeclarator (',' variableDeclarator)*
    ;

variableDeclarator
    : variableDeclaratorId ('=' variableInitializer)?
    ;

variableDeclaratorId
    : IDENTIFIER ('[' ']')*
    ;

variableInitializer
    : arrayInitializer
    | expression
    ;

arrayInitializer
    : '{' (variableInitializer (',' variableInitializer)* (',')? )? '}'
    ;

// Statement

toplevelStatement
    : blockStatements
    ;

block
    : '{' blockStatements '}'
    ;

blockStatements
    : blockStatement*
    ;

blockStatement
    : variableDeclarators
    | statement
    | functionDeclare
//    | classDeclare
    ;

statement
    : blockLabel=block
    | IF parBooleanExpression statement (ELSE statement)?
    //todo switch
    | FOR '(' forBlock ')' statement
    | WHILE parBooleanExpression statement
    | DO statement WHILE parExpression
    | RETURN expression?
    | BREAK IDENTIFIER?
//    | SEMI
    | statementExpression=expression // 表达式语句
    | identifierLabel=IDENTIFIER ':' statement // label
    ;

forBlock
    : enhancedForBlock
    | forInit? ';' booleanExpression? ';' forUpdate=expressionList?
    ;

enhancedForBlock
    : typeType variableDeclaratorId ':' expression
    ;

forInit
    : variableDeclarators
    | expressionList
    ;

// EXPRESSIONS

parExpression
    : '(' expression ')'
    ;

parBooleanExpression
    : '(' booleanExpression ')'
    ;

booleanExpression
    : BOOL_LITERAL
    | IDENTIFIER
    | expression ('<' '<' | '>' '>' '>' | '>' '>') expression // <<, >>>, >>
    | expression bop=('<=' | '>=' | '>' | '<') expression
    | expression bop=('==' | '!=') expression
    | expression bop='&&' expression
    | expression bop='||' expression
    ;

// 优先级以产生式的顺序来表达
// bop -> binary op
expression
    : primary
    | expression '[' expression ']' // array
    | functionCall
    | '(' expression ')'
    | expression postfix=('++'|'--')
    | prefix=('+'|'-'|'++'|'--') expression
    | prefix=('!'|'~') expression
    | expression bop=('*'|'/'|'%') expression
    | expression bop=('+'|'-') expression
    | expression ('<' '<' | '>' '>' '>' | '>' '>') expression // <<, >>>, >>
    | expression bop=('<=' | '>=' | '>' | '<') expression
    | expression bop=('==' | '!=') expression
    | expression bop='&' expression
    | expression bop='^' expression
    | expression bop='|' expression
    | expression bop='&&' expression
    | expression bop='||' expression
    | expression bop='?' expression ':' expression
    | <assoc=right> expression
      bop=('=' | '+=' | '-=' | '*=' | '/=' | '&=' | '|=' | '^=' | '>>=' | '>>>=' | '<<=' | '%=')
      expression
    ;

functionCall
    : IDENTIFIER '(' expressionList? ')'
    | THIS '(' expressionList? ')'  // this()
    | SUPER '(' expressionList? ')' // super call
    ;

expressionList
    : expression (',' expression)*
    ;

// TYPE

typeList
    : typeType (',' typeType)*
    ;

typeType
    : (classOrInterfaceType| functionType | primitiveType) ('[' ']')*
    ;

classOrInterfaceType
    : IDENTIFIER ('.' IDENTIFIER)*
    //: IDENTIFIER
    ;

functionType
    : FUNCTION typeTypeOrVoid '(' typeList? ')'
    ;

typeTypeOrVoid
    : typeType
    | 'void'
    ;

primitiveType
    : BOOLEAN
    | CHAR
    | BYTE
    | SHORT
    | INT
    | LONG
    | FLOAT
    | DOUBLE
    | STRING
    ;

primary
    : literal
    | IDENTIFIER
    ;

literal
    : integerLiteral
    | floatLiteral
    | CHAR_LITERAL
    | STRING_LITERAL
    | BOOL_LITERAL
    | NULL_LITERAL
    ;

integerLiteral
    : DECIMAL_LITERAL
    | HEX_LITERAL
    | OCT_LITERAL
    | BINARY_LITERAL
    ;

floatLiteral
    : FLOAT_LITERAL
    | HEX_FLOAT_LITERAL
    ;