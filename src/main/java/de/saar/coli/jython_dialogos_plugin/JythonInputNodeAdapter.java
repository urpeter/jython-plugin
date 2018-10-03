/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.saar.coli.jython_dialogos_plugin;

import com.clt.diamant.Device;
import com.clt.diamant.InputCenter;
import com.clt.diamant.graph.nodes.AbstractInputNode;
import com.clt.script.exp.Pattern;
import com.clt.script.exp.patterns.VarPattern;
import com.clt.speech.recognition.LanguageName;
import com.clt.speech.recognition.MatchResult;
import com.clt.speech.recognition.RecognitionExecutor;
import com.clt.srgf.Grammar;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFormat;
import javax.swing.JLayeredPane;

/**
 *
 * @author koller
 */
public class JythonInputNodeAdapter extends AbstractInputNode {

    private static JythonObjectFactory factory = JythonObjectFactory.getSingleton();
    private AbstractInputNode delegate;

    public JythonInputNodeAdapter(String module, String clazz) {
        delegate = factory.create(module, clazz, AbstractInputNode.class);
    }

    /* If Jython class implements a Recognize method, delegate it to the Jython class;
       otherwise, use the default implementation.
     */
    @Override
    public void recognizeInBackground(Grammar recGrammar, InputCenter input, VarPattern backgroundPattern, float confidenceThreshold) {
        Class delegateClass = delegate.getClass();
        try {
            Method methodInDelegate = delegateClass.getDeclaredMethod("recognizeInBackground", new Class[]{Grammar.class, InputCenter.class, VarPattern.class, float.class});
            methodInDelegate.invoke(delegate, recGrammar, input, backgroundPattern, confidenceThreshold);
        } catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException  ex) {
            // Delegate class does not implement this method => use default implementation
            super.recognizeInBackground(recGrammar, input, backgroundPattern, confidenceThreshold);
        } catch(InvocationTargetException ex) {
            throw (RuntimeException) ex.getCause();
        }
    }

    @Override
    public MatchResult graphicallyRecognize(JLayeredPane layer, Grammar recGrammar, Pattern[] patterns, long timeout, float confidenceThreshold, boolean interactiveTest) throws TimeoutException {
        Class delegateClass = delegate.getClass();
        try {
            Method methodInDelegate = delegateClass.getDeclaredMethod("graphicallyRecognize", new Class[]{JLayeredPane.class, Grammar.class, Pattern[].class, long.class, float.class, boolean.class});
            return (MatchResult) methodInDelegate.invoke(delegate, layer, recGrammar, patterns, timeout, confidenceThreshold, interactiveTest);
        } catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException  ex) {
            // Delegate class does not implement this method => use default implementation
            return super.graphicallyRecognize(layer, recGrammar, patterns, timeout, confidenceThreshold, interactiveTest); //To change body of generated methods, choose Tools | Templates.
        } catch(InvocationTargetException ex) {
            throw (RuntimeException) ex.getCause();
        }
    }

    /* delegate all abstract methods to Jython class */
    @Override
    public AudioFormat getAudioFormat() {
        return delegate.getAudioFormat();
    }

    @Override
    public RecognitionExecutor createRecognitionExecutor(Grammar grmr) {
        return delegate.createRecognitionExecutor(grmr);
    }

    @Override
    public Device getDevice() {
        return delegate.getDevice();
    }

    @Override
    public List<LanguageName> getAvailableLanguages() {
        return delegate.getAvailableLanguages();
    }

    @Override
    public LanguageName getDefaultLanguage() {
        return delegate.getDefaultLanguage();
    }
}
