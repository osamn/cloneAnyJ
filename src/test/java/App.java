import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.function.Consumer;

import ReIW.tiny.cloneAny.stream.StreamExtention;

public class App {

	@FunctionalInterface
	interface Hoge {

		void accept(String t) throws Exception;

	}

	public static void main(String[] args) throws Exception {
		File f = new File("C:\\.dev\\java\\foo.txt");

		try (var lines = Files.lines(f.toPath()); var stream = StreamExtention.of(lines)) {
			stream.tryMap(s -> {
				if (s.contentEquals("b")) {
					throw new IOException();
				}
				return s;
			}, (e, val) -> {
				System.out.println(val + ":" + e);
				return true;
			}).forEach(System.out::println);
		}

	}

}
