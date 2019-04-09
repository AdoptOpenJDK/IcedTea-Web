package net.sourceforge.jnlp.util.logging;

public enum OutputControllerLevel {

     MESSAGE_ALL, // - stdout/log in all cases
     MESSAGE_DEBUG, // - stdout/log in verbose/debug mode
     WARNING_ALL, // - stdout+stderr/log in all cases (default for
     WARNING_DEBUG, // - stdou+stde/logrr in verbose/debug mode
     ERROR_ALL, // - stderr/log in all cases (default for
     ERROR_DEBUG; // - stderr/log in verbose/debug mode
     //ERROR_DEBUG is default for Throwable
     //MESSAGE_DEBUG is default  for String

     public  boolean isOutput() {
         return this == OutputControllerLevel.MESSAGE_ALL
                 || this == OutputControllerLevel.MESSAGE_DEBUG
                 || this == OutputControllerLevel.WARNING_ALL
                 || this == OutputControllerLevel.WARNING_DEBUG;
     }

     public  boolean isError() {
         return this == OutputControllerLevel.ERROR_ALL
                 || this == OutputControllerLevel.ERROR_DEBUG
                 || this == OutputControllerLevel.WARNING_ALL
                 || this == OutputControllerLevel.WARNING_DEBUG;
     }

     public  boolean isWarning() {
         return this == OutputControllerLevel.WARNING_ALL
                 || this == OutputControllerLevel.WARNING_DEBUG;
     }

      public  boolean isDebug() {
         return this == OutputControllerLevel.ERROR_DEBUG
                 || this == OutputControllerLevel.MESSAGE_DEBUG
                 || this == OutputControllerLevel.WARNING_DEBUG;
     }

     public  boolean isInfo() {
         return this == OutputControllerLevel.ERROR_ALL
                 || this == OutputControllerLevel.WARNING_ALL
                 || this == OutputControllerLevel.MESSAGE_ALL;
     }
 }
