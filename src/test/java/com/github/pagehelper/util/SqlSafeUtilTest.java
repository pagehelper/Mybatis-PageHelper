package com.github.pagehelper.util;

import org.junit.Test;

import static org.junit.Assert.*;

public class SqlSafeUtilTest {

	@Test
	public void check() {
		String sql = "insert into xx";
		assertTrue(SqlSafeUtil.check(sql));

		sql = "insertxxinto xx";
		assertFalse(SqlSafeUtil.check(sql));

		sql = "insert           into xx";
		assertTrue(SqlSafeUtil.check(sql));

		// 验证 issue #707 问题
		sql = "databaseType desc,orderNum desc";
		assertFalse(SqlSafeUtil.check(sql));
	}
}