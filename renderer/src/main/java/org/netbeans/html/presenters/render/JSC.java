/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.html.presenters.render;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.PointerByReference;
import java.util.Arrays;
import java.util.List;

/** Interface to JavaScriptCore native library.
 */
public interface JSC extends Library {
    /** Data to create new JSC class definition.
     */
    public static final class JSClassDefinition extends Structure {

        public int version; /* current (and only) version is 0 */
        public int attributes;
        public String className = "javaClazz";
        public Pointer parentClass;
        public Pointer staticValues;
        public Pointer staticFunctions;
        public Pointer initialize;
        public Callback finalize;
        public Pointer hasProperty;
        public Pointer getProperty;
        public Pointer setProperty;
        public Pointer deleteProperty;
        public Pointer getPropertyNames;
        public Pointer callAsFunction;
        public Pointer callAsConstructor;
        public Pointer hasInstance;
        public Pointer convertToType;

        public JSClassDefinition(Callback finalize) {
            this.finalize = finalize;
        }

        @Override
        protected List getFieldOrder() {
            return Arrays.asList(
                "version", "attributes", "className", "parentClass", 
                "staticValues", "staticFunctions", "initialize", 
                "finalize", "hasProperty", "getProperty", "setProperty", 
                "deleteProperty", "getPropertyNames", "callAsFunction", 
                "callAsConstructor", "hasInstance", "convertToType"
            );
        }
    }

    Pointer JSClassCreate(JSClassDefinition def);

    /*!
    @function
    @abstract Tests whether a JavaScript value is an object with a given class in its class chain.
    @param ctx The execution context to use.
    @param value The JSValue to test.
    @param jsClass The JSClass to test against.
    @result true if value is an object and has jsClass in its class chain, otherwise false.
     */
    int JSValueIsObjectOfClass(Pointer ctx, Pointer value, Pointer jsClass);

    Pointer JSContextGetGlobalObject(Pointer ctx);

    Pointer JSStringCreateWithUTF8CString(String s);

    Pointer JSStringRetain(Pointer string);

    Pointer JSStringRelease(Pointer string);

    int JSStringGetMaximumUTF8CStringSize(Pointer string);

    int JSStringGetUTF8CString(Pointer string, Memory mem, int bufferSize);

    boolean JSStringIsEqual(Pointer a, Pointer b);

    boolean JSStringIsEqualToUTF8CString(Pointer a, String b);

    /*!
    @function JSEvaluateScript
    @abstract Evaluates a string of JavaScript.
    @param ctx The execution context to use.
    @param script A JSString containing the script to evaluate.
    @param thisObject The object to use as "this," or NULL to use the global object as "this."
    @param sourceURL A JSString containing a URL for the script's source file. This is only used when reporting exceptions. Pass NULL if you do not care to include source file information in exceptions.
    @param startingLineNumber An integer value specifying the script's starting line number in the file located at sourceURL. This is only used when reporting exceptions. The value is one-based, so the first line is line 1 and invalid values are clamped to 1.
    @param exception A pointer to a JSValueRef in which to store an exception, if any. Pass NULL if you do not care to store an exception.
    @result The JSValue that results from evaluating script, or NULL if an exception is thrown.
     */
    Pointer JSEvaluateScript(Pointer ctx, Pointer script, Pointer thisObject, Pointer sourceURL, int startingLineNumber, PointerByReference exception);

    /*!
    @function
    @abstract Sets a property on an object.
    @param ctx The execution context to use.
    @param object The JSObject whose property you want to set.
    @param propertyName A JSString containing the property's name.
    @param value A JSValue to use as the property's value.
    @param exception A pointer to a JSValueRef in which to store an exception, if any. Pass NULL if you do not care to store an exception.
    @param attributes A logically ORed set of JSPropertyAttributes to give to the property.
     */
    void JSObjectSetProperty(Pointer ctx, Pointer object, Pointer propertyName, Pointer value, int attributes, PointerByReference exception);

    /*!
    @function
    @abstract Gets a property from an object.
    @param ctx The execution context to use.
    @param object The JSObject whose property you want to get.
    @param propertyName A JSString containing the property's name.
    @param exception A pointer to a JSValueRef in which to store an exception, if any. Pass NULL if you do not care to store an exception.
    @result The property's value if object has the property, otherwise the undefined value.
     */
    Pointer JSObjectGetProperty(Pointer ctx, Pointer object, Pointer propertyName, PointerByReference exception);

    /*!
    @function
    @abstract Gets a property from an object by numeric index.
    @param ctx The execution context to use.
    @param object The JSObject whose property you want to get.
    @param propertyIndex An integer value that is the property's name.
    @param exception A pointer to a JSValueRef in which to store an exception, if any. Pass NULL if you do not care to store an exception.
    @result The property's value if object has the property, otherwise the undefined value.
    @discussion Calling JSObjectGetPropertyAtIndex is equivalent to calling JSObjectGetProperty with a string containing propertyIndex, but JSObjectGetPropertyAtIndex provides optimized access to numeric properties.
     */
    Pointer JSObjectGetPropertyAtIndex(Pointer ctx, Pointer object, int propertyIndex, PointerByReference exception);

    /*!
    @function
    @abstract Convenience method for creating a JavaScript function with a given callback as its implementation.
    @param ctx The execution context to use.
    @param name A JSString containing the function's name. This will be used when converting the function to string. Pass NULL to create an anonymous function.
    @param callAsFunction The JSObjectCallAsFunctionCallback to invoke when the function is called.
    @result A JSObject that is a function. The object's prototype will be the default function prototype.
     */
    Pointer JSObjectMakeFunctionWithCallback(Pointer ctx, Pointer name, Callback callAsFunction);

    /*!
    @function
    @abstract Creates a function with a given script as its body.
    @param ctx The execution context to use.
    @param name A JSString containing the function's name. This will be used when converting the function to string. Pass NULL to create an anonymous function.
    @param parameterCount An integer count of the number of parameter names in parameterNames.
    @param parameterNames A JSString array containing the names of the function's parameters. Pass NULL if parameterCount is 0.
    @param body A JSString containing the script to use as the function's body.
    @param sourceURL A JSString containing a URL for the script's source file. This is only used when reporting exceptions. Pass NULL if you do not care to include source file information in exceptions.
    @param startingLineNumber An integer value specifying the script's starting line number in the file located at sourceURL. This is only used when reporting exceptions. The value is one-based, so the first line is line 1 and invalid values are clamped to 1.
    @param exception A pointer to a JSValueRef in which to store a syntax error exception, if any. Pass NULL if you do not care to store a syntax error exception.
    @result A JSObject that is a function, or NULL if either body or parameterNames contains a syntax error. The object's prototype will be the default function prototype.
    @discussion Use this method when you want to execute a script repeatedly, to avoid the cost of re-parsing the script before each execution.
     */
    Pointer JSObjectMakeFunction(Pointer ctx, Pointer name, int parameterCount, Pointer[] parameterNames, Pointer body, Pointer sourceURL, int startingLineNumber, PointerByReference exception);

    /*!
    @function
    @abstract Calls an object as a function.
    @param ctx The execution context to use.
    @param object The JSObject to call as a function.
    @param thisObject The object to use as "this," or NULL to use the global object as "this."
    @param argumentCount An integer count of the number of arguments in arguments.
    @param arguments A JSValue array of arguments to pass to the function. Pass NULL if argumentCount is 0.
    @param exception A pointer to a JSValueRef in which to store an exception, if any. Pass NULL if you do not care to store an exception.
    @result The JSValue that results from calling object as a function, or NULL if an exception is thrown or object is not a function.
     */
    Pointer JSObjectCallAsFunction(Pointer ctx, Pointer object, Pointer thisObject, int argumentCount, Pointer[] arguments, PointerByReference exception);

    /*!
    @function
    @abstract       Creates a JavaScript value of the null type.
    @param ctx  The execution context to use.
    @result         The unique null value.
     */
    Pointer JSValueMakeNull(Pointer ctx);

    /*!
    @function
    @abstract       Creates a JavaScript value of the boolean type.
    @param ctx  The execution context to use.
    @param boolean  The bool to assign to the newly created JSValue.
    @result         A JSValue of the boolean type, representing the value of boolean.
     */
    Pointer JSValueMakeBoolean(Pointer ctx, boolean val);

    /*!
    @function
    @abstract       Creates a JavaScript value of the number type.
    @param ctx  The execution context to use.
    @param number   The double to assign to the newly created JSValue.
    @result         A JSValue of the number type, representing the value of number.
     */
    Pointer JSValueMakeNumber(Pointer ctx, double number);

    /*!
    @function
    @abstract       Creates a JavaScript value of the string type.
    @param ctx  The execution context to use.
    @param string   The JSString to assign to the newly created JSValue. The
    newly created JSValue retains string, and releases it upon garbage collection.
    @result         A JSValue of the string type, representing the value of string.
     */
    Pointer JSValueMakeString(Pointer ctx, /*JSStringRef*/ Pointer string);

    /*!
    @function
    @abstract Creates a JavaScript Array object.
    @param ctx The execution context to use.
    @param argumentCount An integer count of the number of arguments in arguments.
    @param arguments A JSValue array of data to populate the Array with. Pass NULL if argumentCount is 0.
    @param exception A pointer to a JSValueRef in which to store an exception, if any. Pass NULL if you do not care to store an exception.
    @result A JSObject that is an Array.
    @discussion The behavior of this function does not exactly match the behavior of the built-in Array constructor. Specifically, if one argument
    is supplied, this function returns an array with one element.
     */
    Pointer JSObjectMakeArray(Pointer ctx, int argumentCount, Pointer[] data, PointerByReference exception);

    /*!
    @function
    @abstract Creates a JavaScript object.
    @param ctx The execution context to use.
    @param jsClass The JSClass to assign to the object. Pass NULL to use the default object class.
    @param data A void* to set as the object's private data. Pass NULL to specify no private data.
    @result A JSObject with the given class and private data.
    @discussion The default object class does not allocate storage for private data, so you must provide a non-NULL jsClass to JSObjectMake if you want your object to be able to store private data.
    data is set on the created object before the intialize methods in its class chain are called. This enables the initialize methods to retrieve and manipulate data through JSObjectGetPrivate.
     */
    Pointer JSObjectMake(Pointer ctx, Pointer jsClass, Object data);

    Object JSObjectGetPrivate(Pointer object);

    int JSValueGetType(Pointer ctx, Pointer value);

    /*!
    @function
    @abstract       Converts a JavaScript value to number and returns the resulting number.
    @param ctx  The execution context to use.
    @param value    The JSValue to convert.
    @param exception A pointer to a JSValueRef in which to store an exception, if any. Pass NULL if you do not care to store an exception.
    @result         The numeric result of conversion, or NaN if an exception is thrown.
     */
    double JSValueToNumber(Pointer ctx, Pointer value, PointerByReference exception);

    /*!
    @function
    @abstract       Converts a JavaScript value to string and copies the result into a JavaScript string.
    @param ctx  The execution context to use.
    @param value    The JSValue to convert.
    @param exception A pointer to a JSValueRef in which to store an exception, if any. Pass NULL if you do not care to store an exception.
    @result         A JSString with the result of conversion, or NULL if an exception is thrown. Ownership follows the Create Rule.
     */
    Pointer JSValueToStringCopy(Pointer ctx, Pointer value, PointerByReference exception);

    /*!
    @function
    @abstract Converts a JavaScript value to object and returns the resulting object.
    @param ctx  The execution context to use.
    @param value    The JSValue to convert.
    @param exception A pointer to a JSValueRef in which to store an exception, if any. Pass NULL if you do not care to store an exception.
    @result         The JSObject result of conversion, or NULL if an exception is thrown.
     */
    Pointer JSValueToObject(Pointer ctx, Pointer value, PointerByReference exception);

    void JSValueProtect(Pointer ctx, Pointer value);

    void JSValueUnprotect(Pointer ctx, Pointer value);
    
}
