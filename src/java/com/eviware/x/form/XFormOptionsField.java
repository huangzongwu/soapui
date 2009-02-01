/*
 *  soapUI, copyright (C) 2004-2009 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */
package com.eviware.x.form;

/**
 * 
 * @author Lars H
 */
public interface XFormOptionsField extends XFormField
{
   public void addItem(String value);
   
   public void setOptions(Object[] values);
   
   public String[] getOptions();
   
   public String[] getSelectedOptions();
   
   public void setSelectedOptions( String [] options );

	public int[] getSelectedIndexes();
}
