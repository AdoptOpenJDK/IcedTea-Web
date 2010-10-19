/* IcedTeaRunnable.h

   Copyright (C) 2009, 2010  Red Hat

This file is part of IcedTea.

IcedTea is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

IcedTea is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with IcedTea; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version. */

#ifndef __ICEDTEARUNNABLE_H__
#define __ICEDTEARUNNABLE_H__

#define MOZILLA 1
#if MOZILLA

#if MOZILLA_VERSION_COLLAPSED < 1090100
#include <nsIRunnable.h>
#include <string>
#endif

/*
 * This struct holds the result from the main-thread dispatched method
 */
typedef struct result_data
{
	// Return identifier (if applicable)
    int return_identifier;

    // Return string (if applicable)
    std::string* return_string;

    // Return wide/mb string (if applicable)
    std::wstring* return_wstring;

    // Error message (if an error occurred)
    std::string* error_msg;

    // Boolean indicating if an error occurred
    bool error_occured;

    // If this result is ready
    bool result_ready;

} ResultData;

class IcedTeaRunnable : public nsIRunnable
{
public:
  NS_DECL_ISUPPORTS
  NS_DECL_NSIRUNNABLE

  IcedTeaRunnable ();

  ~IcedTeaRunnable ();
};

class IcedTeaRunnableMethod : public IcedTeaRunnable
{
public:

  typedef void* (*Method) (void*, void*);

  IcedTeaRunnableMethod (Method, void* thread_data, void* result);
  NS_IMETHOD Run ();

  ~IcedTeaRunnableMethod ();

  Method method;
  void* thread_data;
  void* result;
};

#endif /* MOZILLA */

#endif /* __ICEDTEARUNNABLE_H__ */
