
from com.clt.diamant.graph import Node
from javax.swing import JLabel


class JythonTestNode(Node):
    def __init__(self, java_node):
        self.java_node = java_node        
        self.java_node.addEdge()  # output nodes have one port for an outgoing edge
    
    def execute(self, wozInterface, inputCenter, executionLogger):
        print("test node executed")
        return self.java_node.getEdge(0).getTarget() # jump to next connected node

    def createEditorComponent(self, map):
        return JLabel("hello from TestNode")

    
