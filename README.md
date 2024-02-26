#GitLet
This is a software with basic functionality of github




Init
Usage: java gitlet.Main init

Description: Creates a new Gitlet version-control system in the current directory. This system will automatically start with one commit: a commit that contains no files and has the commit message initial commit (just like that, with no punctuation). It will have a single branch: master, which initially points to this initial commit, and master will be the current branch. The timestamp for this initial commit will be 00:00:00 UTC, Thursday, 1 January 1970 in whatever format you choose for dates (this is called “The (Unix) Epoch”, represented internally by the time 0.) Since the initial commit in all repositories created by Gitlet will have exactly the same content, it follows that all repositories will automatically share this commit (they will all have the same UID) and all commits in all repositories will trace back to it.

Runtime: Should be constant relative to any significant measure.

Failure cases: If there is already a Gitlet version-control system in the current directory, it should abort. It should NOT overwrite the existing system with a new one. Should print the error message A Gitlet version-control system already exists in the current directory.

Dangerous?: No

Our line count: ~25

add
Usage: java gitlet.Main add [file name]

Description: Adds a copy of the file as it currently exists to the staging area (see the description of the commit command). For this reason, adding a file is also called staging the file for addition. Staging an already-staged file overwrites the previous entry in the staging area with the new contents. The staging area should be somewhere in .gitlet. If the current working version of the file is identical to the version in the current commit, do not stage it to be added, and remove it from the staging area if it is already there (as can happen when a file is changed, added, and then changed back). The file will no longer be staged for removal (see gitlet rm), if it was at the time of the command.

Runtime: In the worst case, should run in linear time relative to the size of the file being added and 
, for 
 the number of files in the commit.

Failure cases: If the file does not exist, print the error message File does not exist. and exit without changing anything.

Dangerous?: No

Our line count: ~20

commit
Usage: java gitlet.Main commit [message]

Description: Saves a snapshot of certain files in the current commit and staging area so they can be restored at a later time, creating a new commit. The commit is said to be tracking the saved files. By default, each commit’s snapshot of files will be exactly the same as its parent commit’s snapshot of files; it will keep versions of files exactly as they are, and not update them. A commit will only update the contents of files it is tracking that have been staged for addition at the time of commit, in which case the commit will now include the version of the file that was staged instead of the version it got from its parent. A commit will save and start tracking any files that were staged for addition but weren’t tracked by its parent. Finally, files tracked in the current commit may be untracked in the new commit as a result being staged for removal by the rm command (below).

The bottom line: By default a commit is the same as its parent. Files staged for addition and removal are the updates to the commit.

Some additional points about commit:

The staging area is cleared after a commit.

The commit command never adds, changes, or removes files in the working directory (other than those in the .gitlet directory). The rm command will remove such files, as well as staging them for removal, so that they will be untracked after a commit.

Any changes made to files after staging for addition or removal are ignored by the commit command, which only modifies the contents of the .gitlet directory. For example, if you remove a tracked file using the Unix rm command (rather than Gitlet’s command of the same name), it has no effect on the next commit, which will still contain the deleted version of the file.

After the commit command, the new commit is added as a new node in the commit tree.

The commit just made becomes the “current commit”, and the head pointer now points to it. The previous head commit is this commit’s parent commit.

Each commit should contain the date and time it was made.

Each commit has a log message associated with it that describes the changes to the files in the commit. This is specified by the user. The entire message should take up only one entry in the array args that is passed to main. To include multiword messages, you’ll have to surround them in quotes.

Each commit is identified by its SHA-1 id, which must include the file (blob) references of its files, parent reference, log message, and commit time.

Runtime: Runtime should be constant with respect to any measure of number of commits. Runtime must be no worse than linear with respect to the total size of files the commit is tracking. Additionally, this command has a memory requirement: Committing must increase the size of the .gitlet directory by no more than the total size of the files staged for addition at the time of commit, not including additional metadata. This means don’t store redundant copies of versions of files that a commit receives from its parent. You are allowed to save whole additional copies of files; don’t worry about only saving diffs, or anything like that.

Failure cases: If no files have been staged, abort. Print the message No changes added to the commit. Every commit must have a non-blank message. If it doesn’t, print the error message Please enter a commit message. It is not a failure for tracked files to be missing from the working directory or changed in the working directory. Just ignore everything outside the .gitlet directory entirely.

Dangerous?: No

Differences from real git: In real git, commits may have multiple parents (due to merging) and also have considerably more metadata.

Our line count: ~35

rm
Usage: java gitlet.Main rm [file name]

Description: Unstage the file if it is currently staged for addition. If the file is tracked in the current commit, stage it for removal and remove the file from the working directory if the user has not already done so (do not remove it unless it is tracked in the current commit).

Runtime: Should run in constant time relative to any significant measure.

Failure cases: If the file is neither staged nor tracked by the head commit, print the error message No reason to remove the file.

Dangerous?: Yes (although if you use our utility methods, you will only hurt your repository files, and not all the other files in your directory.)

Our line count: ~20

log
Usage: java gitlet.Main log

global-log
Usage: java gitlet.Main global-log

Description: Like log, except displays information about all commits ever made. The order of the commits does not matter.

Runtime: Linear with respect to the number of commits ever made.

Failure cases: None

Dangerous?: No

Our line count: ~10

find
Usage: java gitlet.Main find [commit message]

Description: Prints out the ids of all commits that have the given commit message, one per line. If there are multiple such commits, it prints the ids out on separate lines. The commit message is a single operand; to indicate a multiword message, put the operand in quotation marks, as for the commit command below.

Runtime: Should be linear relative to the number of commits.

Failure cases: If no such commit exists, prints the error message Found no commit with that message.

Dangerous?: No

Differences from real git: Doesn’t exist in real git. Similar effects can be achieved by grepping the output of log.

Our line count: ~15

status
Usage: java gitlet.Main status

Description: Displays what branches currently exist, and marks the current branch with a *. Also displays what files have been staged for addition or removal. An example of the exact format it should follow is as follows.

checkout
Checkout is a kind of general command that can do a few different things depending on what its arguments are. There are 3 possible use cases. In each section below, you’ll see 3 bullet points. Each corresponds to the respective usage of checkout.

Usages:

java gitlet.Main checkout -- [file name]

java gitlet.Main checkout [commit id] -- [file name]

java gitlet.Main checkout [branch name]

Descriptions:

Takes the version of the file as it exists in the head commit, the front of the current branch, and puts it in the working directory, overwriting the version of the file that’s already there if there is one. The new version of the file is not staged.

Takes the version of the file as it exists in the commit with the given id, and puts it in the working directory, overwriting the version of the file that’s already there if there is one. The new version of the file is not staged.

Takes all files in the commit at the head of the given branch, and puts them in the working directory, overwriting the versions of the files that are already there if they exist. Also, at the end of this command, the given branch will now be considered the current branch (HEAD). Any files that are tracked in the current branch but are not present in the checked-out branch are deleted. The staging area is cleared, unless the checked-out branch is the current branch (see Failure cases below).

Runtimes:

Should be linear relative to the size of the file being checked out.

Should be linear with respect to the total size of the files in the commit’s snapshot. Should be constant with respect to any measure involving number of commits. Should be constant with respect to the number of branches.

Failure cases:

If the file does not exist in the previous commit, abort, printing the error message File does not exist in that commit.

If no commit with the given id exists, print No commit with that id exists. Otherwise, if the file does not exist in the given commit, print the same message as for failure case 1.

If no branch with that name exists, print No such branch exists. If that branch is the current branch, print No need to checkout the current branch. If a working file is untracked in the current branch and would be overwritten by the checkout, print There is an untracked file in the way; delete it, or add and commit it first. and exit; perform this check before doing anything else.

Differences from real git: Real git does not clear the staging area and stages the file that is checked out. Also, it won’t do a checkout that would overwrite or undo changes (additions or removals) that you have staged.

A [commit id] is, as described earlier, a hexadecimal numeral. A convenient feature of real Git is that one can abbreviate commits with a unique prefix. For example, one might abbreviate a0da1ea5a15ab613bf9961fd86f010cf74c7ee48 as a0da1e in the (likely) event that no other object exists with a SHA-1 identifier that starts with the same six digits. You should arrange for the same thing to happen for commit ids that contain fewer than 40 characters. Unfortunately, using shortened ids might slow down the finding of objects if implemented naively (making the time to find a file linear in the number of objects), so we won’t worry about timing for commands that use shortened ids. We suggest, however, that you poke around in a .git directory (specifically, .git/objects) and see how it manages to speed up its search. You will perhaps recognize a familiar data structure implemented with the file system rather than pointers.

Only version 3 (checkout of a full branch) modifies the staging area: otherwise files scheduled for addition or removal remain so.

Dangerous?: Yes!

Our line counts:

~15
~5
~15
branch
Usage: java gitlet.Main branch [branch name]

Description: Creates a new branch with the given name, and points it at the current head node. A branch is nothing more than a name for a reference (a SHA-1 identifier) to a commit node. This command does NOT immediately switch to the newly created branch (just as in real Git). Before you ever call branch, your code should be running with a default branch called “master”.

Runtime: Should be constant relative to any significant measure.

Failure cases: If a branch with the given name already exists, print the error message A branch with that name already exists.

Dangerous?: No

Our line count: ~10


rm-branch
Usage: java gitlet.Main rm-branch [branch name]

Description: Deletes the branch with the given name. This only means to delete the pointer associated with the branch; it does not mean to delete all commits that were created under the branch, or anything like that.

Runtime: Should be constant relative to any significant measure.

Failure cases: If a branch with the given name does not exist, aborts. Print the error message A branch with that name does not exist. If you try to remove the branch you’re currently on, aborts, printing the error message Cannot remove the current branch.

Dangerous?: No

Our line count: ~15

reset
Usage: java gitlet.Main reset [commit id]

Description: Checks out all the files tracked by the given commit. Removes tracked files that are not present in that commit. Also moves the current branch’s head to that commit node. See the intro for an example of what happens to the head pointer after using reset. The [commit id] may be abbreviated as for checkout. The staging area is cleared. The command is essentially checkout of an arbitrary commit that also changes the current branch head.

Runtime: Should be linear with respect to the total size of files tracked by the given commit’s snapshot. Should be constant with respect to any measure involving number of commits.

Failure case: If no commit with the given id exists, print No commit with that id exists. If a working file is untracked in the current branch and would be overwritten by the reset, print There is an untracked file in the way; delete it, or add and commit it first. and exit; perform this check before doing anything else.

Dangerous?: Yes!

Differences from real git: This command is closest to using the --hard option, as in git reset --hard [commit hash].

Our line count: ~10

merge
Usage: java gitlet.Main merge [branch name]

Description: Merges files from the given branch into the current branch. This method is a bit complicated, so here’s a more detailed description:

First consider what might be called the split point of the current branch and the given branch. For example, if master is the current branch and branch is the given branch:



A few notes about the remote commands:

Execution time will not be graded. For your own edification, please don’t do anything ridiculous, though.

All the commands are significantly simplified from their git equivalents, so specific differences from git are usually not notated. Be aware they are there, however.

So now let’s go over the commands:

add-remote
Usage: `java gitlet.Main add-remote [remote name] [name of remote directory]/.gitlet

Description: Saves the given login information under the given remote name. Attempts to push or pull from the given remote name will then attempt to use this .gitlet directory. By writing, e.g., java gitlet.Main add-remote other ../testing/otherdir/.gitlet you can provide tests of remotes that will work from all locations (on your home machine or within the grading program’s software). Always use forward slashes in these commands. Have your program convert all the forward slashes into the path separator character (forward slash on Unix and backslash on Windows). Java helpfully defines the class variable java.io.File.separator as this character.

Failure cases: If a remote with the given name already exists, print the error message: A remote with that name already exists. You don’t have to check if the user name and server information are legit.

Dangerous?: No.

rm-remote
Usage: java gitlet.Main rm-remote [remote name]

Description: Remove information associated with the given remote name. The idea here is that if you ever wanted to change a remote that you added, you would have to first remove it and then re-add it.

Failure cases: If a remote with the given name does not exist, print the error message: A remote with that name does not exist.

Dangerous?: No.

push
Usage: java gitlet.Main push [remote name] [remote branch name]

Description: Attempts to append the current branch’s commits to the end of the given branch at the given remote. Details:

This command only works if the remote branch’s head is in the history of the current local head, which means that the local branch contains some commits in the future of the remote branch. In this case, append the future commits to the remote branch. Then, the remote should reset to the front of the appended commits (so its head will be the same as the local head). This is called fast-forwarding.

If the Gitlet system on the remote machine exists but does not have the input branch, then simply add the branch to the remote Gitlet.

Failure cases: If the remote branch’s head is not in the history of the current local head, print the error message Please pull down remote changes before pushing. If the remote .gitlet directory does not exist, print Remote directory not found.

Dangerous?: No.

fetch
Usage: java gitlet.Main fetch [remote name] [remote branch name]

Description: Brings down commits from the remote Gitlet repository into the local Gitlet repository. Basically, this copies all commits and blobs from the given branch in the remote repository (that are not already in the current repository) into a branch named [remote name]/[remote branch name] in the local .gitlet (just as in real Git), changing [remote name]/[remote branch name] to point to the head commit (thus copying the contents of the branch from the remote repository to the current one). This branch is created in the local repository if it did not previously exist.

Failure cases: If the remote Gitlet repository does not have the given branch name, print the error message That remote does not have that branch. If the remote .gitlet directory does not exist, print Remote directory not found.
Dangerous? No

pull
Usage: java gitlet.Main pull [remote name] [remote branch name]

Description: Fetches branch [remote name]/[remote branch name] as for the fetch command, and then merges that fetch into the current branch.

Failure cases: Just the failure cases of fetch and merge together.

Dangerous? Yes!
