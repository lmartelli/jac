package org.objectweb.jac.core.rtti;

public class NoSuchFieldException extends RuntimeException {
   ClassItem cli;
   String fieldName;
   public NoSuchFieldException(ClassItem cli, String fieldName) {
      super("No such field '"+fieldName+"' in class "+cli.getName());
      this.cli = cli;
      this.fieldName = fieldName;
   }
   public ClassItem getClassItem() {
      return cli;
   }
   public String getFieldName() {
      return fieldName;
   }
}
