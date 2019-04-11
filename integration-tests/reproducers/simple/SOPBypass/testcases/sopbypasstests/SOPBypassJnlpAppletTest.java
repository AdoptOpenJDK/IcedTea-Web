/* 
   Copyright (C) 2015 Red Hat, Inc.

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

package sopbypasstests;

import net.sourceforge.jnlp.ProcessResult;
import net.sourceforge.jnlp.annotations.NeedsDisplay;
import org.junit.Test;

import static sopbypasstests.SOPBypassUtil.*;


public class SOPBypassJnlpAppletTest extends SOPBypassBeforeAndAfterChunks {

    

    @Test
    @NeedsDisplay
    public void testLocalAbsoluteArchiveLocalPathCodebase_JAT() throws Exception {
        TemplatedJnlpDoc templatedDoc = filterJnlp("SOPBypass.jnlp", server.getUrl("SOPBypass.jar"), server.getUrl("."), getUnrelatedServer());
        ProcessResult pr = performTest(templatedDoc);
        assertCodebaseConnection(pr, serverInstance());
        assertDocumentBaseConnection(pr, serverInstance());
        assertNoUnrelatedConnection(pr, unrelatedInstance());
        //assertResourcesConnection(pr, serverInstance());
        //for some reason, url connection is heving permission denied
        resourcesImpl(false, true, true, pr, serverInstance());
    }

    @Test
    @NeedsDisplay
    public void testLocalAbsoluteArchiveUnrelatedRemoteCodebase_JAT() throws Exception {
        TemplatedJnlpDoc templatedDoc = filterJnlp("SOPBypass.jnlp", server.getUrl("SOPBypass.jar"), serverC.getUrl("."), getUnrelatedServer());
        ProcessResult pr = performTest(templatedDoc);
        assertCodebaseConnection(pr, serverC);
        assertDocumentBaseConnection(pr, serverInstance());
        assertNoUnrelatedConnection(pr, unrelatedInstance());
        //assertResourcesConnection(pr, serverInstance());
        //for some reason, url connection is heving permission denied
        resourcesImpl(false, true, true, pr, serverInstance());
    }

    @Test
    @NeedsDisplay
    public void testRemoteAbsoluteArchiveSameRemoteCodebase_JAT() throws Exception {
        TemplatedJnlpDoc templatedDoc = filterJnlp("SOPBypass.jnlp", serverC.getUrl("SOPBypass.jar"), serverC.getUrl("."), getUnrelatedServer());
        ProcessResult pr = performTest(templatedDoc);
        assertCodebaseConnection(pr, serverC);
        assertNoDocumentBaseConnection(pr, serverInstance());
        assertNoUnrelatedConnection(pr, unrelatedInstance());
        //assertResourcesConnection(pr, serverC);
        //for some reason, url connection is heving permission denied
        resourcesImpl(false, true, true, pr, serverC);
    }

    @Test
    @NeedsDisplay
    public void testRemoteAbsoluteArchiveUnrelatedRemoteCodebase_JAT() throws Exception {
        TemplatedJnlpDoc templatedDoc = filterJnlp("SOPBypass.jnlp", serverB.getUrl("SOPBypass.jar"), serverC.getUrl("."), getUnrelatedServer());
        ProcessResult pr = performTest(templatedDoc);
        assertCodebaseConnection(pr, serverC);
        assertNoDocumentBaseConnection(pr, serverInstance());
        assertNoUnrelatedConnection(pr, unrelatedInstance());
        //assertResourcesConnection(pr, serverB);
        //for some reason, url connection is heving permission denied
        resourcesImpl(false, true, true, pr, serverB);
    }

    @Test
    @NeedsDisplay
    public void testRemoteAbsoluteArchiveLocalPathCodebase_JAT() throws Exception {
        TemplatedJnlpDoc templatedDoc = filterJnlp("SOPBypass.jnlp", serverB.getUrl("SOPBypass.jar"), server.getUrl("."), getUnrelatedServer());
        ProcessResult pr = performTest(templatedDoc);
        assertCodebaseConnection(pr, serverInstance());
        assertDocumentBaseConnection(pr, serverInstance());
        assertNoUnrelatedConnection(pr, unrelatedInstance());
        //assertResourcesConnection(pr, serverB);
        //for some reason, url connection is heving permission denied
        resourcesImpl(false, true, true, pr, serverB);
    }

    @Test
    @NeedsDisplay
    public void testRemoteAbsoluteArchiveLocalDotCodebase_JAT() throws Exception {
        TemplatedJnlpDoc templatedDoc = filterJnlp("SOPBypass.jnlp", serverB.getUrl("SOPBypass.jar"), ".", getUnrelatedServer());
        ProcessResult pr = performTest(templatedDoc);
        assertCodebaseConnection(pr, serverInstance());
        assertDocumentBaseConnection(pr, serverInstance());
        assertNoUnrelatedConnection(pr, unrelatedInstance());
        //assertResourcesConnection(pr, serverB);
        //for some reason, url connection is heving permission denied
        resourcesImpl(false, true, true, pr, serverB);
    }

    @Test
    @NeedsDisplay
    public void testRemoteAbsoluteArchiveNoCodebase_JAT() throws Exception {
        TemplatedJnlpDoc templatedDoc = filterJnlp("SOPBypass.jnlp", serverB.getUrl("SOPBypass.jar"), (String) null, getUnrelatedServer());
        ProcessResult pr = performTest(templatedDoc);
        assertCodebaseConnection(pr, serverInstance());
        assertDocumentBaseConnection(pr, serverInstance());
        assertNoUnrelatedConnection(pr, unrelatedInstance());
        //assertResourcesConnection(pr, serverB);
        //for some reason, url connection is heving permission denied
        resourcesImpl(false, true, true, pr, serverB);
    }

    @Test
    @NeedsDisplay
    public void testLocalAbsoluteArchiveNoCodebase_JAT() throws Exception {
        TemplatedJnlpDoc templatedDoc = filterJnlp("SOPBypass.jnlp", server.getUrl("SOPBypass.jar"), (String) null, getUnrelatedServer());
        ProcessResult pr = performTest(templatedDoc);
        assertCodebaseConnection(pr, serverInstance());
        assertDocumentBaseConnection(pr, serverInstance());
        assertNoUnrelatedConnection(pr, unrelatedInstance());
        //assertResourcesConnection(pr, serverInstance());
        //for some reason, url connection is heving permission denied
        resourcesImpl(false, true, true, pr, serverInstance());
    }

    @Test
    @NeedsDisplay
    public void testLocalRelativeArchiveNoCodebase_JAT() throws Exception {
        TemplatedJnlpDoc templatedDoc = filterJnlp("SOPBypass.jnlp", "SOPBypass.jar", (String) null, getUnrelatedServer());
        ProcessResult pr = performTest(templatedDoc);
        assertCodebaseConnection(pr, serverInstance());
        assertDocumentBaseConnection(pr, serverInstance());
        assertNoUnrelatedConnection(pr, unrelatedInstance());
        assertResourcesConnection(pr, serverInstance());
    }

    @Test
    @NeedsDisplay
    public void testLocalRelativeArchiveUnrelatedRemoteCodebase_JAT() throws Exception {
        TemplatedJnlpDoc templatedDoc = filterJnlp("SOPBypass.jnlp", "SOPBypass.jar", serverC.getUrl(), getUnrelatedServer());
        ProcessResult pr = performTest(templatedDoc);
        assertCodebaseConnection(pr, serverC);
        assertNoDocumentBaseConnection(pr, serverInstance());
        assertNoUnrelatedConnection(pr, unrelatedInstance());
        assertResourcesConnection(pr, serverC);
    }

    @Test
    @NeedsDisplay
    public void testLocalAbsoluteArchiveLocalDotCodebase_JAT() throws Exception {
        TemplatedJnlpDoc templatedDoc = filterJnlp("SOPBypass.jnlp", server.getUrl("SOPBypass.jar"), ".", getUnrelatedServer());
        ProcessResult pr = performTest(templatedDoc);
        assertCodebaseConnection(pr, serverInstance());
        assertDocumentBaseConnection(pr, serverInstance());
        assertNoUnrelatedConnection(pr, unrelatedInstance());
        //assertResourcesConnection(pr, serverInstance());
        //for some reason, url connection is heving permission denied
        resourcesImpl(false, true, true, pr, serverInstance());
    }

    @Test
    @NeedsDisplay
    public void testLocalRelativeArchiveLocalPathCodebase_JAT() throws Exception {
        TemplatedJnlpDoc templatedDoc = filterJnlp("SOPBypass.jnlp", "SOPBypass.jar", server.getUrl("/"), getUnrelatedServer());
        ProcessResult pr = performTest(templatedDoc);
        assertCodebaseConnection(pr, serverInstance());
        assertDocumentBaseConnection(pr, serverInstance());
        assertNoUnrelatedConnection(pr, unrelatedInstance());
        assertResourcesConnection(pr, serverInstance());
    }

    @Test
    @NeedsDisplay
    public void testLocalRelativeArchiveLocalDotCodebase_JAT() throws Exception {
        TemplatedJnlpDoc templatedDoc = filterJnlp("SOPBypass.jnlp", "SOPBypass.jar", ".", getUnrelatedServer());
        ProcessResult pr = performTest(templatedDoc);
        assertCodebaseConnection(pr, serverInstance());
        assertDocumentBaseConnection(pr, serverInstance());
        assertNoUnrelatedConnection(pr, unrelatedInstance());
        assertResourcesConnection(pr, serverInstance());
    }

    @Test
    @NeedsDisplay
    public void testRemoteRelativeArchiveSameRemoteCodebase_JAT() throws Exception {
        TemplatedJnlpDoc templatedDoc = filterJnlp("SOPBypass.jnlp", "SOPBypass.jar", serverC.getUrl("/"), getUnrelatedServer());
        ProcessResult pr = performTest(templatedDoc);
        assertCodebaseConnection(pr, serverC);
        assertNoDocumentBaseConnection(pr, serverInstance());
        assertNoUnrelatedConnection(pr, unrelatedInstance());
        assertResourcesConnection(pr, serverC);
    }

    public ProcessResult performTest(TemplatedJnlpDoc templatedDoc) throws Exception {
        ProcessResult pr = server.executeJavawsHeadless(templatedDoc.getFileName(), getClosingListener(), null);
        assertStart(pr);
        assertEnd(pr);
        assertUnprivileged(pr);
        return pr;
    }


}
