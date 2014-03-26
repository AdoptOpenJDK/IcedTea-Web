/*Copyright (C) 2014 Red Hat, Inc.

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

package net.sourceforge.jnlp.security.policyeditor;

import java.util.Map;
import javax.swing.JCheckBox;
import static net.sourceforge.jnlp.runtime.Translator.R;


/**
 * Defines the set of default permissions for PolicyEditor, ie the ones which are assigned
 * dedicated checkboxes
 */
public enum PolicyEditorPermissions {

    READ_LOCAL_FILES(R("PEReadFiles"), R("PEReadFilesDetail"),
            PermissionType.FILE_PERMISSION, PermissionTarget.USER_HOME, PermissionActions.READ),

    WRITE_LOCAL_FILES(R("PEWriteFiles"), R("PEWriteFilesDetail"),
            PermissionType.FILE_PERMISSION, PermissionTarget.USER_HOME, PermissionActions.WRITE),

    READ_PROPERTIES(R("PEReadProps"), R("PEReadPropsDetail"),
            PermissionType.PROPERTY_PERMISSION, PermissionTarget.ALL, PermissionActions.READ),

    WRITE_PROPERTIES(R("PEWriteProps"), R("PEWritePropsDetail"),
            PermissionType.PROPERTY_PERMISSION, PermissionTarget.ALL, PermissionActions.WRITE),

    READ_SYSTEM_FILES(R("PEReadSystemFiles"), R("PEReadSystemFilesDetail"),
            PermissionType.FILE_PERMISSION, PermissionTarget.ALL_FILES, PermissionActions.READ),

    WRITE_SYSTEM_FILES(R("PEWriteSystemFiles"), R("PEWriteSystemFilesDetail"),
            PermissionType.FILE_PERMISSION, PermissionTarget.ALL_FILES, PermissionActions.WRITE),

    READ_TMP_FILES(R("PEReadTempFiles"), R("PEReadTempFilesDetail"),
            PermissionType.FILE_PERMISSION, PermissionTarget.TMPDIR, PermissionActions.READ),

    WRITE_TMP_FILES(R("PEWriteTempFiles"), R("PEWriteTempFilesDetail"),
            PermissionType.FILE_PERMISSION, PermissionTarget.TMPDIR, PermissionActions.WRITE),

    JAVA_REFLECTION(R("PEReflection"), R("PEReflectionDetail"),
            PermissionType.REFLECT_PERMISSION, PermissionTarget.REFLECT, PermissionActions.NONE),

    GET_CLASSLOADER(R("PEClassLoader"), R("PEClassLoaderDetail"),
            PermissionType.RUNTIME_PERMISSION, PermissionTarget.CLASSLOADER, PermissionActions.NONE),

    ACCESS_CLASS_IN_PACKAGE(R("PEClassInPackage"), R("PEClassInPackageDetail"),
            PermissionType.RUNTIME_PERMISSION, PermissionTarget.ACCESS_CLASS_IN_PACKAGE, PermissionActions.NONE),

    ACCESS_DECLARED_MEMBERS(R("PEDeclaredMembers"), R("PEDeclaredMembersDetail"),
            PermissionType.RUNTIME_PERMISSION, PermissionTarget.DECLARED_MEMBERS, PermissionActions.NONE),

    NETWORK(R("PENetwork"), R("PENetworkDetail"),
            PermissionType.SOCKET_PERMISSION, PermissionTarget.ALL, PermissionActions.NETALL),

    EXEC_COMMANDS(R("PEExec"), R("PEExecDetail"),
            PermissionType.FILE_PERMISSION, PermissionTarget.ALL_FILES, PermissionActions.EXECUTE),

    GET_ENV(R("PEGetEnv"), R("PEGetEnvDetail"),
            PermissionType.RUNTIME_PERMISSION, PermissionTarget.GETENV, PermissionActions.NONE),

    ALL_AWT(R("PEAWTPermission"), R("PEAWTPermissionDetail"),
            PermissionType.AWT_PERMISSION, PermissionTarget.ALL, PermissionActions.NONE),

    CLIPBOARD(R("PEClipboard"), R("PEClipboardDetail"),
            PermissionType.AWT_PERMISSION, PermissionTarget.CLIPBOARD, PermissionActions.NONE),

    PLAY_AUDIO(R("PEPlayAudio"), R("PEPlayAudioDetail"),
            PermissionType.AUDIO_PERMISSION, PermissionTarget.PLAY, PermissionActions.NONE),

    RECORD_AUDIO(R("PERecordAudio"), R("PERecordAudioDetail"),
            PermissionType.AUDIO_PERMISSION, PermissionTarget.RECORD, PermissionActions.NONE),

    PRINT(R("PEPrint"), R("PEPrintDetail"),
            PermissionType.RUNTIME_PERMISSION, PermissionTarget.PRINT, PermissionActions.NONE);

    
    public static enum Group {

       
        ReadFileSystem(R("PEGReadFileSystem"),  READ_LOCAL_FILES, READ_PROPERTIES, READ_SYSTEM_FILES, READ_TMP_FILES, GET_ENV),
        WriteFileSystem(R("PEGWriteFileSystem"), WRITE_LOCAL_FILES, WRITE_PROPERTIES, WRITE_SYSTEM_FILES, WRITE_TMP_FILES, EXEC_COMMANDS),
        AccesUnowenedCode(R("PEGAccesUnowenedCode"), JAVA_REFLECTION, GET_CLASSLOADER, ACCESS_CLASS_IN_PACKAGE, ACCESS_DECLARED_MEMBERS),
        MediaAccess(R("PEGMediaAccess"), PLAY_AUDIO, RECORD_AUDIO, PRINT, CLIPBOARD);
        

        private final PolicyEditorPermissions[] permissions;
        private final String title; 
        private Group(String title, PolicyEditorPermissions... permissions) {
            this.title = title;
            this.permissions = permissions;
        
        }
        
        public static boolean anyContains(PolicyEditorPermissions permission) {
            for (Group g : Group.values()) {
                if (g.contains(permission)) {
                    return true;
                }
            }
            return false;
        }
        
        public static boolean anyContains(JCheckBox view, Map<PolicyEditorPermissions, JCheckBox> checkboxMap) {
            for (Map.Entry<PolicyEditorPermissions, JCheckBox> pairs : checkboxMap.entrySet()){
                if (pairs.getValue() == view) {
                    for (Group g : Group.values()) {
                        if (g.contains(pairs.getKey())) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }
        
        /*
         * + all is selected
         * 0 invalid
         * - none is selected
         */
        public int getState (final Map<PolicyEditorPermissions, Boolean> map) {
            boolean allTrue=true;
            boolean allFalse=true;
            for (PolicyEditorPermissions pp: getPermissions()){
                Boolean b = map.get(pp);
                if (b == null){
                    return 0;
                }
                if (b.booleanValue()){
                    allFalse = false;
                } else {
                    allTrue = false;
                }
            }
            if (allFalse){
                return -1;
            }
            if (allTrue){
                return 1;
            }
            return 0;
        }
        
        public boolean contains(PolicyEditorPermissions permission) {
            for (PolicyEditorPermissions policyEditorPermissions : permissions) {
                if (policyEditorPermissions == permission) {
                    return true;
                }
            }
            return false;

        }

        public String getTitle() {
            return title + " ˇ";
        }

        public PolicyEditorPermissions[] getPermissions() {
            return permissions;
        }
        
        
        
    }

    
    private final String name, description;
    private final PermissionType type;
    private final PermissionTarget target;
    private final PermissionActions actions;

    private PolicyEditorPermissions(final String name, final String description,
            final PermissionType type, final PermissionTarget target, final PermissionActions actions) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.target = target;
        this.actions = actions;
    }

    /**
     * A short human-readable name for this permission
     * @return the name of this permission
     */
    public String getName() {
        return this.name;
    }

    /**
     * A longer human-readable description for this permission
     * @return the description of this permission
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * @return the type of this permission, eg java.io.FilePermission
     */
    public PermissionType getType() {
        return this.type;
    }

    /**
     * @return the target of this permission, eg ${user.home}${/}*
     */
    public PermissionTarget getTarget() {
        return this.target;
    }

    /**
     * @return the actions of this permission, eg read,write
     */
    public PermissionActions getActions() {
        return this.actions;
    }

    /**
     * A full String representation of this permission as it should appear when
     * written into a policy file
     * @return a policy file-ready String representation of this permission
     */
    public String toPermissionString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("permission ");
        sb.append(this.type.type);
        sb.append(" \"");
        sb.append(this.target.target);
        sb.append("\"");

        if (!this.actions.equals(PermissionActions.NONE)) {
            sb.append(", \"");
            sb.append(setToActionList(this.actions.getActions().toString()));
            sb.append("\"");
        }

        sb.append(";");

        return sb.toString();
    }

    private static String setToActionList(final String string) {
        return string.replaceAll("[\\[\\]\\s]", "");
    }

    /**
     * Get a PolicyEditorPermissions instance matching the input string
     * @param string a full policy file permissions line, eg `permission java.io.FilePermission "${io.tmpdir}" "read;"`
     * @return the PolicyEditorPermissions value matching the input String, or null if no such match is found
     */
    public static PolicyEditorPermissions fromString(final String string) {
        final CustomPermission tmpPerm = CustomPermission.fromString(string);
        if (tmpPerm == null) {
            return null;
        }

        final PermissionType type = PermissionType.fromString(tmpPerm.type);
        final PermissionTarget target = PermissionTarget.fromString(tmpPerm.target);
        final PermissionActions actions = PermissionActions.fromString(tmpPerm.actions);

        for (final PolicyEditorPermissions perm : PolicyEditorPermissions.values()) {
            final boolean sameType = perm.type.equals(type);
            final boolean sameTarget = perm.target.equals(target);
            final boolean sameActions = perm.actions.getActions().equals(actions.getActions());

            if (sameType && sameTarget && sameActions) {
                return perm;
            }
        }
        return null;
    }

}
