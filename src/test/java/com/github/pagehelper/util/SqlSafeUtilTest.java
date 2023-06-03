package com.github.pagehelper.util;

import org.junit.Test;

import static org.junit.Assert.*;

public class SqlSafeUtilTest {

	@Test
	public void check() {
		assertSql(true, "insert into xx");
		// 无空格
		assertSql(false, "insertxxinto xx");
		assertSql(false, "insert_into");
		assertSql(true, "SELECT aa FROM user");
		// 无空格
		assertSql(true, "SELECT*FROM user");
		// 左空格
		assertSql(true, "SELECT *FROM user");
		// 右空格
		assertSql(true, "SELECT* FROM user");
		// 左tab
		assertSql(true, "SELECT                 *FROM user");
		// 右tab
		assertSql(true, "SELECT*        FROM user");
		assertSql(false, "SELECT*FROMuser");

		// 验证 issue #707 问题
		assertSql(false, "databaseType desc,orderNum desc");
	}

	private void assertSql(boolean injection, String sql) {
		assertEquals(injection, SqlSafeUtil.check(sql));
	}
}