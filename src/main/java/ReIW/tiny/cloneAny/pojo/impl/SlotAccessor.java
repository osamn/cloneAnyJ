package ReIW.tiny.cloneAny.pojo.impl;

import java.util.Map;

import ReIW.tiny.cloneAny.pojo.Accessor;

interface SlotAccessor extends Accessor {

	SlotAccessor chown(String owner);

	SlotAccessor rebind(Map<String, String> binds);

}
