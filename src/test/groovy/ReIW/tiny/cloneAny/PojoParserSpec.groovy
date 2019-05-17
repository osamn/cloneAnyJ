package ReIW.tiny.cloneAny

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
