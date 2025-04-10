package com.github.hypfvieh.db;

import static java.lang.StackWalker.Option.RETAIN_CLASS_REFERENCE;

import com.github.hypfvieh.util.StringUtil;
import com.github.hypfvieh.util.SystemUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

/**
 * HyperSQL utility is intended to support database-backed tests by copying HyperSQL test databases
 * from the classpath to another location (such as the system's temp directory) and performing
 * clean-up work afterward.
 */
public final class HyperSqlUtil {
	private static final List<String> HYPERSQL_EXTENSIONS =
		Arrays.asList(".script", ".properties", ".data", ".log", ".tmp", ".lck", ".lobs");

	private final Logger              logger              = LoggerFactory.getLogger(getClass());

	private final Set<String>         copiedHsqlBasenames = new TreeSet<>();

	private final String              dbBaseName;
	private final File                targetDir;
	/** Remember if this object had to create <code>targetDir</code>. */
	private final boolean             targetDirCreated;
	private final boolean             deleteOnExit;

	/**
	 * Copies the given database to system temp directory using the calling class name as subdirectory.
	 * @param dbBaseName the base name of the database without extension; must not be null
	 */
	public HyperSqlUtil(String dbBaseName) {
		this(dbBaseName, createTempDir(), true);
	}

	/**
	 * Constructs a new instance of HyperSqlUtil to manage a HyperSQL database in a target directory.
	 * This constructor ensures the provided target directory exists, is writable, and may optionally
	 * delete the directory on JVM exit.
	 * <p>
	 * <b>Warning: Existing database files (dbBaseName + any of the extensions listed in {@link #HYPERSQL_EXTENSIONS}) will be removed!</b>
	 * </p>
	 * @param dbBaseName the base name of the database without extension; must not be null
	 * @param targetDir the directory where the database files will be managed; must not be null
	 * @param deleteOnExit specifies if the target directory should be deleted when the JVM exits
	 * @throws NullPointerException if dbBaseName or targetDir is null
	 * @throws RuntimeException if the target directory cannot be created or its read/write permissions cannot be set
	 */
	public HyperSqlUtil(String dbBaseName, File targetDir, boolean deleteOnExit) {
		this.dbBaseName = Objects.requireNonNull(dbBaseName, "Database base name required").replace("\\", "/");
		this.deleteOnExit = deleteOnExit;

		this.targetDir = Objects.requireNonNull(targetDir, "Target directory required");

		cleanupTargetDir();

		targetDirCreated = createTargetDir(targetDir);

		logger.debug("{} created with target dir '{}'", getClass().getSimpleName(), this.targetDir);
	}

	/**
	 * Ensures the existence of the target directory and its required permissions.
	 * If the directory does not exist, it attempts to create it.
	 * If the directory already exists but lacks the necessary read/write permissions,
	 * it adjusts them to be readable and writable.
	 *
	 * @param targetDir the target directory to create or validate; must not be null
	 * @return true if the directory was created, false if it already existed
	 * @throws RuntimeException if the directory cannot be created or its permissions cannot be set
	 */
	private boolean createTargetDir(File targetDir) {
		if (!targetDir.exists()) {
			if (!targetDir.mkdirs()) {
				throw new RuntimeException("Unable to create target directory " + targetDir);
			}
			return true;
		}

		if ((!targetDir.canRead() || !targetDir.canWrite())
			&& (!targetDir.setReadable(true) || !targetDir.setWritable(true))) {
				throw new RuntimeException("Unable to set read/write permissions on target directory " + targetDir);
		}
		return false;
	}

	/**
	 * Returns the path including database file name to use in JDBC URL.
	 * @return String
	 */
	public String getDbPath() {
		return targetDir + "/" + extractFileName(dbBaseName);
	}

	private String extractFileName(String dbBaseName) {
		return StringUtil.substringAfterLast(dbBaseName, "/");
	}

	/**
	 * Copies all files of a HyperSQL database, identified by its base name without extension,
	 * from the classpath with an optional search path <code>searchPath</code> to the target directory.<br>
	 * Each HyperSQL database consists of multiple files of identical base names but varying extensions.
	 *
	 * @param searchPath search path for database files, may be null
	 * @param dbBaseName database base name
	 * @param overwrite overwrite existing database files
	 * @param dbExtensions extensions to copy (including leading dot)
	 * @return the instance
	 */
	HyperSqlUtil copyHsqlDbFiles(String searchPath, String dbBaseName, boolean overwrite, String... dbExtensions) {
		logger.debug("Copying hsql database files with base name '{}'", dbBaseName);
		File destFile = null;
		for (String ext : dbExtensions) {
			String srcFileName = dbBaseName + ext;
			String pkgSrcFileName = (searchPath == null ? "" : searchPath + "/") + srcFileName;
			try (InputStream is = HyperSqlUtil.class.getClassLoader().getResourceAsStream(pkgSrcFileName)) {
				if (is == null) {
					// the .script file is always required, throw if not available
					if (".script".equalsIgnoreCase(ext)) {
						throw new RuntimeException("Required database file '" + pkgSrcFileName + "' does not exist");
					}

					logger.warn("Skipping non-existing file '{}'", pkgSrcFileName);
					continue;
				}
				copiedHsqlBasenames.add(dbBaseName);
				Objects.requireNonNull(is, "Could not get input " + srcFileName);
				destFile = new File(targetDir, extractFileName(srcFileName));
				if (deleteOnExit) {
					destFile.deleteOnExit();
				}
				if (destFile.exists() && !overwrite) {
					logger.info("Not copying file '{}' to existing file '{}'", srcFileName, destFile);
				} else {
					logger.debug("Copying file '{}' to '{}'", srcFileName, destFile);
					Files.copy(is, destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
				}
			} catch (IOException ex) {
				throw new UncheckedIOException("Failed to copy " + srcFileName + " to " + destFile, ex);
			}

		}
		return this;
	}

	/**
	 * @see #copyHsqlDbFiles(String, String, boolean, String...)
	 */
	public HyperSqlUtil copyHsqlDbFiles(String searchPath, boolean overwrite) {
		return copyHsqlDbFiles(searchPath, dbBaseName, overwrite, ".script", ".properties", ".data", ".lobs");
	}

	/**
	 * @see #copyHsqlDbFiles(String, String, boolean, String...)
	 */
	public HyperSqlUtil copyHsqlDbFiles() {
		return copyHsqlDbFiles(null, true);
	}

	/**
	 * Cleans and removes the target directory.
	 */
	public void cleanupTargetDir() {
		logger.debug("Cleaning up target dir '{}'", targetDir);

		if (!targetDir.exists()) {
			logger.debug("Cannot delete non-existing target directory '{}'", targetDir);
			return;
		}

		for (String baseTarget : copiedHsqlBasenames) {
			for (String ext : HYPERSQL_EXTENSIONS) {
				File file = new File(targetDir, baseTarget + ext);
				if (file.exists()) {
					if (file.isDirectory()) {
						logger.debug("Deleting dir '{}'", file);
						SystemUtil.deleteRecursivelyQuiet(file.getAbsolutePath());
					} else {
						logger.debug("Deleting file '{}'", file);
						boolean deleted = file.delete();
						if (!deleted) {
							logger.warn("Failed to delete file '{}'", file);
						}
					}
				}
			}
		}

		if (targetDirCreated) {
			logger.debug("Deleting target directory '{}'", targetDir);
			SystemUtil.deleteRecursivelyQuiet(targetDir.getAbsolutePath());
		}
	}

	public File getTargetDir() {
		return targetDir;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName()
			+ "["
			+ "baseName=" + dbBaseName + ", targetDir=" + targetDir
			+ "]";
	}

	/**
	 * Creates a temporary directory within the system's temporary file directory.
	 * The directory name is derived from the calling class's simple name.
	 * The directory is marked for deletion upon the JVM's termination.
	 *
	 * @return the created temporary directory as a File object
	 */
	private static File createTempDir() {
		String subDirName = StackWalker.getInstance(RETAIN_CLASS_REFERENCE).walk(e ->
				e.filter(f -> !f.getClassName().equals(HyperSqlUtil.class.getName())).findFirst())
			.map(f -> StringUtil.substringAfterLast(f.getClassName(), "."))
			.orElse("NOT_FOUND");

		File tmpDir = new File(System.getProperty("java.io.tmpdir"), subDirName);
		tmpDir.deleteOnExit();
		return tmpDir;
	}
}
