
import javax.imageio.ImageIO;
import javax.jnlp.BasicService;
import javax.jnlp.ServiceManager;
import javax.jnlp.UnavailableServiceException;
import java.applet.Applet;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;


/* SimpleTest1.java
 Copyright (C) 2011 Red Hat, Inc.

 This file is part of IcedTea.

 IcedTea is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License as published by
 the Free Software Foundation, version 2.

 IcedTea is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with IcedTea; see the file COPYING.  If not, write to
 the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 02110-1301 USA.

 Linking this library statically or dynamically with other modules is
 making a combined work based on this library.  Thus, the terms and
 conditions of the GNU General Public License cover the whole
 combination.

 As a special exception, the copyright holders of this library give you
 permission to link this library with independent modules to produce an
 executable, regardless of the license terms of these independent
 modules, and to copy and distribute the resulting executable under
 terms of your choice, provided that you also meet, for each linked
 independent module, the terms and conditions of the license of that
 module.  An independent module is a module which is not derived from
 or based on this library.  If you modify this library, you may extend
 this exception to your version of the library, but you are not
 obligated to do so.  If you do not wish to do so, delete this
 exception statement from your version.
 */
public class IcoAccess  extends Applet{
    
      @Override
    public void init() {
          try {
              main(getCodeBase().toExternalForm());
          } catch (Exception ex){
              ex.printStackTrace();
          }
    }

    public static void main(String... args) throws UnavailableServiceException, MalformedURLException, IOException {
        //args = new String[]{"http://localhost:44321"};
        System.out.println("IcoAccess running");
        final URL codeBase;
        if (args.length == 0) {
            final BasicService bs = (BasicService) ServiceManager.lookup(
                    "javax.jnlp.BasicService");
            codeBase = bs.getCodeBase();
        } else {
            //for "normal" app tsting and appelts
            codeBase = new URL(args[0]);
        }
        System.out.println(codeBase);
        URL ico = new URL(codeBase + "/IcoAccess.ico");
        URL png = new URL(codeBase + "/IcoAccess.png");
        System.out.println(ico);
        byte[] b = new byte[1406];
        for (int i = 0; i < b.length; i++) {
            b[i] = 0;

        }
        try (InputStream is = ico.openStream()) {
            is.read(b);
        }
        check(0, b[0]);
        check(0, b[1]);
        check(1, b[2]);
        check(0, b[3]);
        check(1, b[4]);
        check(0, b[5]);
        check(16, b[6]);
        check(16, b[7]);
        check(0, b[1273]);
        for (int i = 1274; i <= 1341; i++) {
            check(1, b[i]);
        }
        check(0, b[1342]);
        System.out.println("array checks passed");
        BufferedImage i = ImageIO.read(new BufferedInputStream(new ByteArrayInputStream(b)));
        System.out.println("ico: " + i);
        System.out.println("IcoAccess ended");
        BufferedImage i2 = ImageIO.read(png);
        System.out.println("png: " + i2);
        System.out.println("*** APPLET FINISHED ***");

    }

    private static void check(int i, byte b) {
        if (i != b) {
            throw new RuntimeException("The image was not loaded!");
        }
    }
}
