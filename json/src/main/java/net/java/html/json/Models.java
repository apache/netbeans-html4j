/**
 * HTML via Java(tm) Language Bindings
 * Copyright (C) 2013 Jaroslav Tulach <jaroslav.tulach@apidesign.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details. apidesign.org
 * designates this particular file as subject to the
 * "Classpath" exception as provided by apidesign.org
 * in the License file that accompanied this code.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. Look for COPYING file in the top folder.
 * If not, see http://wiki.apidesign.org/wiki/GPLwithClassPathException
 */
package net.java.html.json;

import net.java.html.BrwsrCtx;
import java.io.IOException;
import java.io.InputStream;
import org.apidesign.html.json.impl.JSON;

/** Information about and 
 * operations for classes generated by the {@link Model @Model}
 * annotation.
 *
 * @author Jaroslav Tulach <jaroslav.tulach@apidesign.org>
 */
public final class Models {
    private Models() {
    }
   
    /** Finds out whether given class is a model class - e.g. has been
     * generated by {@link Model @Model} annotation.
     * 
     * @param clazz the class to test
     * @return true, if <code>clazz</code> was generated by {@link Model} annotation
     * @since 0.2
     */
    public static boolean isModel(Class<?> clazz) {
        return JSON.isModel(clazz);
    }
    
    /** Binds given model to another context. 
     * 
     * @param <Model> class defined by {@link net.java.html.json.Model} annotation
     * @param model instance of a model defined by {@link net.java.html.json.Model} annotation
     * @param context context to which the model should be bound
     * @return new instance of model bound to new <code>context</code>
     * @throws IllegalArgumentException in case the instance is not generated by model interface
     * @since 0.4
     */
    public static <Model> Model bind(Model model, BrwsrCtx context) {
        return JSON.bindTo(model, context);
    }
    
    /** Generic method to parse content of a model class from a stream.
     * 
     * @param c context of the technology to use for reading 
     * @param model the model class generated by {@link Model} annotation
     * @param is input stream with data
     * @return new instance of the model class
     * @since 0.2
     */
    public static <M> M parse(BrwsrCtx c, Class<M> model, InputStream is) throws IOException {
        return JSON.readStream(c, model, is);
    }
    
    /** Converts an existing, raw, JSON object into a {@link Model model class}.
     * 
     * @param <M> the type of the model class
     * @param ctx context of the technology to use for converting
     * @param model the model class
     * @param jsonObject original instance of the JSON object
     * @return new instance of the model class
     * @since 0.7
     */
    public static <M> M fromRaw(BrwsrCtx ctx, Class<M> model, Object jsonObject) {
        return JSON.read(ctx, model, jsonObject);
    }
    
//    /** Converts an existing {@link Model model} into its associated, raw 
//     * JSON object. The object may, but does not have to, be the same instance
//     * as the model object.
//     * 
//     * @param model the model object
//     * @return the raw JSON object associated with the model
//     * @since 0.7
//     */
//    public static Object toRaw(Object model) {
//        return JSON.toJSON(model);
//    }
}
