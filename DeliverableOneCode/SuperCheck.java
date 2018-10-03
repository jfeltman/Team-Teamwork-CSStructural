package net.sf.eclipsecs.sample.checks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

public class SuperCheck extends AbstractCheck {

 private int CommentCount = 0;
 private int CommentLinesCount = 0;
 private int begin = 0;
 private int end = 0;
 
 private int refcount = 0;
 private int countLocals = 0;
 private int countExternals = 0;
 List<String> all = new ArrayList<String>();
 List<String> locals = new ArrayList<String>();
 
 private int VariableCount = 0;
 
 private int LoopCount = 0;
 
 private int ExpCount = 0;
 
 @Override
 public boolean isCommentNodesRequired() {
  return true;
 }

 @Override
 public int[] getAcceptableTokens() {
   return new int[] { TokenTypes.SINGLE_LINE_COMMENT, TokenTypes.BLOCK_COMMENT_BEGIN, TokenTypes.BLOCK_COMMENT_END,
                     TokenTypes.METHOD_CALL, TokenTypes.METHOD_REF, TokenTypes.METHOD_DEF,
                     TokenTypes.VARIABLE_DEF,
                     TokenTypes.LITERAL_FOR, TokenTypes.LITERAL_WHILE,
                     TokenTypes.EXPR};
 }

 @Override
 public int[] getRequiredTokens() {
   return new int[0];
 }

 @Override
 public int[] getDefaultTokens() {
   return new int[] { TokenTypes.SINGLE_LINE_COMMENT, TokenTypes.BLOCK_COMMENT_BEGIN, TokenTypes.BLOCK_COMMENT_END,
                     TokenTypes.METHOD_CALL, TokenTypes.METHOD_REF, TokenTypes.METHOD_DEF,
                     TokenTypes.VARIABLE_DEF,
                     TokenTypes.LITERAL_FOR, TokenTypes.LITERAL_WHILE,
                     TokenTypes.EXPR};
 }

 public void setMax(int limit) {
 }

 @Override
 public void visitToken(DetailAST ast) {
   //comment count
   if(ast.getType() == TokenTypes.SINGLE_LINE_COMMENT) {
     this.CommentCount++;
   }
   //line comment count
   if(ast.getType() == TokenTypes.SINGLE_LINE_COMMENT) {
     this.CommentLinesCount++;
   }
   else if(ast.getType() == TokenTypes.BLOCK_COMMENT_BEGIN)
   {
     begin = ast.getLineNo();
   }
   else {
     end = ast.getLineNo();
     this.CommentLinesCount += end-begin+1;
     /*5-4=1. if a comment block begins on 4 and ends on 5, it is spanning two lines. prevent off by one with +1.
     */
   }
   //external and local refs count
   if(ast.getType() == TokenTypes.METHOD_CALL || ast.getType() == TokenTypes.METHOD_REF)
   {
     this.refcount++;//a method is referenced
     //find ident
     if(ast.getFirstChild().getType() == TokenTypes.IDENT)
     {//methodCall()
       this.all.add(ast.getFirstChild().getText());//add the reference to our list of references we made
     }
     else if(ast.getFirstChild().getType() == TokenTypes.DOT)
     {//object.methodCall()
       DetailAST node = ast.getFirstChild().getLastChild();//currnode.getDot.getLastChild
       while(node.getType() != TokenTypes.IDENT)
       {
         node = node.getPreviousSibling();//work back up tree
       }
       String temp = node.getPreviousSibling().getText();
       if(!locals.contains(temp))
       {
         this.all.add(node.getText() + "(Ext. Method)");
         //add (External Method) to the name of all external methods in case of overriden methods having same name.
       }
     }
   }
   if(ast.getType() == TokenTypes.METHOD_DEF)
   {
   //find ident
     this.locals.add(ast.getFirstChild().getNextSibling().getNextSibling().getText());//add the name of the locally defined method to this list
   }
   //variable defs
   if(ast.getType() == TokenTypes.METHOD_DEF)
   {
     this.VariableCount++;
   }
   //loop counts
   if(ast.getType() == TokenTypes.LITERAL_FOR || ast.getType() == TokenTypes.LITERAL_WHILE)
   {
     this.LoopCount++;
   }
   //exp counts
   if(ast.getType() == TokenTypes.EXPR)
   {
     this.ExpCount++;
   }
   
 }
 
 @Override
 public void finishTree(DetailAST ast) {
   log(ast, "commentcount", CommentCount);
   log(ast, "commentlinescount", CommentLinesCount);
   
   System.out.println(Arrays.toString(locals.toArray()));
   System.out.println(Arrays.toString(all.toArray()));
   List<String> commons = new ArrayList<String>(all);
   commons.retainAll(locals);
   countLocals = commons.size();
   countExternals = all.size() - countLocals;
   log(ast, "refcounts", countLocals, countExternals);
   
   log(ast, "variablecount", VariableCount);
   
   log(ast, "loopscount", LoopCount);
   
   log(ast, "numberofexpressions", ExpCount);
   
   refcount = 0;
   countLocals = 0;
   countExternals = 0;
   all.clear();
   locals.clear();
   CommentCount = 0;
   CommentLinesCount = 0;
   VariableCount = 0;
   LoopCount = 0;
   ExpCount = 0;
 }

}