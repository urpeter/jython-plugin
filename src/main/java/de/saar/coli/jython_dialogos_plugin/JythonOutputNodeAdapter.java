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

/**
 *
 * @author koller
 */
public class JythonOutputNodeAdapter extends AbstractOutputNode {

    private static JythonOutputNodeFactory factory = new JythonOutputNodeFactory("jython.output_node", "JythonOutputNode");
    private AbstractOutputNode delegate = factory.create();
    
    public static String getNodeTypeName(Class<?> c) {
        return "Jython Output";
    }

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

}
