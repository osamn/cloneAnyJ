package ReIW.tiny.cloneAny.core;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.atomic.AtomicReference;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.ModuleVisitor;
import org.objectweb.asm.TypePath;

import static org.objectweb.asm.Opcodes.*;

public final class AssemblyDomain extends ClassLoader {

	private static final AtomicReference<AssemblyDomain> domainRef;

	static {
		final AssemblyDomain domain = AccessController.doPrivileged(new PrivilegedAction<AssemblyDomain>() {
			@Override
			public AssemblyDomain run() {
				return new AssemblyDomain(ClassLoader.getSystemClassLoader());
				// return new AssemblyDomain(Thread.currentThread().getContextClassLoader());
			}
		});
		domainRef = new AtomicReference<AssemblyDomain>(domain);
	}

	public static AssemblyDomain getDefaultAssemblyDomain() {
		return domainRef.get();
	}

	public static AssemblyDomain setDefaultAssemblyDomain(final AssemblyDomain domain) {
		return domainRef.getAndSet(domain);
	}

	public AssemblyDomain() {
		super();
	}

	public AssemblyDomain(final ClassLoader parent) {
		super(parent);
	}

	public ClassVisitor getTerminalClassVisitor() {
		return new ClassResolver();
	}

	public Class<?> forName(final String name) throws ClassNotFoundException {
		final String clazzName = name.replace('/', '.'); // internal name の場合もあるためここで吸収しておく
		final Class<?> clazz = findLoadedClass(clazzName);
		if (clazz == null) {
			throw new ClassNotFoundException(clazzName);
		}
		return clazz;
	}

	/** for Debug use only;-) */
	public ClassVisitor getTerminalClassVisitor(final ClassVisitor inspector) {
		// ClassVisitor チェーンの一番最後に ClassResolver を連結
		// わざわざ reflection でやるまでもないし、本来 test 側でやった方がいいよね
		// まあ使い勝手をみて削除するかも
		try {
			final Field f = ClassVisitor.class.getDeclaredField("cv");
			f.setAccessible(true);

			ClassVisitor cv = inspector;
			ClassVisitor fcv = (ClassVisitor) f.get(cv);
			while (fcv != null) {
				cv = fcv;
				fcv = (ClassVisitor) f.get(cv);
			}
			f.set(cv, getTerminalClassVisitor());
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			throw new AssemblyException(e);
		}
		return inspector;
	}

	private final class ClassResolver extends ClassVisitor {
		private final ClassVisitor cv;
		private final ClassWriter cw;
		private String className;

		private ClassResolver() {
			super(ASM7);
			this.cw = new ClassWriter(0);
			this.cv = this.cw;
		}

		public void visit(int version, int access, String name, String signature, String superName,
				String[] interfaces) {
			this.className = name.replace('/', '.');
			cv.visit(version, access, name, signature, superName, interfaces);
		}

		public void visitSource(String source, String debug) {
			cv.visitSource(source, debug);
		}

		public ModuleVisitor visitModule(String name, int access, String version) {
			return cv.visitModule(name, access, version);
		}

		public void visitNestHost(String nestHost) {
			cv.visitNestHost(nestHost);
		}

		public void visitOuterClass(String owner, String name, String descriptor) {
			cv.visitOuterClass(owner, name, descriptor);
		}

		public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
			return cv.visitAnnotation(descriptor, visible);
		}

		public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor,
				boolean visible) {
			return cv.visitTypeAnnotation(typeRef, typePath, descriptor, visible);
		}

		public void visitAttribute(Attribute attribute) {
			cv.visitAttribute(attribute);
		}

		public void visitNestMember(String nestMember) {
			cv.visitNestMember(nestMember);
		}

		public void visitInnerClass(String name, String outerName, String innerName, int access) {
			cv.visitInnerClass(name, outerName, innerName, access);
		}

		public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
			return cv.visitField(access, name, descriptor, signature, value);
		}

		public MethodVisitor visitMethod(int access, String name, String descriptor, String signature,
				String[] exceptions) {
			if (AccessFlag.isFinal(access)) {
				return null;
			}
			return cv.visitMethod(access, name, descriptor, signature, exceptions);
		}

		public void visitEnd() {
			cv.visitEnd();
			final byte[] b = cw.toByteArray();
			final Class<?> clazz = AssemblyDomain.this.defineClass(className, b, 0, b.length,
					AssemblyDomain.class.getProtectionDomain());
			AssemblyDomain.this.resolveClass(clazz);
		}
	}
}
