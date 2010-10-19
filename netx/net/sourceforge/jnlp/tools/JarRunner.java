package net.sourceforge.jnlp.tools;

import net.sourceforge.jnlp.tools.JarSigner;
public class JarRunner {


        public static void main(String[] args) throws Exception{


                //JarSigner.main(args);
                JarSigner js = new JarSigner();
                js.verifyJar(args[0]);
        }

}
