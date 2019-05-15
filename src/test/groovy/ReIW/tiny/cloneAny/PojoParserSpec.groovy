package ReIW.tiny.cloneAny

import static org.junit.jupiter.api.Assertions.*

import java.util.function.Consumer

import org.junit.jupiter.api.Test

import spock.lang.Specification

class PojoParserSpec extends Specification {

	def "とりあえずぱーす" () {
		setup:
		def parser = new PojoParser({val -> System.out.println(val.toString() + ";")})

		when:
		parser.parse("beans.Struct1")
		
		then:
		
		System.out.println("---")

	}

}
