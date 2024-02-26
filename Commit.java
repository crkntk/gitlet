package gitlet;

import java.io.File;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/** Commit keeps track of the user's commits and ensures that all associated
 * local variables are kept up to date
 */

public class Commit implements Serializable {
    /** Constructs a commit given parameters */

    /** the time of the commit */
    private ZonedDateTime time;
    /** the time of the commit given as a string*/
    private String date;
    /** The parent of the commit. */
    private String parentId;
    /** The second parent of the commit */
    private String parentId2;
    /** the sha-1 of the commit */
    private String sha;
    /** The String holds the commit message. */
    private String message;
    /** The String holds the branch made started by this commit. */
    private String branchName;
    /** The Hashmap stores all the file on this commit, saved by file name. */
    private HashMap<String, Blobs> files;
    /** Boolean that keeps track of merge */
    private boolean merge;

    public Commit(String message, Commit parent, Commit parent2,
                  HashMap<String, Blobs> stagingMap, boolean init,
                  boolean merged, String branchName, List<String> rmList) {
        if (init) {
            date = "Wed Dec 31 16:00:00 1969 -0800";
            parentId = null;
            files = new HashMap<>();
        } else {
            time = ZonedDateTime.now();
            date = time.format(DateTimeFormatter.ofPattern
                    ("EEE MMM d HH:mm:ss yyyy xxxx"));
            parentId = parent.getSha();
            files = copyAllFiles(parent);
            update(files, stagingMap, rmList);
        }
        this.message = message;
        sha = getSha();
        this.branchName = branchName;
        merge = merged;
    }

    /** initializes a new Commit */
    static Commit init() {
        return new Commit("initial commit", null, null, null, true,
                false, "master", null);
    }

    /** Given the name of a file, return the Blob */
    public Blobs getBlobs(String fileName) {
        if (files.containsKey(fileName))
            return files.get(fileName);
        return null;
    }

    /** Returns the commit's hash (ie: the string of all file names along with the
     * time, their parent, and the associated message) */
    public String getSha() {
        String allfile = "";
        for (Map.Entry<String, Blobs> entry : files.entrySet()) {
            String key = entry.getKey();
            allfile += key;
        }
        if (parentId == null) {
            return Utils.sha1(date, message, allfile);
        }
        String parentToString = parentId;
        if (merge) {
            parentToString += parentId2;
        }
        parentToString += parentId2;
        return Utils.sha1(date, message, parentToString, allfile);
    }

    /** returns the instance variable that keeps track of merging*/
    public boolean getMerge() {
        return merge;
    }

    /** to be added to status to keep track of merging in output */
    public String mergeString(){
        return getParentId().substring(0, 7) + " "  + getParentId2().substring(0, 7);
    }

    /** Returns the branch of the commit */
    public String getBranchName() {
        return branchName;
    }

    /** Returns all of the files in the commit */
    public LinkedList<String> getAllFiles() {
        LinkedList<String> result = new LinkedList<String>();
        for (Map.Entry<String, Blobs> entry : files.entrySet()) {
            String key = entry.getKey();
            result.add(key);
        }
        return result;
    }

    /** Copies files from the parent */
    public HashMap<String, Blobs> copyAllFiles(Commit parent) {
        HashMap<String, Blobs> result = new HashMap<String, Blobs>();
        for (Map.Entry<String, Blobs> entry : parent.files.entrySet()) {
            String key = entry.getKey();
            Blobs b = entry.getValue();
            result.put(key, b);
        }
        return result;
    }

    /** Update the hashmap with all the file names for the commit */
    public void update(HashMap<String, Blobs> filemap, HashMap<String, Blobs> stagingMap, List<String> removeList) {
        if (!stagingMap.isEmpty()) {
            for (Map.Entry<String, Blobs> entry : stagingMap.entrySet()) {
                String filename = entry.getKey();
                Blobs stagingB = entry.getValue();
                if (!filemap.containsKey(filename))
                    filemap.put(filename, stagingB);
                else {
                    Blobs commitB = (Blobs) filemap.get(filename);
                    if (!commitB.getsha().equals(stagingB.getsha()))
                        filemap.replace(filename, stagingB);
                }
                File toDelete = new File(".gitlet/staging/" + stagingB.getsha());
                toDelete.delete();
            }
        }
        if (!removeList.isEmpty()) {
            while (!removeList.isEmpty())
                filemap.remove(removeList.remove(0));
        }
    }

    /** Return the parent of this commit's sha-1 */
    public String getParentId() {
        return parentId;
    }

    /** Return the commit's second parent's sha-1. */
    public String getParentId2() {
        return parentId2;
    }

    /** check if the commit is tracking a file. */
    public boolean tracking(String fileName) {
        return files.containsKey(fileName);
    }

    /** return the time of the commit */
    public String getDate() {
        return date;
    }

    /** return the message of the commit */
    public String getMessage() {
        return message;
    }

    /** return the hashmap of all the files*/
    public HashMap<String, Blobs> getFiles() {
        return files;
    }



}