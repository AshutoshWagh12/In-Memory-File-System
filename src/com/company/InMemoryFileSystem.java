package com.company;


import java.util.*;

class File {
    String name;
    String content;

    public File(String name) {
        this.name = name;
        this.content = "";
    }
}

class Directory {
    String name;
    Map<String, Directory> directories;
    Map<String, File> files;

    public Directory(String name) {
        this.name = name;
        this.directories = new HashMap<>();
        this.files = new HashMap<>();
    }
}

public class InMemoryFileSystem {
    private Directory root;
    private Directory currentDirectory;

    public InMemoryFileSystem() {
        this.root = new Directory("/");
        this.currentDirectory = this.root;
    }

    public void mkdir(String dirName) {
        if (is_Valid_Name(dirName)) {
            currentDirectory.directories.put(dirName, new Directory(dirName));
        } else {
            System.out.println("Invalid directory name: " + dirName);
        }
    }

    public void cd(String path) {
        if (path.equals("/")) {
            currentDirectory = root;
            return;
        }

        String[] dirs = path.split("/");
        for (String dir : dirs) {
            if (dir.equals("..")) {
                if (currentDirectory != root) {
                    currentDirectory = findParentDirectory(currentDirectory);
                }
            } else {
                if (currentDirectory.directories.containsKey(dir)) {
                    currentDirectory = currentDirectory.directories.get(dir);
                } else {
                    System.out.println("Directory not found: " + path);
                    return;
                }
            }
        }
    }

    public void ls(String path) {
        Directory dir = path.isEmpty() ? currentDirectory : findDirectory(path);
        if (dir == null) {
            System.out.println("Directory not found: " + path);
            return;
        }

        System.out.println("Contents of " + dir.name + ":");
        for (String name : dir.directories.keySet()) {
            System.out.println("Directory: " + name);
        }
        for (String name : dir.files.keySet()) {
            System.out.println("File: " + name);
        }
    }

    public void touch(String fileName) {
        if (is_Valid_Name(fileName)) {
            currentDirectory.files.put(fileName, new File(fileName));
        } else {
            System.out.println("Invalid file name: " + fileName);
        }
    }

    public void cat(String fileName) {
        File file = currentDirectory.files.get(fileName);
        if (file != null) {
            System.out.println(file.content);
        } else {
            System.out.println("File not found: " + fileName);
        }
    }

    public void echo(String fileName, String content) {
        if (is_Valid_Name(fileName)) {
            File file = currentDirectory.files.get(fileName);
            if (file != null) {
                file.content = content;
            } else {
                File newFile = new File(fileName);
                newFile.content = content;
                currentDirectory.files.put(fileName, newFile);
            }
        } else {
            System.out.println("Invalid file name: " + fileName);
        }
    }

    public void mv(String sourcePath, String destinationPath) {
        if (sourcePath.isEmpty() || destinationPath.isEmpty()) {
            System.out.println("Invalid source or destination path.");
            return;
        }

        Directory sourceDir = findDirectory(sourcePath);
        Directory destDir = findDirectory(destinationPath);

        if (sourceDir != null && destDir != null) {
            String[] sourceDirs = sourcePath.split("/");
            String sourceName = sourceDirs[sourceDirs.length - 1];
            if (sourceDir.files.containsKey(sourceName)) {
                File fileToMove = sourceDir.files.get(sourceName);
                destDir.files.put(sourceName, fileToMove);
                sourceDir.files.remove(sourceName);
                System.out.println("File moved successfully.");
            } else {
                System.out.println("File not found: " + sourceName);
            }
        } else {
            System.out.println("Unable to move. Source or destination not found.");
        }
    }

    public void cp(String sourcePath, String destinationPath) {
        if (sourcePath.isEmpty() || destinationPath.isEmpty()) {
            System.out.println("Invalid source or destination path.");
            return;
        }

        Directory sourceDir = findDirectory(sourcePath);
        Directory destDir = findDirectory(destinationPath);

        if (sourceDir != null && destDir != null) {
            String[] sourceDirs = sourcePath.split("/");
            String sourceName = sourceDirs[sourceDirs.length - 1];
            if (sourceDir.files.containsKey(sourceName)) {
                File fileToCopy = sourceDir.files.get(sourceName);
                destDir.files.put(sourceName, new File(sourceName));
                destDir.files.get(sourceName).content = fileToCopy.content;
                System.out.println("File copied successfully.");
            } else {
                System.out.println("File not found: " + sourceName);
            }
        } else {
            System.out.println("Unable to copy. Source or destination not found.");
        }
    }

    public void rm(String path) {
        if (path.isEmpty()) {
            System.out.println("Invalid file or directory path.");
            return;
        }

        Directory parentDir = findDirectory(path.substring(0, path.lastIndexOf('/') + 1));
        String name = path.substring(path.lastIndexOf('/') + 1);

        if (parentDir != null && parentDir.files.containsKey(name)) {
            parentDir.files.remove(name);
            System.out.println("File removed successfully.");
        } else {
            System.out.println("Unable to remove. File or directory not found.");
        }
    }

    private Directory findDirectory(String path) {
        String[] dirs = path.split("/");
        Directory current = root;
        for (String dir : dirs) {
            if (current.directories.containsKey(dir)) {
                current = current.directories.get(dir);
            } else {
                return null;
            }
        }
        return current;
    }

    private Directory findParentDirectory(Directory currentDirectory) {
        return findParent(root, currentDirectory);
    }

    private Directory findParent(Directory current, Directory target) {
        if (current.directories.containsValue(target)) {
            return current;
        }

        for (Directory subdir : current.directories.values()) {
            Directory result = findParent(subdir, target);
            if (result != null) {
                return result;
            }
        }

        return null;
    }

    private boolean is_Valid_Name(String name) {
        return name != null && !name.isEmpty() && name.matches("[a-zA-Z0-9_.]+");
    }


    public static void main(String[] args) {
        InMemoryFileSystem fileSystem = new InMemoryFileSystem();
        Scanner scanner = new Scanner(System.in);
        String command;

        System.out.println("Welcome to In-Memory File System!!!");
        System.out.println("Available commands: mkdir, cd, ls, touch, cat, echo, mv, cp, rm, exit");

        do {
            System.out.print(fileSystem.currentDirectory.name + " $ ");
            command = scanner.nextLine().trim();
            String[] parts = command.split("\\s+", 2);
            String operation = parts[0].toLowerCase();

            switch (operation) {
                case "mkdir":
                    if (parts.length > 1) {
                        fileSystem.mkdir(parts[1]);
                    } else {
                        System.out.println("Usage: mkdir <directory_name>");
                    }
                    break;
                case "cd":
                    if (parts.length > 1) {
                        fileSystem.cd(parts[1]);
                    } else {
                        System.out.println("Usage: cd <directory_path>");
                    }
                    break;
                case "ls":
                    if (parts.length > 1) {
                        fileSystem.ls(parts[1]);
                    } else {
                        fileSystem.ls("");
                    }
                    break;
                case "touch":
                    if (parts.length > 1) {
                        fileSystem.touch(parts[1]);
                    } else {
                        System.out.println("Usage: touch <file_name>");
                    }
                    break;
                case "cat":
                    if (parts.length > 1) {
                        fileSystem.cat(parts[1]);
                    } else {
                        System.out.println("Usage: cat <file_name>");
                    }
                    break;
                case "echo":
                    if (parts.length > 2) {
                        fileSystem.echo(parts[1], parts[2]);
                    } else {
                        System.out.println("Usage: echo <file_name> <content>");
                    }
                    break;
                case "mv":
                    if (parts.length > 2) {
                        fileSystem.mv(parts[1], parts[2]);
                    } else {
                        System.out.println("Usage: mv <source_path> <destination_path>");
                    }
                    break;
                case "cp":
                    if (parts.length > 2) {
                        fileSystem.cp(parts[1], parts[2]);
                    } else {
                        System.out.println("Usage: cp <source_path> <destination_path>");
                    }
                    break;
                case "rm":
                    if (parts.length > 1) {
                        fileSystem.rm(parts[1]);
                    } else {
                        System.out.println("Usage: rm <file_or_directory_path>");
                    }
                    break;
                case "exit":
                    System.out.println("Exiting...");
                    break;
                default:
                    System.out.println("Unknown command. Available commands: mkdir, cd, ls, touch, cat, echo, mv, cp, rm, exit");
            }
        } while (!command.equalsIgnoreCase("exit"));

        scanner.close();
    }
}

