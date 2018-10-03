/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.saar.coli.jython_dialogos_plugin;

import com.clt.diamant.graph.nodes.AbstractOutputNode;
import com.clt.speech.SpeechException;
import com.clt.speech.tts.VoiceName;
import java.util.List;
import java.util.Map;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;

/**
 *
 * @author koller
 */
public class JythonOutputNodeAdapter extends AbstractOutputNode {
    private static JythonFactory factory = new JythonFactory();
    private AbstractOutputNode delegate = factory.create();

    @Override
    public String getResourceString(String string) {
        return delegate.getResourceString(string);
    }

    @Override
    public List<VoiceName> getAvailableVoices() {
        return delegate.getAvailableVoices();
    }

    @Override
    public void speak(String string, Map<String, Object> map) throws SpeechException {
        delegate.speak(string, map);
    }

    @Override
    public void stopSynthesis() {
        delegate.stopSynthesis();
    }
    

    private static class JythonFactory {
        public JythonFactory() {
            PythonInterpreter interpreter = new PythonInterpreter();
            interpreter.exec("from jython.output_node import JythonOutputNode");
            pyNodeClass = interpreter.get("JythonOutputNode");
        }

        public AbstractOutputNode create() {
            PyObject pyNodeObject = pyNodeClass.__call__();
            return (AbstractOutputNode) pyNodeObject.__tojava__(AbstractOutputNode.class);
        }

        private PyObject pyNodeClass;
    }
}
