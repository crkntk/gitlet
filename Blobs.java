package gitlet;
import java.io.*;

/** Blob class which contains information associated with the files. */
public class Blobs implements Serializable {
    /** Given a name, constructs a Blob */
    Blobs(String name) {
        File newFile = new File(name);
        content = Utils.readContents(newFile);
        contentString = Utils.readContentsAsString(newFile);
        String temp = "";
        temp = name + Utils.readContentsAsString(newFile);;
        sha1 = Utils.sha1(temp);
    }

    /** return contents in byte */
    public byte[] getContent() {
        return content;
    }

    /** return the sha-1 */
    public String getsha() {
        return sha1;
    }

    public String getContents() {
        return contentString;
    }

    /** The String of the file name. */
    private String fileName;
    /** The byte array to hold content. */
    private byte[] content;
    /** The String to hold hashcode of the whole blob. */
    private String sha1;
    private String contentString;
}