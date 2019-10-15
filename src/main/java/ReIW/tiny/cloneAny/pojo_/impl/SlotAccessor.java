package ReIW.tiny.cloneAny.pojo_.impl;

import java.util.Map;

import ReIW.tiny.cloneAny.pojo_.Accessor;

interface SlotAccessor extends Accessor {

	SlotAccessor chown(String owner);

	SlotAccessor rebind(Map<String, String> binds);

}
