
from com.clt.diamant.graph import Node
from javax.swing import JLabel


class JythonTestNode(Node):
    def execute(self, wozInterface, inputCenter, executionLogger):
        print("test node executed")
        return self

    def createEditorComponent(self, map):
        return JLabel("hello from TestNode")

    
