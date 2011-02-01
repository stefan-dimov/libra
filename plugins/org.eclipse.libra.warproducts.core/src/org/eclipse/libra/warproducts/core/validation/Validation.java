/******************************************************************************* 
* Copyright (c) 2010 EclipseSource and others. All rights reserved. This
* program and the accompanying materials are made available under the terms of
* the Eclipse Public License v1.0 which accompanies this distribution, and is
* available at http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
*   EclipseSource - initial API and implementation
*******************************************************************************/ 
package org.eclipse.libra.warproducts.core.validation;

import java.util.ArrayList;
import java.util.List;


public class Validation {
  
  private List errors;

  Validation() {
    this.errors = new ArrayList();
  }

  public boolean isValid() {
    return errors.isEmpty();
  }

  public ValidationError[] getErrors() {
    ValidationError[] result = new ValidationError[ errors.size() ];
    errors.toArray( result );
    return result;
  }

  void addError( final ValidationError error ) {
    errors.add( error );
  }
  
}
