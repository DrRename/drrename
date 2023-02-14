package com.github.drrename;

import com.github.tools1000.io.BackupCreator;
import drrename.strategy.RenamingStrategy;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.File;
import java.nio.file.Path;

public abstract class AbstractRenamingStrategyTest {

	@BeforeEach
	public void setUp() throws Exception {

		if ((renamedFile != null) && renamedFile.toFile().exists())
			FileUtils.deleteQuietly(renamedFile.toFile());
		File testFile = getTestFile();
		if (!testFile.exists()) {
			if (!testFile.createNewFile())
				throw new RuntimeException("Could not read and create test file " + testFile.getPath());
		}

		backupFile = new BackupCreator().makeBackup(testFile);
		s = getStrategy();

	}

	@AfterEach
	public void tearDown() throws Exception {

		new BackupCreator().restoreBackup(backupFile);
		FileUtils.deleteQuietly(backupFile);
		backupFile = null;
		s = null;
		if((renamedFile != null) && renamedFile.toFile().exists())
			FileUtils.deleteQuietly(renamedFile.toFile());
	}

	protected File backupFile;
	protected Path renamedFile;
	protected RenamingStrategy s;

	protected abstract RenamingStrategy getStrategy();

	protected abstract File getTestFile();
}
