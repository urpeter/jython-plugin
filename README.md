# DialogOS plugins using Jython

This library makes it easy to implement a [DialogOS plugin](https://github.com/dialogos-project/dialogos/wiki/Plugins) using [Jython](http://www.jython.org/). This makes it possible for developers who are more familiar with Python than with Java to implement DialogOS plugins.

To implement your own DialogOS plugin using Jython, proceed as follows. An example Jython plugin can be found [here](https://github.com/dialogos-project/jython-demo-plugin).

## Set up your build.gradle

This documentation assumes that you use [Gradle](https://gradle.org/) to compile and run your plugin. Gradle is a build system that keeps the configuration of your project in a file called `build.gradle`. A minimal version of this file for a Jython plugin looks as follows:

```
plugins {
  id 'java'
  id 'maven'
  id 'application'
}
repositories {
  mavenLocal()
  jcenter()
  maven { url "https://jitpack.io" }
}

dependencies {
  implementation 'com.github.dialogos-project:jython_plugin:1.0'
}

mainClassName = 'com.clt.dialogos.DialogOS'
version = '1.0'
```

Observe in particular the following details:

 * The buildfile declares a dependency to `jython_plugin`. This is the compiled version of the repository you are looking at. It depends itself on Jython and DialogOS. The first time you compile your plugin, Gradle will therefore automatically download Jython, DialogOS, and the jython_plugin code.
 * The buildfile declares that the `mainClassName` is `com.clt.dialogos.DialogOS`. This means that when you execute `gradlew run`, Gradle will start DialogOS (and load your plugin, providing the new nodes provided by your plugin).

 
## Directory structure
 
The Jython plugin infrastructure assumes that you have the following directory structure below the directory that contains the `build.gradle`:

```
src/
+--main/
+----java/
+----resources/
+------jython/
+------META-INF/
+--------services/
```

These directories are meant for the following contents:

* src/main/resources/jython will contain your Jython code.
* src/main/java will contain a couple of Java files that make your Jython code accessible to DialogOS.
* src/main/resources/META-INF/services will contain a file called `com.clt.dialogos.plugin.Plugin` which declares your plugin to DialogOS.
* src/main/resources may also contain further files, e.g. icons for your nodes.


## Writing your Jython plugin

A plugin defines a collection of nodes. Throughout this documentation, we will assume that you are implementing a node called `JythonTestNode` to simplify the language.

To implement your plugin, create a Python file in `src/main/resources/jython` - let's say it is called `test_node.py`. This file declares a class `JythonTestNode` which is derived from `com.clt.diamant.graph.Node`. Thus the start of your file will typically look as follows:

```
from com.clt.diamant.graph import Node
from javax.swing import JLabel, BorderFactory, JTabbedPane, JPanel
from java.awt import GridBagLayout, GridBagConstraints, Insets
from com.clt.diamant.gui import NodePropertiesDialog

class JythonTestNode(Node):
  ...
```

The first few lines import classes from DialogOS and the Java standard library that you will need below. The final line declares your node class.

Your class must implement the `execute` and `createEditorComponent` methods as explained in the [Wiki article on plugins](https://github.com/dialogos-project/dialogos/wiki/Plugins). Some Jython-specific notes:

* The `__init__` method of the Jython class is passed an argument `java_node`. This is a "mirror image" of your Jython plugin node inside of the Java part of DialogOS. Whenever you want to call methods that are not purely internal to your node implementation (such as setProperty, getProperty, addEdge, and so on), you should call these methods on the `java_node` object and not on `self`. For instance, you should access the value of a property as `self.java_node.getProperty("PROPERTY")`.
* Placing the components onto the panel requires some knowledge of Swing [layout managers](https://docs.oracle.com/javase/tutorial/uiswing/layout/visual.html). It is probably most convenient to use a [gridbag layout](https://docs.oracle.com/javase/tutorial/uiswing/layout/visual.html#gridbag). Look at the [demo plugin](https://github.com/dialogos-project/jython-demo-plugin) to get a sense of how to do this.


## Creating a Java wrapper for your plugin

Because of architectural details of Jython, DialogOS cannot access your Jython plugin class directly. Instead, you must write a small amount of Java code to make your plugin accessible to DialogOS.

Java programs are structured into classes, which are grouped together into [packages](https://www.tutorialspoint.com/java/java_packages.htm). You need to choose a name for the package in which your plugin and its classes will live. It can be anything you like, as long as it is syntactically correct for a Java package name. For simplicity, we will assume that you choose the package name `de.dialogos.plugin`.

### Java class for the plugin

You need to implement a Java class for your plugin as a whole and put it into your package (`de.dialogos.plugin`). This plugin must be derived from `com.clt.dialogos.plugin.Plugin` and implement a number of methods. Have a look at the [plugin class of the demo plugin](https://github.com/dialogos-project/jython-demo-plugin/blob/master/src/main/java/de/saar/coli/jython_dialogos_plugin/JythonDialogosPlugin.java), and observe in particular the following details:

* The package in the `package` declaration at the top of the file defines the package to which your plugin class belongs.
* The directory in which your `...Plugin.java` file lives must match the package name. In our example with the package `de.dialogos.plugin`, the file must be in the directory `src/main/java/de/dialogos/plugin` (otherwise Java won't find it).
* In the line `public class ... implements Plugin`, the `...` must be the name of your plugin class. You can choose it as you wish, as long it is valid Java syntax for class names. The name of the Java file must match the class name; so e.g. if you call your plugin class `JythonDialogosPlugin`, it must be in the file `JythonDialogosPlugin.java`.
* The method `getId` is expected to return a unique name for your plugin.
* The method `getName` returns a name for your plugin that will be visible to the user. There will be an entry under the Dialog menu in DialogOS for your plugin, which will be shown with this name.
* The method `getIcon` can return a Java [Icon](https://docs.oracle.com/javase/tutorial/uiswing/components/icon.html) object, which will be displayed next to your plugin name in the Dialog Setup window (e.g. when you select Dialog -> your plugin).
* The method `initialize` is called when DialogOS first loads your plugin (on the startup screen). The most important point of this method is to declare all the nodes of your plugin to DialogOS using the `Node.registerNodeTypes` method. Again, see the demo plugin for an example and modify it as needed for your own plugin. Notice that the demo plugin also constructs one object for each ndoe type. This shifts some of the initialization cost of your nodes (e.g. to compile the Jython scripts) to an early time, so creating a node while someone edits the dialog is fast. Notice also that the classes that are being registered here are the Java wrapper classes (see below), not the Jython classes directly.
* The method `createDefaultSettings` returns a Swing component for setting global options for your plugin (e.g. the default language of a speech recognizer).


### Java wrapper for the nodes

You need to create a Java class for each of your Jython node classes which wraps the Jython class in a thin layer of Java. See the *Node.java classes in the demo plugin for examples. There are two things that need to happen here:

* In the constructor (= the method with the same name as your class and no return type), you need to call `super` with the name of your Jython script (`jython.test_node` corresponds to `test_node.py` in the directory `src/main/resourcs/jython`) and the name of your Jython node class.
* In the method `getNodeTypeName`, you return the name of your node type. This will be shown in the node toolbox in DialogOS and on each node that you create for your node class.


## Declaring the plugin to DialogOS

You must declare your plugin to DialogOS (through the `com.clt.dialogos.plugin.Plugin` file) just like you would a [Java plugin](https://github.com/dialogos-project/dialogos/wiki/Plugins). Your Plugin class is written in Java anyway, so use its fully qualified class name in this file.


## Running and deploying the plugin

Proceed as described on the [Wiki page for Java plugins](https://github.com/dialogos-project/dialogos/wiki/Plugins). In particular, you can run `gradlew run` to run a DialogOS instance with your plugin (and only your plugin) loaded for quick testing.


