package gitlet;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 */
public class Main {

    /** the repository */
    private static Repository repo;
    private static final List<String> commands = new LinkedList<>(Arrays.asList("init", "add", "commit", "rm", "log", "global-log", "find", "status", "checkout", "branch", "rm-branch", "reset", "merge"));
    private static final List<String> commands_one = new LinkedList<>(Arrays.asList("init", "log", "global-log", "status", "commit"));
    private static final List<String> commands_two = new LinkedList<>(Arrays.asList("add", "commit", "rm", "find", "checkout", "branch", "rm-branch", "reset", "merge"));
    public static void main(String... args) {

        try {
            if (args.length == 0) {
                Utils.message("Please enter a command.");
                System.exit(0);
            }
            if (!commands.contains(args[0])) {
                Utils.message("No command with that name exists.");
                System.exit(0);
            }
            // print out error message if init is called and there's already a repo that's been instantiated
            if (args[0].equals("init") && Files.exists(Paths.get(".gitlet"))) {
                Utils.message("A Gitlet version-control system already exists in the current directory.");
                System.exit(0);
            }

            if (!args[0].equals("init") && commands.contains(args[0]) && !Files.exists(Paths.get(".gitlet"))) {
                Utils.message("Not in an initialized Gitlet directory.");
                System.exit(0);
            }
            if (args.length == 1 && args[0].equals("init") && !Files.exists(Paths.get(".gitlet"))) {
                repo = new Repository();
                File hist = new File(".gitlet/myrepo");
                Utils.writeObject(hist, repo);
            }

            // check whether a repo exists already or not
            if (Files.exists(Paths.get(".gitlet"))) {
                repo = Utils.readObject(new File(".gitlet/myrepo"), Repository.class);
                if (args.length == 1) {
                    switch (args[0]) {
                        case "commit":
                            Utils.message("Please enter a commit message.");
                            System.exit(0);
                            break;
                        case "log":
                            repo.log(repo.getHead());
                            break;
                        case "global-log":
                            repo.globalLog();
                            break;
                        case "status":
                            repo.status();
                            break;
                        default:
                    }
                } else {
                    switch (args[0]) {
                        case "add":
                            repo.add(args[1]);
                            break;
                        case "commit":
                            repo.commit(args[1]);
                            break;
                        case "rm":
                            repo.rm(args[1]);
                            break;
                        case "rm-branch":
                            repo.rmBranch(args[1]);
                            break;
                        case "reset":
                            repo.reset(args[1]);
                            break;
                        case "find":
                            repo.find(args[1]);
                            break;
                        case "branch":
                            repo.branch(args[1]);
                            break;
                        case "checkout":
                            if (args.length == 3 && args[1].equals("--"))
                                repo.checkout1(args[2], repo.getHead());
                            else if (args.length == 4 && args[2].equals("--"))
                                repo.checkout2(args[1], args[3]);
                            else if (args.length == 2)
                                repo.checkout3(args[1]);
                            else {
                                Utils.message("Incorrect operands.");
                                return;
                            }
                            break;
                        default:
                    }
                }
                File f = new File(".gitlet/myrepo");
                Utils.writeObject(f, repo);
            }
        } catch (GitletException e) {
            System.exit(0);
        }
    }
}