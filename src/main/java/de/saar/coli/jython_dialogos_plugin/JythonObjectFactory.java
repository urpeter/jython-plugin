/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.saar.coli.jython_dialogos_plugin;

import java.util.HashMap;
import java.util.Map;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;

/**
 * A class for creating Jython objects and returning them as Java objects.
 * After obtaining an instance of this class through the {@link #getSingleton() }
 * method, you can then create a Jython object using {@link #create(java.lang.String, java.lang.String, java.lang.Class) }.
 * The factory internally maintains a single Jython interpreter, and loads
 * each module only once for each class that is requested.
 * 
 * @author koller
 */
public class JythonObjectFactory {
    private static JythonObjectFactory singleton = new JythonObjectFactory();

    private Map<String, PyObject> pyClassCache = new HashMap<>();
    private PythonInterpreter interpreter = new PythonInterpreter();
    
    private JythonObjectFactory() {
        
    }

    private PyObject getOrCreatePyClass(String module, String clazz) {
        String key = module + "@@@" + clazz;
        PyObject ret = pyClassCache.get(key);

        if (ret == null) {
            interpreter.exec("from " + module + " import " + clazz);
            ret = interpreter.get(clazz);
            pyClassCache.put(key, ret);
        }
        
        return ret;
    }

    /**
     * Creates a Jython object and returns it as a Java object.
     * <p>
     * 
     * The Jython object is constructed by importing the class with name
     * "jythonClassName" from the module "module". It is then converted
     * into a Java object of class "javaClass" and returned as such.<p>
     * 
     * The Jython interpreter looks for the given module at the top level
     * of the classpath. To package a Jython module in a Jar file, the
     * easiest way is to put it into src/main/resources.
     * 
     * @param <E>
     * @param module
     * @param jythonClassName
     * @param javaClass
     * @return 
     */
    public <E> E create(String module, String jythonClassName, Class<E> javaClass) {
        PyObject pyNodeClass = getOrCreatePyClass(module, jythonClassName);
        PyObject pyNodeObject = pyNodeClass.__call__();
        return (E) pyNodeObject.__tojava__(javaClass);
    }
    
    /**
     * Returns the (singleton) instance of this factory class.
     * 
     * @return 
     */
    public static JythonObjectFactory getSingleton() {
        return singleton;
    }
}
