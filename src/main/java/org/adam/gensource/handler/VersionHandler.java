/**
 * 
 */
package org.adam.gensource.handler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

/**
 * @author nixiaorui
 *
 */
public class VersionHandler {

	public static final String FILE_NAME = "/vfile";

	public static void handle(String version, String versionFilePath) {
		try {
			ClassPool pool = ClassPool.getDefault();
			Class<org.adam.version.Version> clazz = org.adam.version.Version.class;
			String path = clazz.getProtectionDomain().getCodeSource().getLocation().getPath();
			pool.insertClassPath(path);
			CtClass cc = pool.getCtClass(clazz.getName());
			// 取得需要修改的方法
			CtMethod cMethod = cc.getDeclaredMethod("getVersion");

			String versionNew = getNewVersion(version, versionFilePath);
			cMethod.setBody("{ return \"" + versionNew + "\"; }");
			File dirFile = new File(path);
			dirFile.setWritable(true, true);
			JarOutputStream stream = new JarOutputStream(new FileOutputStream(path));
			String clazzName = clazz.getName().replace(".", "/") + ".class";
			JarEntry entry = new JarEntry(clazzName);
			stream.putNextEntry(entry);
			byte[] clazzBytes = cc.toBytecode();
			stream.write(clazzBytes);
			stream.close();
		} catch (CannotCompileException | IOException | NotFoundException e) {
			System.err.println("error occor:" + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * 获取新的版本号
	 * 
	 * @param versionTpl
	 * @param versionFilePath
	 * @return
	 * @throws IOException
	 */
	private static String getNewVersion(String versionTpl, String versionFilePath) throws IOException {
		Path path = Paths.get(versionFilePath + FILE_NAME);
		// 先获取旧的版本号
		String versionOld = getOldVersion(path, versionFilePath);
		System.out.println("version old is " + versionOld);
		String version = versionTpl;
		// 如果是同个体系的版本号则进行处理，不同体系的不用处理
		if (isSameVersion(versionTpl, versionOld)) {
			version = dealSameVersion(versionTpl, versionOld);
		}
		updateVersion(version, path);
		return version;
	}

	/**
	 * 更新文件上的版本号
	 * 
	 * @param version
	 * @param path
	 * @throws IOException
	 */
	private static void updateVersion(String version, Path path) throws IOException {
		Files.write(path, Arrays.asList(version), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
	}

	/**
	 * 处理如果版本号体系是一致的情况
	 * 
	 * @param version
	 * @return
	 */
	private static String dealSameVersion(String versionTpl, String version) {
		String[] versionTplStruct = getLastNodeStruct(versionTpl.substring(versionTpl.lastIndexOf(".")));
		String[] versionStruct = getLastNodeStruct(version.substring(version.lastIndexOf(".")));
		int versionTplDigit = Integer.parseInt(versionTplStruct[1]);
		int versionDigit = Integer.parseInt(versionStruct[1]);
		// 旧版本号要自加1
		int digit = Math.max(versionTplDigit, versionDigit + 1);

		return version.substring(0, version.lastIndexOf(".")) + versionTplStruct[0] + digit + versionTplStruct[2];
	}

	/**
	 * 获取最后一个节点的结构数据
	 * 
	 * @param versionSub
	 * @return 【0】前缀 【1】数字 【2】后缀
	 * 
	 */
	private static String[] getLastNodeStruct(String versionSub) {
		StringBuilder tmpDigit = new StringBuilder(versionSub.length());
		boolean isBefore = true;
		// 前缀
		StringBuilder before = new StringBuilder(versionSub.length());
		int i = 0;
		// 认到第一个数字及后面连续的数字，如果后面有数字被分隔开会忽视
		for (; i < versionSub.length(); i++) {
			if (Character.isDigit(versionSub.charAt(i))) {
				tmpDigit.append(versionSub.charAt(i));
				isBefore = false;
			} else {
				if (isBefore) {
					before.append(versionSub.charAt(i));
				} else {
					break;
				}
			}
		}
		// 后缀
		String after = versionSub.substring(i);
		return new String[] { before.toString(), (0 == tmpDigit.length()) ? "0" : tmpDigit.toString(), after };
	}

	/**
	 * 看看版本号体系有没升级
	 * 
	 * @param version
	 * @param versionOld
	 * @return
	 */
	private static boolean isSameVersion(String version, String versionOld) {
		if (null == versionOld || "".equals(versionOld)) {
			return false;
		}

		String versionPre = version.substring(0, version.lastIndexOf("."));
		String versionOldPre = versionOld.substring(0, versionOld.lastIndexOf("."));

		return versionPre.equals(versionOldPre);
	}

	/**
	 * 如果文件不存在则创建文件，存在则读以前版本号
	 * 
	 * @param path
	 * @param versionFilePath
	 * @return
	 * @throws IOException
	 */
	private static String getOldVersion(Path path, String versionFilePath) throws IOException {
		if (Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
			List<String> lines = Files.readAllLines(path);
			if (null == lines || lines.size() < 1) {
				return null;
			}
			return lines.get(0);
		} else {
			Files.createDirectories(Paths.get(versionFilePath));
			Files.createFile(path);
		}
		return null;
	}

	public static void main(String[] args) throws IOException {
		getNewVersion("0.0.0-snapshot", "D:/test/test");
	}
}
