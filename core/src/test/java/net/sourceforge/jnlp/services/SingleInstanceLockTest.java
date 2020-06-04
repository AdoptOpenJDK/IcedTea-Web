package net.sourceforge.jnlp.services;

import java.net.URL;

import org.junit.Test;

import net.adoptopenjdk.icedteaweb.io.FileUtils;
import net.sourceforge.jnlp.JNLPFile;
import net.sourceforge.jnlp.ParserSettings;
import net.sourceforge.jnlp.util.logging.NoStdOutErrTest;

public final class SingleInstanceLockTest
	extends NoStdOutErrTest
{
    @Test
    public void testCreateWithPort() 
    	throws Exception 
    {
    	final URL url = this.getClass().getClassLoader().getResource("net/sourceforge/jnlp/basic.jnlp");
        final JNLPFile jnlpFile = new JNLPFile(url.openStream(), url, new ParserSettings(false,false,false));
        
        assert jnlpFile!=null;
        
        final SingleInstanceLock sil = new SingleInstanceLock(jnlpFile);
        
        sil.lockFile.delete();
        assert sil.lockFile.exists()==false;
        
        sil.createWithPort(123);
        assert sil.lockFile.exists()==true;
        assert "123".equals(FileUtils.loadFileAsUtf8String(sil.lockFile).trim());
        

        sil.createWithPort(456);
        assert sil.lockFile.exists()==true;
        assert "456".equals(FileUtils.loadFileAsUtf8String(sil.lockFile).trim());
    }
}
