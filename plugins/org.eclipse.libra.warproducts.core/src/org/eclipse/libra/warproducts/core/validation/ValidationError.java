/******************************************************************************* 
* Copyright (c) 2010, 2011 EclipseSource and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   Holger Staudacher - initial API and implementation
*******************************************************************************/ 
package org.eclipse.libra.warproducts.core.validation;

public class ValidationError {

  public static final int LIBRARY_MISSING = 0;
  public static final int LIBRARY_DOESNT_EXIST = 1;
  public static final int BUNDLE_BANNED = 2;
  public static final int BUNDLE_MISSING = 3;
  
  private int type;
  private String message;
  private Object causingObject;

  ValidationError( final int type, 
                   final String message, 
                   final Object causingObject ) 
  {
    this.type = type;
    this.message = message;
    this.causingObject = causingObject;
  }

  public String getMessage() {
    return message;
  }

  public int getType() {
    return type;
  }

  public Object getCausingObject() {
    return causingObject;
  }
  
  public String toString() {
    return message;
  }
  
}
