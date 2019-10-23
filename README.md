# gensource
版本号生成器
简单易用

使用APT(Annotation Processing Tool) 在编译时候使用javassist修改版本号方法返回，实现了每次在打包机器上自动升级版本号的功能

@VersionAnnotation
value：版本号，每次打包会自动在最后面的首个数值自加1，如果大版本号改变了则会自动从0开始。
path：存放版本号的临时文件


demo：
@VersionAnnotation(value="1.1.1", path="D:\\")
public class Application {

	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println(Version.getVersion());
	}

}
