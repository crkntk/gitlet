package gitlet;

import java.io.*;
import java.util.*;
import java.nio.file.Files;
import java.nio.file.Paths;

import java.util.HashMap;


/** The Repository class initializes and keeps track of files and ongoing activities made by the user
 */
public class Repository implements Serializable {
    /** INSTANCE VARIABLES THAT HELP WITH KEEPING TRACK OF INFO WITHIN THE CLASS */
    /** the string name of the head */
    private String head;
    /** hashmap that stores all of the commits as keys and their sha-1 codes as values */
    private HashMap<String, Commit> commits;
    /** hashmap that stores all of the branches as keys and their sha-1 codes as values */
    private HashMap<String, String> branches;
    /** hashmap stores all the files that are being staged along with their file name */
    private HashMap<String, Blobs> stage;
    /** list of all of the files that are untracked */
    private LinkedList<String> untracked;
    /** untracked files that are not accounted for yet */
    private LinkedList<String> untrackedFiles;
    /* the string representing the second parentId */
    private String parentId2;
    /* list of all the files that have been modified */
    private LinkedList<String> modified;

    /** initializes the Repository */
    public Repository() {
        try {
            if (!Files.exists(Paths.get(".gitlet"))) {
                Files.createDirectories(Paths.get(".gitlet"));
                Files.createDirectories(Paths.get(".gitlet/commits"));
                Files.createDirectories(Paths.get(".gitlet/staging"));
                Commit newCommit = Commit.init();
                head = newCommit.getBranchName();
                commits = new HashMap<>();
                branches = new HashMap<>();
                stage = new HashMap<>();
                // not implemented or in use yet
                untracked = new LinkedList<>();
                commits.put(newCommit.getSha(), newCommit);
                branches.put(newCommit.getBranchName(), newCommit.getSha());
                modified = new LinkedList<>();
                File newFile = new File(".gitlet/commits/" + newCommit.getSha());
                Utils.writeObject(newFile, newCommit);
                untrackedFiles = new LinkedList<>();
            }
        } catch (IOException e) {
            Utils.message("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        }
    }


    /** returns the head commit */
    public Commit getHead() {
        return commits.get(branches.get(head));
    }

    /** returns commit by its hash code */
    public Commit getCommit(String hash) {
        return commits.get(hash);
    }

    /** looks through all of the files in the repository and generates a list of
     * untracked files, and then given the file name checks the file that the user
     * is trying to add. */
    public void add(String fileName) {
        File newFile = new File(fileName);
        if (!newFile.exists()) {
            Utils.message("File does not exist.");
            return;
        }
        Blobs b = new Blobs(fileName);
        Blobs bcommit = getHead().getBlobs(fileName);
        if (bcommit == null) {
            if (stage.isEmpty() || !stage.containsKey(fileName))
                stage.put(fileName, b);
            else
                stage.replace(fileName, b);
            File blobsFile = new File(".gitlet/staging/" + b.getsha());
            Utils.writeObject(blobsFile, b);
        } else {
            if (bcommit.getsha().equals(b.getsha()))
                stage.remove(fileName);
            else {
                stage.put(fileName, b);
                File blobsFile = new File(".gitlet/staging/" + b.getsha());
                Utils.writeObject(blobsFile, b);
            }
        }
        untrackedFiles.remove(fileName);
    }

    /** makes a new commit */
    public void commit(String message) {
        // check whether the user has entered a message
        if (message == null || message.equals("")) {
            Utils.message("Please enter a commit message.");
            System.exit(0);
        }
        // check if the stage is empty and there are no untracked files,
        // this indicates that nothing has been changed
        if (stage.isEmpty() && untrackedFiles.isEmpty()) {
            Utils.message("No changes added to the commit.");
            System.exit(0);
        }
        Commit parent = getHead();
        Commit parent2 = getCommitFromId(parentId2);
        Commit newCommit = new Commit(message, parent, getCommitFromId(parentId2), stage, false, false, head, untrackedFiles);
        head = newCommit.getBranchName();
        // once you add the file, clear the stage and list of untracked files
        stage.clear();
        untrackedFiles.clear();
        commits.put(newCommit.getSha(), newCommit);
        String newCommitId = newCommit.getSha();
        branches.replace(newCommit.getBranchName(), newCommitId);
        File commitFile = new File(".gitlet/commits/" + newCommitId);
        Utils.writeObject(commitFile, newCommit);
    }

    /**  displays info about the current commit and every commit backwards along the commit tree until the initial commit */
    public void log(Commit head) {
        Commit curr = head;
        while (curr != null) {
            printLog(curr);
            curr = commits.get(curr.getParentId());
        }
    }

    /** displays info about every commit made */
    public void globalLog() {
        for (Map.Entry<String, Commit> entry : commits.entrySet()) {
            Commit c = entry.getValue();
            printLog(c);
        }
    }

    /** helper method for log above */
    public void printLog(Commit curr) {
        System.out.println("===");
        System.out.println("commit " + curr.getSha());
        if (curr.getMerge())
            System.out.println("Merge: " + curr.mergeString());
        System.out.println("Date: " + curr.getDate());
        System.out.println(curr.getMessage());
        System.out.println();
    }

    /** helper that finds split points */
    public Commit getSplitPoint(Commit branchCommit) {
        ArrayList<String> l = findParent();
        while (!l.isEmpty()) {
            if (isParent(l.get(0), branchCommit)) {
                return getCommitFromId(l.get(0));
            }
            l.remove(0);
        }
        return null;
    }

    /** find a list of parents */
    public ArrayList<String> findParent() {
        ArrayList<String> parentList = new ArrayList<>();
        Commit c = getHead();
        findParentHelper(c, parentList);
        return parentList;
    }

    /** finds all of the parents */
    public void findParentHelper(Commit c, ArrayList<String> list) {
        String pId1 = c.getParentId();
        String pId2 = c.getParentId2();
        if (pId1 != null) {
            list.add(pId1);
            findParentHelper(getCommitFromId(pId1), list);
        }
        if (pId2 != null) {
            list.add(pId2);
            findParentHelper(getCommitFromId(pId2), list);
        }
    }

    /** checks whether the commit is actually the parent of another commit*/
    public boolean isParent(String pId, Commit child) {
        Commit curr = child;
        while (curr != null) {
            String p1 = curr.getParentId();
            String p2 = curr.getParentId2();
            if (p1 != null) {
                if (p1.equals(pId))
                    return true;
            }
            if (p2 != null) {
                if (p2.equals(pId))
                    return true;
            }
            curr = commits.get(curr.getParentId());
        }
        return false;
    }

    public void status(){
        LinkedList<String> contentList = new LinkedList<>();
        for (Map.Entry<String, String> entry : branches.entrySet())
            contentList.add(entry.getKey());
        System.out.println("=== " + "Branches" + " ===");
        statusHelper(contentList);
        contentList.clear();
        for (Map.Entry<String, Blobs> entry : stage.entrySet())
            contentList.add(entry.getKey());
        System.out.println("=== " + "Staged Files" + " ===");
        statusHelper(contentList);


        contentList.clear();
        contentList.addAll(untrackedFiles);
        System.out.println("=== " + "Removed Files" + " ===");
        statusHelper(contentList);
        update();
        System.out.println("=== " + "Modifications Not Staged For Commit" + " ===");
        statusHelper(modified);
        System.out.println("=== " + "Untracked Files" + " ===");
        statusHelper(untracked);
        update();
    }

    public void update(){
        // clear any existing files stored in untracked or modified
        File f = new File(System.getProperty("user.dir"));
        File[] allFileList = f.listFiles(File::isFile);
        LinkedList<String> fileList = new LinkedList<>();
        untracked.clear();
        modified.clear();
        for (File file : allFileList) {
            String fileName = file.getName();
            fileList.add(fileName);
            if (file.isFile() && !file.isHidden() && !getHead().tracking(fileName) && !stage.containsKey(fileName)) {
                untracked.add(fileName);
            }
            String content = Utils.readContentsAsString(file);
            if (getHead().tracking(fileName) && !stage.containsKey(fileName)) {
                if (!getHead().getBlobs(fileName).getContents().equals(content)) {
                    String s = fileName + " (modified)";
                    modified.add(s);
                }
            }
            if (stage.containsKey(fileName)){
                // was getting a null pointer exception when this wasn't nested prior
                if (!stage.get(fileName).getContents().equals(content)){
                    String s = fileName + " (modified)";
                    modified.add(s);
                }
            }
        }
        for (Map.Entry<String, Blobs> entry : stage.entrySet()) {
            String fileName = entry.getKey();
            if (!fileList.contains(fileName) && !untrackedFiles.contains(fileName)) {
                String s = fileName + " (deleted)";
                modified.add(s);
            }
        }
        LinkedList<String> trackedFiles = getHead().getAllFiles();
        for (String fileName : trackedFiles) {
            if (!fileList.contains(fileName) && !untrackedFiles.contains(fileName)) {
                String s = fileName + " (deleted)";
                modified.add(s);
            }
        }

    }

    /** print helper for the status method that formats the output according to the spec */
    private void statusHelper(LinkedList<String> contents){
        if (contents != null) {
            Collections.sort(contents);
            for (String s : contents) {
                if (s.equals(head))
                    s = "*" + s;
                System.out.println(s);
            }
        }
        System.out.println();
    }

    /** given a message find the associated commit */
    public void find(String message) {
        boolean found = false;
        for (Map.Entry<String, Commit> entry : commits.entrySet()) {
            String key = entry.getKey();
            Commit c = entry.getValue();
            if (c.getMessage().equals(message)) {
                System.out.println(key);
                found = true;
            }
        }
        if (!found)
            System.out.println("Found no commit with that message.");
    }


    /** checks whether there already exists a branch with the given name, and if there isn't, creates a new branch
     * with the name and the corresponding sha */
    public void branch(String branchName) {
        if (branches.containsKey(branchName)) {
            System.out.println("A branch with that name already exists.");
            return;
        }
        branches.put(branchName, getHead().getSha());
    }

    /** Checkout case 1: java gitlet.Main checkout -- [file name]
     * Takes the version of the file as it exists in the head commit and puts it in the
     * working directory, overwriting the version of the file that’s already there if there is one. */
    public void checkout1(String fileName, Commit c) {
        if (!c.getAllFiles().contains(fileName)) {
            System.out.println("File does not exist in that commit.");
            return;
        }
        Blobs b = c.getBlobs(fileName);
        File f = new File(fileName);
        Utils.writeContents(f, (Object) b.getContent());
        stage.remove(fileName);
    }

    /** find the commit with the appropriate path given the id passed in */
    public Commit getCommitFromId(String id) {
        if (id != null) {
            File commitFile = new File(".gitlet/commits");
            File[] commitList = commitFile.listFiles();
            if (commitList != null && commitList.length != 0) {
                for (File f : commitList) {
                    if (f.getName().contains(id))
                        return commits.get(f.getName());
                }
            }
        }
        return null;
    }

    /** Takes the version of the file as it exists in the commit with the given id,
     * and puts it in the working directory, overwriting the version of the file that’s
     * already there if there is one. The new version of the file is not staged. */
    public void checkout2(String commitID, String fileName) {
        Commit c = getCommitFromId(commitID);
        if (c == null) {
            System.out.println("No commit with that id exists.");
            return;
        }
        checkout1(fileName, c);
    }

    /** Takes all files in the commit at the head of the given branch, and puts them in the
     * working directory, overwriting the versions of the files that are already there
     * if they exist.  */
    public void checkout3(String branch) {
        if (!branches.containsKey(branch)) {
            System.out.println("No such branch exists.");
            return;
        }
        if (getHead().getSha().equals(branches.get(branch)) && branch.equals(head)) {
            System.out.println("No need to checkout the current branch.");
            return;
        }
        Commit checkCommit = getCommit(branches.get(branch));
        checkOutCommit(checkCommit);
        head = branch;
    }

    /** remove if the file has been staged, otherwise check for untracked case and if it does exist */
    public void rm(String fileName) {
        File filepath = null;
        File rmFile = new File(fileName);
        // if the file isn't staged, exit
        if (!stage.containsKey(fileName) && !getHead().tracking(fileName)) {
            Utils.message("No reason to remove the file.");
            return;
        }
        // if the file does exist, get the sha of the file and add it to the appropriate path
        if (rmFile.exists()) {
            Blobs rmBlob = new Blobs(fileName);
            filepath = new File(".gitlet/staging/" + rmBlob.getsha());
        }
        // if the file is untracked, add it to the linkedlist of untracked files
        if (getHead().tracking(fileName)) {
            rmFile.delete();
            untrackedFiles.add(fileName);
        }
        // if the file has been staged, remove the file and from the stage and delete
        if (stage.containsKey(fileName)) {
            stage.remove(fileName);
            if (filepath != null)
                filepath.delete();
            update();
        }
    }

    /** if the branch can be deleted, delete it! */
    public void rmBranch(String branchName) {
        if (!branches.containsKey(branchName)) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        if (branchName.equals(head)) {
            System.out.println("Cannot remove the current branch.");
            return;
        }
        branches.remove(branchName);
    }

    /** check whether there is a commit with the ID that's passed in */
    public void reset(String commitId) {
        Commit c = getCommitFromId(commitId);
        if (c == null) {
            System.out.println("No commit with that id exists.");
            return;
        }
        checkOutCommit(c);
        branches.replace(head, c.getSha());
    }

    /** check the untracked commits and if there are any currently untracked commits
     * ask the user to add and commit them first */
    public void checkUntrackedCommit(Commit branchCommit, LinkedList<String> oldList, LinkedList<String> newList) {
        for (String f : newList) {
            if (!oldList.contains(f)) {
                File newFile = new File(f);
                if (newFile.exists()) {
                    String content = Utils.readContentsAsString(newFile);
                    String branchContent = branchCommit.getBlobs(f).getContents();
                    if (!content.equals(branchContent)) {
                        System.out.println("There is an untracked file in the way; delete it or add and commit it first.");
                        return;
                    }
                }
            }
        }
    }

    /** given a commit, perform the checkout, clearing the stage after migrating the elements from the old list to the new list */
    public void checkOutCommit(Commit c) {
        LinkedList<String> oldList = getHead().getAllFiles();
        LinkedList<String> newList = c.getAllFiles();
        checkUntrackedCommit(c, oldList, newList);
        for (Object o : newList)
            checkout1((String) o, c);
        for (Object s : oldList) {
            if (!newList.contains(s)) {
                File f = new File((String) s);
                f.delete();
            }
        }
        stage.clear();
    }


}