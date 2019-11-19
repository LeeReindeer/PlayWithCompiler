grammar HelloScript;

import Hello;

stmt
  : intDeclare
  | expStmt
  | assignmentStmt
  ;

expStmt
  : add? ';'
  ;

intDeclare
  : Int Id ('=' add)? ';'
  ;

assignmentStmt
  : Id AssignmentOP add ';'
  ;

add
  : mul
  | add '+' mul
  | add '-' mul
  ;

mul
  : unary
  | mul '*' unary
  | mul '/' unary
  ;

unary
  : pri '--'
  | pri '++'
  | '--' pri
  | '++' pri
  | pri
  ;

pri
  : IntLiteral
  | Id
  | '(' add ')'
  ;
