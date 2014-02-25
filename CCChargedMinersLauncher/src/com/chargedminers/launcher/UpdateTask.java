package com.chargedminers.launcher;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.CRC32;
import javax.swing.SwingWorker;
import com.chargedminers.launcher.gui.UpdateScreen;
import com.chargedminers.shared.SharedUpdaterCode;
import com.chargedminers.shared.SharedUpdaterCode.OperatingSystem;

// Handles downloading and deployment of client updates,
// as well as resource files used by the client.
public final class UpdateTask
        extends SwingWorker<Boolean, UpdateTask.ProgressUpdate> {

    // =============================================================================================
    //                                                                    CONSTANTS & INITIALIZATION
    // =============================================================================================
    private static final int MAX_PARALLEL_DOWNLOADS = 2;
    private static final UpdateTask instance = new UpdateTask();

    public static UpdateTask getInstance() {
        return instance;
    }

    private UpdateTask() {
    }

    // =============================================================================================
    //                                                                                          MAIN
    // =============================================================================================
    private Thread[] workerThreads;
    private final List<FileToDownload> files = new ArrayList<>();
    private int activeFileNumber, filesDone, totalFiles;
    private boolean needLzma;
    private boolean updatesApplied;

    @Override
    protected Boolean doInBackground()
            throws Exception {
        this.digest = MessageDigest.getInstance("MD5");
        final Logger logger = LogUtil.getLogger();

        // build up file list
        logger.log(Level.INFO, "Checking for updates.");
        files.addAll(pickBinariesToDownload());

        if (files.isEmpty()) {
            logger.log(Level.INFO, "No updates needed.");

        } else {
            this.updatesApplied = true;
            logger.log(Level.INFO, "Downloading updates: {0}", listFileNames(files));

            this.activeFileNumber = 0;
            this.totalFiles = files.size();

            if (needLzma) {
                // We need to get lzma.jar before deploying any other files, because some of them
                // may need to be decompressed. "lzma.jar" will always be the first on the list.
                processOneFile(getNextFileSync(false));
            }

            // The rest of the files are processed by worker threads.
            int numThreads = Math.min(totalFiles, MAX_PARALLEL_DOWNLOADS);
            workerThreads = new Thread[numThreads];
            for (int i = 0; i < numThreads; i++) {
                workerThreads[i] = new DownloadThread(logger);
                workerThreads[i].start();
            }
            // Wait for all workers to finish
            for (int i = 0; i < numThreads; i++) {
                workerThreads[i].join();
            }
        }

        // confirm that all required files have been downloaded and deployed
        verifyFiles(files);

        if (this.updatesApplied) {
            logger.log(Level.INFO, "Updates applied.");
        }
        return true;
    }

    private void processOneFile(final FileToDownload file)
            throws InterruptedException, IOException {
        // step 1: download
        final File downloadedFile = downloadFile(file);

        // step 2: deploy
        deployFile(downloadedFile, file.targetName);
    }

    // Make a list of local names for all files that we intend to download, for logging
    private static String listFileNames(final List<FileToDownload> files) {
        if (files == null) {
            throw new NullPointerException("files");
        }
        final StringBuilder sb = new StringBuilder();
        String sep = "";
        for (final FileToDownload s : files) {
            sb.append(sep).append(s.localName.getName());
            sep = ", ";
        }
        return sb.toString();
    }

    // Grabs the next file from the list, and sends a progress report to UpdateScreen.
    // Returns null when there are no more files left to download.
    private synchronized FileToDownload getNextFileSync(boolean fileWasDone) {
        if (fileWasDone) {
            filesDone++;
        }
        FileToDownload fileToReturn = null;
        String fileNameToReport;

        if (activeFileNumber != totalFiles) {
            fileToReturn = files.get(activeFileNumber);
            activeFileNumber++;
            fileNameToReport = fileToReturn.localName.getName();
        } else {
            fileNameToReport = files.get(totalFiles - 1).localName.getName();
        }

        int overallProgress = (this.filesDone * 100 + 100) / this.totalFiles;
        final String status = String.format("Updating %s (%d/%d)",
                fileNameToReport, this.activeFileNumber, this.totalFiles);
        this.publish(new ProgressUpdate(status, overallProgress));
        return fileToReturn;
    }

    // =============================================================================================
    //                                                                        CHECKING / DOWNLOADING
    // =============================================================================================
    private MessageDigest digest;
    public static final String FILE_INDEX_URL = SharedUpdaterCode.BASE_URL + "releases/version.txt",
            LAUNCHER_JAR = "launcher.jar";

    private List<FileToDownload> pickBinariesToDownload()
            throws IOException {
        final List<FileToDownload> filesToDownload = new ArrayList<>();
        final List<FileToDownload> localFiles = listBinaries();
        final HashMap<String, RemoteFile> remoteFiles = getRemoteIndex();
        final boolean updateExistingFiles = (Prefs.getUpdateMode() != UpdateMode.DISABLED);

        // Getting remote file index failed. Abort update.
        if (remoteFiles == null) {
            return filesToDownload;
        }

        for (final FileToDownload localFile : localFiles) {
            signalCheckProgress(localFile.localName.getName());

            final RemoteFile remoteFile = remoteFiles.get(localFile.remoteName);
            boolean download = false;
            boolean localFileMissing = !localFile.localName.exists();
            File fileToHash = localFile.localName;

            // lzma.jar and launcher.jar get special treatment
            boolean isLauncherJar = (localFile == launcherJarFile);

            if (isLauncherJar) {
                if (localFileMissing) {
                    // If launcher.jar is missing from its usual location, that means we're
                    // currently running from somewhere else. We need to take care to avoid
                    // repeated attempts to update the launcher.
                    LogUtil.getLogger().log(Level.WARNING,
                            "launcher.jar is not present in its usual location!");
                    // We check if "launcher.jar.new" is up-to-date (instead of checking "launcher.jar"),
                    // and only download it if UpdateMode is not DISABLED.
                    fileToHash = localFile.targetName;
                    localFileMissing = !localFile.targetName.exists();
                } else if (localFile.targetName.exists()) {
                    // If "launcher.jar.new" already exists, just check if it's up-to-date.
                    fileToHash = localFile.targetName;
                    LogUtil.getLogger().log(Level.WARNING,
                            "launcher.jar.new already exists: we're probably not running from self-updater.");
                }
            }

            if (localFileMissing) {
                // If local file does not exist
                LogUtil.getLogger().log(Level.INFO,
                        "Will download {0}: does not exist locally", localFile.localName.getName());
                download = true;

            } else if (updateExistingFiles) {
                // If local file exists, but may need updating
                if (remoteFile != null) {
                    try {
                        final String localHash = computeFileHash(fileToHash);
                        if (!localHash.equalsIgnoreCase(remoteFile.hash)) {
                            // If file contents don't match
                            LogUtil.getLogger().log(Level.INFO,
                                    "Contents of {0} don''t match ({1} vs {2}). Will re-download.",
                                    new Object[]{fileToHash.getName(), localHash, remoteFile.hash});
                            download = true;
                        }
                    } catch (final IOException ex) {
                        LogUtil.getLogger().log(Level.SEVERE,
                                "Error computing hash of a local file. Will attempt to re-download.", ex);
                        download = true;
                    } catch (final SecurityException ex) {
                        String logMsg = "Error verifying " + fileToHash.getName() + ". Will re-download.";
                        LogUtil.getLogger().log(Level.SEVERE, logMsg, ex);
                        download = true;
                    }
                } else {
                    LogUtil.getLogger().log(Level.WARNING,
                            "No remote match for local file {0}", fileToHash.getName());
                }
            }

            if (download) {
                if (remoteFile == null) {
                    String errMsg = String.format("Required file \"%s%s\" cannot be found.",
                            localFile.baseUrl, localFile.remoteName);
                    throw new RuntimeException(errMsg);
                }
                filesToDownload.add(localFile);
            }
        }
        return filesToDownload;
    }

    private FileToDownload launcherJarFile, primaryBinaryFile, secondaryBinaryFile;

    private List<FileToDownload> listBinaries()
            throws IOException {
        List<FileToDownload> localFiles = new ArrayList<>();
        final File dataDir = SharedUpdaterCode.getDataDir();

        launcherJarFile = new FileToDownload(SharedUpdaterCode.BASE_URL + "launcher/",
                "launcher.jar",
                new File(dataDir, LAUNCHER_JAR),
                new File(dataDir, SharedUpdaterCode.LAUNCHER_NEW_JAR_NAME));
        localFiles.add(launcherJarFile);

        String primaryBinaryName = PathUtil.getBinaryName(true);
        primaryBinaryFile = new FileToDownload(SharedUpdaterCode.BASE_URL + "releases/",
                primaryBinaryName, new File(dataDir, primaryBinaryName));
        localFiles.add(primaryBinaryFile);

        String altBinaryName = PathUtil.getBinaryName(false);
        if (altBinaryName != null) {
            secondaryBinaryFile = new FileToDownload(SharedUpdaterCode.BASE_URL + "releases/",
                    altBinaryName, new File(dataDir, altBinaryName));
            localFiles.add(secondaryBinaryFile);
        }
        return localFiles;
    }

    // get a list of binaries available from CC.net
    // TODO: port VersionsTxt from old ChargedMinersLauncher
    private HashMap<String, RemoteFile> getRemoteIndex() {
        final String hashIndex = HttpUtil.downloadString(FILE_INDEX_URL);
        final HashMap<String, RemoteFile> remoteFiles = new HashMap<>();

        // if getting the list failed, don't panic. Abort update instead.
        if (hashIndex == null) {
            return null;
        }

        // the rest of the files
        for (final String line : hashIndex.split("\\r?\\n")) {
            final String[] components = line.split(" ");
            final RemoteFile file = new RemoteFile();
            file.name = components[0];
            file.hash = components[2].toLowerCase();
            remoteFiles.put(file.name.toLowerCase(), file);
        }
        return remoteFiles;
    }

    private String computeFileHash(File file)
            throws FileNotFoundException, IOException {
        try (InputStream is = new FileInputStream(file)) {
            final byte[] ioBuffer = new byte[64 * 1024];
            try (final DigestInputStream dis = new DigestInputStream(is, digest)) {
                while (dis.read(ioBuffer) != -1) {
                    // DigestInputStream is doing its job, we just need to read through it.
                }
            }
        }
        final byte[] localHashBytes = digest.digest();
        final String hashString = new BigInteger(1, localHashBytes).toString(16);
        return padLeft(hashString, '0', 40);
    }

    private static String padLeft(final String s, final char c, final int n) {
        if (s == null) {
            throw new NullPointerException("s");
        }
        final StringBuilder sb = new StringBuilder();
        for (int toPrepend = n - s.length(); toPrepend > 0; toPrepend--) {
            sb.append(c);
        }
        sb.append(s);
        return sb.toString();
    }

    private File downloadFile(final FileToDownload file)
            throws MalformedURLException, FileNotFoundException, IOException, InterruptedException {
        if (file == null) {
            throw new NullPointerException("file");
        }
        final File tempFile = File.createTempFile(file.localName.getName(), ".downloaded");
        final URL website = new URL(file.baseUrl + file.remoteName);

        try (InputStream siteStream = website.openStream()) {
            PathUtil.copyStreamToFile(siteStream, tempFile);
        }
        return tempFile;
    }

    // =============================================================================================
    //                                                                      POST-DOWNLOAD PROCESSING
    // =============================================================================================
    private synchronized void deployFile(final File processedFile, File targetFile) {
        if (processedFile == null) {
            throw new NullPointerException("processedFile");
        }
        if (targetFile == null) {
            throw new NullPointerException("localName");
        }
        LogUtil.getLogger().log(Level.INFO, "Deploying {0}", targetFile);
        try {
            final File parentDir = targetFile.getCanonicalFile().getParentFile();
            if (!parentDir.exists() && !parentDir.mkdirs()) {
                throw new IOException("Unable to make directory " + parentDir);
            }
            PathUtil.replaceFile(processedFile, targetFile);
        } catch (final IOException ex) {
            LogUtil.getLogger().log(Level.SEVERE, "Error deploying " + targetFile.getName(), ex);
        }
    }

    // Checks all local files to make sure that the client is ready to launch
    private void verifyFiles(final List<FileToDownload> files) {
        if (files == null) {
            throw new NullPointerException("files");
        }

        for (final FileToDownload file : files) {
            if (!LAUNCHER_JAR.equals(file.localName.getName()) && !file.localName.exists()) {
                throw new RuntimeException("Update process failed. Missing file: " + file.localName);
            }
        }
    }

    // =============================================================================================
    //                                                                            PROGRESS REPORTING
    // =============================================================================================
    private volatile UpdateScreen updateScreen;
    private static boolean updateFinished = false;

    public static boolean getUpdateFinished() {
        // If "keep open" option is on, we only want the updater to run once (before first launch).
        return updateFinished;
    }

    public static void setUpdateFinished(boolean value) {
        // Set to 'true' by UpdateScreen after a successful update
        updateFinished = value;
    }

    @Override
    protected synchronized void process(final List<ProgressUpdate> chunks) {
        if (chunks == null) {
            throw new NullPointerException("chunks");
        }
        if (this.updateScreen != null) {
            this.updateScreen.setStatus(chunks.get(chunks.size() - 1));
        }
    }

    private void signalCheckProgress(final String fileName) {
        if (fileName == null) {
            throw new NullPointerException("fileName");
        }
        this.publish(new ProgressUpdate("Checking " + fileName, -1));
    }

    private void signalDone() {
        final String message = (this.updatesApplied ? "Updates applied." : "No updates needed.");
        this.publish(new ProgressUpdate(message, 100));
    }

    @Override
    protected synchronized void done() {
        if (this.updateScreen != null) {
            this.signalDone();
            this.updateScreen.onUpdateDone(this.updatesApplied);
        }
    }

    public synchronized void registerUpdateScreen(final UpdateScreen updateScreen) {
        if (updateScreen == null) {
            throw new NullPointerException("updateScreen");
        }
        this.updateScreen = updateScreen;
        if (this.isDone()) {
            this.signalDone();
            updateScreen.onUpdateDone(this.updatesApplied);
        }
    }

    // =============================================================================================
    //                                                                                   INNER TYPES
    // =============================================================================================
    public final static class ProgressUpdate {

        public String statusString;
        public int progress;

        public ProgressUpdate(final String statusString, final int progress) {
            if (statusString == null) {
                throw new NullPointerException("statusString");
            }
            this.statusString = statusString;
            this.progress = progress;
        }
    }

    private final static class FileToDownload {

        // remote filename
        public final String baseUrl;
        public final String remoteName;
        public final File localName;
        public final File targetName;

        FileToDownload(final String baseUrl, final String remoteName, final File localName) {
            this(baseUrl, remoteName, localName, localName);
        }

        FileToDownload(final String baseUrl, final String remoteName, final File localName, final File targetName) {
            this.baseUrl = baseUrl;
            this.remoteName = remoteName;
            this.localName = localName;
            this.targetName = targetName;
        }
    }

    private final static class RemoteFile {

        String name;
        String hash;
    }

    private class DownloadThread extends Thread {

        private final Logger logger;

        DownloadThread(Logger logger) {
            this.logger = logger;
        }

        @Override
        public void run() {
            FileToDownload file = null;
            try {
                file = getNextFileSync(false);
                while (file != null) {
                    processOneFile(file);
                    file = getNextFileSync(true);
                }

            } catch (final Exception ex) {
                String fileName = (file != null ? file.remoteName : "?");
                logger.log(Level.SEVERE, "Error downloading or deploying an updated file: " + fileName, ex);
            }
        }
    }
}
