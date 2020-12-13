package com.ofirrubin;

import java.io.*;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


public class Ex1_Tester {
    public static String[][] tests;

    static {
        Ex1_Tester.tests = new String[][]{{"8", "4", "2"}, {"11", "11", "11"}, {"3331", "3331", "3331"}, {"33333331", "33333331", "33333331"}, {"39088169", "63245986", "1"}, {"433494437", "701408733", "1"}, {"1134903170", "1836311903", "1"}, {"33430993", "29764583", "33331"}, {"566666627", "633333289", "33333331"}, {"1666666655", "999999993", "19607843"}};
    }

    public static void main(final String[] args) {
        System.out.println("EX1 Tester:");
        String basePath;
        if (args.length > 0)
            basePath = String.join(" ", args);
        else
            basePath = ".";
        // Looking for java file or folder / zip which contains java file [takes the first one]
        File f = null;
        try {
            f = firstJavaFound(basePath);
        } catch (FileNotFoundException err) { // File not found
            System.err.println(".java file not found in " + (basePath.equals(".") ? "the current dir" : basePath));
            System.exit(-1);
        }
        System.out.println("Found .java file: " + f);

        // Your file.exists test..
        String checksum = "none!";
        try {
            final boolean b = f.exists();
            if (!b) {
                System.err.println("File " + f + " not found! no tests performed!");
                System.exit(-1);
            }
            final MessageDigest md5Digest = MessageDigest.getInstance("MD5");
            checksum = getFileChecksum(md5Digest, f);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Testing file: " + f + " CS: " + checksum);

        PrintStream console = System.out;
        final ByteArrayOutputStream newConsole = new ByteArrayOutputStream();
        System.setOut(new PrintStream(newConsole));
        System.err.println("Start Testing:");
        int ff = 0;
        double grade = 100.0;
        System.out.println("Initializing test inputs before running...");
        String[][] testInputs = getTestValuesOnly(); // CHANGED: Saving the subarray in advanced so it wont take time afterwards time > storage in this case..
        System.out.println("Test begins:");
        final long begin = System.currentTimeMillis();

        for (int in = 0; in < Ex1_Tester.tests.length; ++in) {
            System.err.print(in + ") Testing gcd(" + Ex1_Tester.tests[in][0] + "," + Ex1_Tester.tests[in][1] + ")");
            final long start = System.currentTimeMillis();
            try {
                Ex1.main(testInputs[in]); // CHANGED: I simply used the subarray. Could also be using Arrays.copyOfRange but it might take a ms more.
            } catch (Exception e2) {
                System.err.println("ERR: " + e2);
                grade -= 10.0;
            }
            final long dt = System.currentTimeMillis() - start;
            if (dt > 500L) {
                grade -= 5.0;
                System.err.println("Too long runtime, took: " + dt);
            }
            final String all = newConsole.toString();
            final String se = all.substring(ff);
            final int ind = contains(se, Ex1_Tester.tests[in][2]);
            final boolean res = ind >= 0;
            if (!res) {
                grade -= 10.0;
            }
            ff = all.length();
            System.err.println(" should be " + Ex1_Tester.tests[in][2] + " --> " + res + "  runtime: " + dt + " mili seconds");
        }
        final long rt = System.currentTimeMillis() - begin;
        System.err.println();
        System.err.println("Report: grade: " + grade + "  runtime: " + rt + " mili seconds");
        System.err.println("************************************");
        System.setOut(console);
        System.out.println("Your output: " + newConsole);
    }

    // Make sure tests has at least 2 strings in each cell before running!
    public static String[][] getTestValuesOnly() {
        String[][] t = new String[tests.length][2];
        for (int i = 0; i < t.length; i++) {
            t[i] = new String[]{tests[i][0], tests[i][1]};
        }
        return t;
    }

    // Returns the first .java file it finds in .ZIP, folders, "path" is the parent dir.
    // returns the first file in the following order: found in archive -> any child dir -> base path
    public static File firstJavaFound(String path) throws FileNotFoundException {
        // Looking for the file in the first zip it finds.
        try {
            File f = firstEndsWith(path, "zip");
            System.out.println("ZIP Found at: " + f);
            unzipArchive(String.valueOf(f), path);
        } catch (FileNotFoundException ignored) {
        }

        // Looking for the file in folders
        String[] folders = listFolders(path);
        if (folders != null) {
            for (String folder : folders) {
                try {
                    return firstEndsWith(Paths.get(path, folder).toString(), "java");
                } catch (FileNotFoundException ignored) {
                }
            }
        }

        // Find in the current folder.
        return firstEndsWith(path, "java");
    }

    // Returns the first file it finds which ends with "endsWith", in the parent dir "path".
    public static File firstEndsWith(String path, String endsWith) throws FileNotFoundException {
        File f = new File(path);
        File[] files = f.listFiles((dir, name) -> name.endsWith(endsWith));
        if (files != null && files.length > 0)
            return files[0];
        throw new FileNotFoundException("No file ends with " + endsWith + " In: " + path);
    }

    // Unzips the archive in the destination dir.
    public static void unzipArchive(String fileZip, String dest) {

        File destDir = new File(dest);
        byte[] buffer = new byte[1024];
        try {
            ZipInputStream zis = new ZipInputStream(new FileInputStream(fileZip));
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                File newFile = new File(destDir, String.valueOf(zipEntry));
                if (zipEntry.isDirectory()) {
                    if (!newFile.isDirectory() && !newFile.mkdirs()) {
                        throw new IOException("Failed to create directory " + newFile);
                    }
                } else {
                    // fix for Windows-created archives
                    File parent = newFile.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        throw new IOException("Failed to create directory " + parent);
                    }

                    // write file content
                    FileOutputStream fos = new FileOutputStream(newFile);
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                }
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
            zis.close();
        } catch (java.io.IOException err) {
            System.err.println("Error. Could not extract Ex1.zip");
        }
    }

    // List fall dirs in "basePath" [NOT walking in a tree way. Simple one level search.]
    public static String[] listFolders(String basePath) {
        File f = new File(basePath);
        return f.list((dir, name) -> new File(dir, name).isDirectory());
    }


    public static int contains(final String log, final String ans) {
        return log.indexOf(ans);
    }

    private static String getFileChecksum(final MessageDigest digest, final File file) throws IOException {
        final FileInputStream fis = new FileInputStream(file);
        final byte[] byteArray = new byte[1024];
        int bytesCount = 0;
        while ((bytesCount = fis.read(byteArray)) != -1) {
            digest.update(byteArray, 0, bytesCount);
        }
        fis.close();
        final byte[] bytes = digest.digest();
        final StringBuilder sb = new StringBuilder();
        for (byte aByte : bytes) {
            sb.append(Integer.toString((aByte & 0xFF) + 256, 16).substring(1));
        }
        return sb.toString();
    }
}