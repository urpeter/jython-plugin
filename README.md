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

Your class must implement the following methods:

* `def execute(self, wozInterface, inputCenter, executionLogger):` This method is called by DialogOS every time an instance of your node is executed during a run of the dialog. You are expected to return the node in the dialog graph that should be executed next (see below).
* `def createEditorComponent(self, properties):` This method is called whenever a dialog developer opens the properties window of your node, by double-clicking on the node or by right-clicking and selecting "Properties". This properties window always has a tab called "General". Your `createEditorComponent` is expected to return an object of class [JTabbedPane](https://docs.oracle.com/javase/tutorial/uiswing/components/tabbedpane.html), which represents an additional tab that will be displayed to the right of the "General" tab.
* a constructor `def __init__(self, java_node):` which accepts a `java_node` argument.

### Constructor

The `java_node` object that is passed to the constructor represents a "mirror image" of your plugin node inside of the Java part of DialogOS. Whenever you want to access the edges into or out of your node or the properties of your node, you should do this by calling the respective methods of the `java_node`. As a result, your constructor should probably store `java_node` in a field of your class (let's say `self.java_node`).

Your constructor will typically add ports for outgoing edges to your node. It does this by calling `java_node.addEdge()` as many times as you want outgoing ports. You can also modify the outgoing ports later if your node requires it.

Finally, your constructor can create properties that can be edited in your node's properties window. It does this by calling `java_node.setProperty(PROPERTY_NAME, INITIAL_VALUE)`; replace "PROPERTY_NAME" by the name you would like to give your property and "INITIAL_VALUE" by its initial value. Properties can be of any data type you like (e.g. strings, ints, booleans).

### Execute

Your `execute` method is called whenever DialogOS executes your node. You can access the current values of the node properties by calling `self.java_node.getProperty(PROPERTY_NAME)`.

When you are done with whatever your node needs to do, you need to return the node into which DialogOS should transition next. You can obtain the node to which your node's k-th output port is connected by calling `self.java_node.getEdge(k).getTarget()`.


### Creating editor components

Your `createEditorComponent` is called whenever the user opens your node's properties window. The method is expected to return an object of class JTabbedPane. Inside of the tabbed pane, you can put any GUI components you like, using Java's standard GUI library, [Swing](https://docs.oracle.com/javase/tutorial/uiswing/). Notice that your code will usually not open its own windows (i.e. JFrames or JDialogs). It will simply return a JTabbedPane that probably contains a [JPanel](https://docs.oracle.com/javase/tutorial/uiswing/components/panel.html) as its top-level content. You will then place JLabels and components for editing properties into the JPanel.

DialogOS simplifies this last step by providing methods which will automatically create Swing components for editing properties. For instance:

* `NodePropertiesDialog.createTextArea(properties, PROPERTY_NAME)` will create a [JTextArea](https://docs.oracle.com/javase/tutorial/uiswing/components/textarea.html) and connect its value to your property, such that when the user closes the properties window by clicking "Ok", the value of the property will be updated.
* `NodePropertiesDialog.createCheckBox(properties, PROPERTY_NAME, LABEL)` will create a [JCheckBox](https://docs.oracle.com/javase/tutorial/uiswing/components/button.html) and connect its value to your (boolean-valued) property. The checkbox will be labeled with the LABEL string you specify.

Placing the components onto the panel requires some knowledge of Swing [layout managers](https://docs.oracle.com/javase/tutorial/uiswing/layout/visual.html). It is probably most convenient to use a [gridbag layout](https://docs.oracle.com/javase/tutorial/uiswing/layout/visual.html#gridbag). Look at the [demo plugin](https://github.com/dialogos-project/jython-demo-plugin) to get a sense of how to do this.



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

Use the file `src/main/resources/META-INF/services/com.clt.dialogos.plugin.Plugin` (with this exact name) to declare your plugin to DialogOS. This file is expected to contain a single line with the fully qualified name of your Java plugin class. If you called your plugin class `JythonDialogosPlugin` and it lives in the package `de.dialogos.plugin`, then the line will read

```
de.dialogos.plugin.JythonDialogosPlugin
```


## Adding icons to your nodes

You can add icons to your node types by putting PNG files into your resources directory. If your node type is `JythonTestNode` in package `de.dialogos.plugin`, this file must be called `JythonTestNode.png` and be in the directory `src/main/resources/de/dialogos/plugin`. Icons must be PNG files with 16x16 pixels. Icons are optional; if there is no file with the correct name and format, DialogOS will simply not display an icon for the node type.


## Running your plugin

To run a version of DialogOS with your plugin in it, go to the main directory (where `build.gradle` lives) on the command line and execute

```
gradlew run
```

This will compile your plugin, make it visible to DialogOS, and run DialogOS. You can then create dialogs that use your plugin. Notice that the non-distribution version of DialogOS (which you are using at this point) does not contain any other plugins (including NXT and SQLite), and can only use the English speech recognizer and synthesis.





