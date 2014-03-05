package net.sourceforge.jnlp.security.dialogs.apptrustwarningpanel;

import net.sourceforge.jnlp.security.dialogs.apptrustwarningpanel.UnsignedAppletTrustWarningPanel;
import net.sourceforge.jnlp.security.dialogs.apptrustwarningpanel.AppTrustWarningPanel;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;
import net.sourceforge.jnlp.PluginBridge;
import net.sourceforge.jnlp.PluginParameters;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.BeforeClass;
import org.junit.Test;

public class AppTrustWarningPanelTest {

    private static URL mockCodebase;
    private static URL mockDocumentBase;
    private static String mockJar;
    private static String mockMainClass;
    private static int mockWidth;
    private static int mockHeight;
    private static PluginParameters mockParameters;

    private static PluginBridge mockPluginBridge;

    /* Should contain an instance of each AppTrustWarningPanel subclass */
    private static List<AppTrustWarningPanel> panelList = new ArrayList<AppTrustWarningPanel>();

    @BeforeClass
    public static void setup() throws Exception {
        mockCodebase = new URL("http://www.example.com");
        mockDocumentBase = new URL("http://www.example.com");
        mockJar = "ApplicationName.jar";
        mockMainClass = "ApplicationMainClass";
        mockWidth = 100;
        mockHeight = 100;

        Map<String, String> fakeMap = new HashMap<String, String>();
        fakeMap.put("code", mockMainClass);
        mockParameters = new PluginParameters(fakeMap);

        mockPluginBridge = new PluginBridge(mockCodebase, mockDocumentBase, mockJar,
                mockMainClass, mockWidth, mockHeight, mockParameters);

        panelList.add(new UnsignedAppletTrustWarningPanel(mockPluginBridge, null));
    }

    @Test
    public void testJNLPFile() throws Exception {
        for (AppTrustWarningPanel panel : panelList) {
            assertNotNull("JNLPFile for " + panel.getClass() + " should not be null", panel.file);
        }
    }

    @Test
    public void testDimensions() throws Exception {
        for (AppTrustWarningPanel panel : panelList) {
            assertTrue("Pane width for " + panel.getClass() + " should be positive", panel.PANE_WIDTH > 0);
            assertTrue("Top panel height for " + panel.getClass() + " should be positive", panel.TOP_PANEL_HEIGHT > 0);
            assertTrue("Info panel height for " + panel.getClass() + " should be positive", panel.INFO_PANEL_HEIGHT > 0);
            assertTrue("Info panel hint height for " + panel.getClass() + " should be positive", panel.INFO_PANEL_HINT_HEIGHT > 0);
            assertTrue("Question panel height for " + panel.getClass() + " should be positive", panel.QUESTION_PANEL_HEIGHT > 0);
        }
    }

    @Test
    public void testButtons() throws Exception {
        for (AppTrustWarningPanel panel : panelList) {
            assertTrue("Allow Button for " + panel.getClass() + " should be a JButton", panel.getAllowButton() instanceof JButton);
            assertTrue("Reject Button for " + panel.getClass() + " should be a JButton", panel.getRejectButton() instanceof JButton);
        }
    }

    @Test
    public void testInfoImage() throws Exception {
        for (AppTrustWarningPanel panel : panelList) {
            assertNotNull("infoImage should not be null for " + panel.getClass(), panel.getInfoImage());
        }
    }

    @Test
    public void testGetTopLabelTextKey() throws Exception {
        for (AppTrustWarningPanel panel : panelList) {
            assertResultTextValid("top panel", panel.getClass(), panel.getTopPanelText());
        }
    }

    @Test
    public void testGetInfoLabelTextKey() throws Exception {
        for (AppTrustWarningPanel panel : panelList) {
            assertResultTextValid("info panel", panel.getClass(), panel.getInfoPanelText());
        }
    }

    @Test
    public void testGetQuestionPanelKey() throws Exception {
        for (AppTrustWarningPanel panel : panelList) {
            assertResultTextValid("question panel", panel.getClass(), panel.getQuestionPanelText());
        }
    }

    @Test
    public void testHtmlWrap() throws Exception {
        final String testText = "This is some text";
        final String expectedResult = "<html>This is some text</html>";
        final String actualResult = UnsignedAppletTrustWarningPanel.htmlWrap(testText);
        assertEquals("htmlWrap should properly wrap text with HTML tags", expectedResult, actualResult);
    }

    private static void assertResultTextValid(String propertyName, Class<? extends AppTrustWarningPanel> panelType, String result) {
        assertNotNull(propertyName + " text should not be null for " + panelType, result);
        assertFalse(propertyName + " text should not be No Resource for " + panelType, result.contains("RNoResource"));
        assertFalse(propertyName + " label text resource should not be missing for " + panelType, result.contains("Missing Resource:"));
        assertTrue(propertyName + " text should be html-wrapped for " + panelType,
                result.startsWith("<html>") && result.endsWith("</html>"));
        assertFalse(propertyName + " should not have empty fields for " + panelType, result.matches(".*\\{\\d+\\}.*"));
    }

}
