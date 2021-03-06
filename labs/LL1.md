# LL(1) Parsing, Lexing

## Goal

In this lab, you will build an LL(1) lexer and parser for simple expressions that follow this grammar:

```
expr : term ('+' term)* ;            // expr is the start symbol
term : factor ('*' factor)* ;
factor : ID | INT | '(' expr ')' ;

ID  : [a-z]+ ;
INT : [0-9]+ ;
WS  : [ \r\t\n]+ -> skip ;
```

I have provided the infrastructure for you, leaving just the details of recognizing tokens and grammatical structure to you. You're free to use any resource, such as my Language Implementation Patterns book.

[Here is your lab starter kit](https://github.com/parrt/cs652/tree/master/labs/code/LL1/src).

Once again you should read all of the source code to fully understand the infrastructure I have provided. You should drill into your head the LL(1) parsing pattern exhibited by this expression parser.

## Tasks

First, build and test the lexer. You'll notice a `main` method in `ExprLexer`:

```java
public static void main(String[] args) throws IOException {
    readAndPrint(new ExprLexer(new InputStreamReader(System.in)));
}
```

which makes it easy to test the lexer all by itself before you move on to the parser.

Next, fill out the parser functions corresponding to the grammar rules `expr`, `term`, and `factor`. The parser automatically throws a `RuntimeException` exception in `match()` if there is a mismatch, but you must make a parsing decision in rule `factor` so you will have to throw an exception there.

In order to test the parser, you will use the `main` program in `Expr.java`:

```java
ExprLexer lexer = new ExprLexer(new InputStreamReader(System.in));
ExprParser parser = new ExprParser(lexer);
parser.expr();
```

Here is a sample run:

```bash
$ java Expr
1+2*3
OK
$ java Expr
(1+2)*3
OK
$ java Expr
1+ +
Exception in thread "main" java.lang.RuntimeException: token ['+':<3>] is not a valid expression element
	at ExprParser.factor(ExprParser.java:43)
	at ExprParser.term(ExprParser.java:22)
	at ExprParser.expr(ExprParser.java:17)
	at Expr.main(Expr.java:8)
```

Note that `expr()` does not explicitly require end of file at the end of the rule and so the parser will simply ignore stuff it does not recognize after the end of a valid expression:

```bash
$ java Expr
1+2)
OK
```
