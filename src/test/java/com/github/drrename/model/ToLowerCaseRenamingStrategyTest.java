package com.github.drrename.model;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Paths;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ToLowerCaseRenamingStrategyTest {

	private ToLowerCaseRenamingStrategy s;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test01() throws IOException, InterruptedException {
		s = new ToLowerCaseRenamingStrategy();
		assertEquals("test", s.getNameNew(Paths.get("/home/TEST")));
	}

}